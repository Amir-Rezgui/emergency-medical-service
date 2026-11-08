import { isPlatformBrowser } from '@angular/common';
import { Injectable, NgZone, OnDestroy, PLATFORM_ID, Signal, computed, inject, signal } from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { Observable, Subject, Subscription } from 'rxjs';
import { DispatchAssignment } from '../../../app/models/dispatch.model';
import { IncidentRealtimeEvent } from '../../../app/models/incident.model';
import { environment } from '../../../environments/environment';

export interface NotificationItem {
  id: string;
  title: string;
  message: string;
  severity: 'info' | 'warning' | 'emergency';
  read: boolean;
  timestamp: Date;
  topic?: string;
}

interface TopicState<T> {
  parser: (message: IMessage) => T | null;
  subject: Subject<T>;
  refCount: number;
  subscription?: StompSubscription;
}

@Injectable({ providedIn: 'root' })
export class RealtimeService implements OnDestroy {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly zone = inject(NgZone);
  private readonly browser = isPlatformBrowser(this.platformId);

  readonly connected = signal(false);
  readonly notifications = signal<NotificationItem[]>([]);
  readonly incidentEvents = signal<IncidentRealtimeEvent[]>([]);
  readonly hospitalManagerIncidents = signal<IncidentRealtimeEvent[]>([]);
  readonly hospitalManagerDispatches = signal<DispatchAssignment[]>([]);
  readonly generalDriverDispatches = signal<DispatchAssignment[]>([]);
  readonly driverDispatchesByVehicle = signal<Record<string, DispatchAssignment[]>>({});

  readonly latestHospitalIncident = computed(() => this.hospitalManagerIncidents()[0] ?? null);
  readonly latestHospitalDispatch = computed(() => this.hospitalManagerDispatches()[0] ?? null);

  private readonly client?: Client;
  private readonly topics = new Map<string, TopicState<unknown>>();
  private readonly internalSubscriptions: Subscription[] = [];

  constructor() {
    if (!this.browser) {
      return;
    }

    this.client = this.createClient();
    this.bootstrapCoreStreams();
    this.client.activate();
  }

  ngOnDestroy() {
    this.internalSubscriptions.forEach((subscription) => subscription.unsubscribe());
    this.topics.forEach((state) => state.subscription?.unsubscribe());
    this.topics.clear();
    void this.client?.deactivate();
  }

  watchIncidentEvents() {
    return this.watchTopic<IncidentRealtimeEvent>(
      environment.websocket.topics.incidents,
      (message) => this.parseMessage<IncidentRealtimeEvent>(message),
    );
  }

  watchHospitalManagerIncidentEvents() {
    return this.watchTopic<IncidentRealtimeEvent>(
      environment.websocket.topics.hospitalManagerIncidents,
      (message) => this.parseMessage<IncidentRealtimeEvent>(message),
    );
  }

  watchHospitalManagerDispatches() {
    return this.watchTopic<DispatchAssignment>(
      environment.websocket.topics.hospitalManagerDispatches,
      (message) => this.parseMessage<DispatchAssignment>(message),
    );
  }

  watchDriverDispatches(vehicleId?: string) {
    const destination = vehicleId
      ? environment.websocket.topics.driverDispatchesByVehicle(vehicleId)
      : environment.websocket.topics.driverDispatches;

    return this.watchTopic<DispatchAssignment>(
      destination,
      (message) => this.parseMessage<DispatchAssignment>(message),
    );
  }

  driverDispatchesSignal(vehicleId: string): Signal<DispatchAssignment[]> {
    return computed(() => this.driverDispatchesByVehicle()[vehicleId] ?? []);
  }

  markAsRead(id: string) {
    this.notifications.update((items) => items.map((item) => item.id === id ? { ...item, read: true } : item));
  }

  clearAll() {
    this.notifications.set([]);
  }

  private createClient() {
    const client = new Client({
      brokerURL: environment.websocket.brokerUrl,
      reconnectDelay: environment.websocket.reconnectDelay,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: () => undefined,
    });

    client.onConnect = () => {
      this.zone.run(() => {
        this.connected.set(true);
        this.restoreSubscriptions();
      });
    };

    client.onDisconnect = () => {
      this.zone.run(() => this.connected.set(false));
    };

    client.onStompError = (frame) => {
      this.zone.run(() => {
        this.connected.set(false);
        this.pushNotification({
          title: 'Realtime broker error',
          message: frame.headers['message'] || 'The websocket broker returned an error frame.',
          severity: 'warning',
          topic: 'stomp',
        });
      });
    };

    client.onWebSocketClose = () => {
      this.zone.run(() => {
        this.connected.set(false);
        this.topics.forEach((state) => {
          state.subscription = undefined;
        });
      });
    };

    return client;
  }

  private bootstrapCoreStreams() {
    this.internalSubscriptions.push(
      this.watchIncidentEvents().subscribe((event) => {
        this.incidentEvents.update((items) => [event, ...items].slice(0, 20));
      }),
      this.watchHospitalManagerIncidentEvents().subscribe((event) => {
        this.hospitalManagerIncidents.update((items) => [event, ...items].slice(0, 20));
        this.pushNotification({
          title: `Incident ${event.action.replaceAll('_', ' ')}`,
          message: `${event.incident.title} at ${event.incident.location}`,
          severity: event.incident.type === 'urgent' ? 'emergency' : 'info',
          topic: environment.websocket.topics.hospitalManagerIncidents,
        });
      }),
      this.watchHospitalManagerDispatches().subscribe((dispatch) => {
        this.hospitalManagerDispatches.update((items) => [dispatch, ...items].slice(0, 20));
        this.pushDriverDispatch(dispatch);
        this.pushNotification({
          title: `Dispatch assigned to ${dispatch.vehicleName}`,
          message: `${dispatch.incidentTitle} • ${dispatch.dispatcher}`,
          severity: 'warning',
          topic: environment.websocket.topics.hospitalManagerDispatches,
        });
      }),
      this.watchDriverDispatches().subscribe((dispatch) => {
        this.pushDriverDispatch(dispatch);
      }),
    );
  }

  private watchTopic<T>(destination: string, parser: (message: IMessage) => T | null): Observable<T> {
    if (!this.browser) {
      return new Observable<T>();
    }

    return new Observable<T>((subscriber) => {
      const state = this.ensureTopic(destination, parser);
      state.refCount += 1;
      this.attachSubscription(destination, state);

      const subjectSubscription = state.subject.subscribe(subscriber);
      return () => {
        subjectSubscription.unsubscribe();
        state.refCount -= 1;
        if (state.refCount <= 0) {
          state.subscription?.unsubscribe();
          this.topics.delete(destination);
        }
      };
    });
  }

  private ensureTopic<T>(destination: string, parser: (message: IMessage) => T | null): TopicState<T> {
    const existing = this.topics.get(destination) as TopicState<T> | undefined;
    if (existing) {
      return existing;
    }

    const state: TopicState<T> = {
      parser,
      subject: new Subject<T>(),
      refCount: 0,
    };
    this.topics.set(destination, state as TopicState<unknown>);
    return state;
  }

  private attachSubscription<T>(destination: string, state: TopicState<T>) {
    if (!this.client || !this.connected() || state.subscription || state.refCount < 1) {
      return;
    }

    state.subscription = this.client.subscribe(destination, (message) => {
      const payload = state.parser(message);
      if (payload === null) {
        return;
      }

      this.zone.run(() => {
        state.subject.next(payload);
      });
    });
  }

  private restoreSubscriptions() {
    this.topics.forEach((state, destination) => {
      if (state.refCount > 0) {
        this.attachSubscription(destination, state);
      }
    });
  }

  private parseMessage<T>(message: IMessage): T | null {
    try {
      return JSON.parse(message.body) as T;
    } catch {
      return null;
    }
  }

  private pushDriverDispatch(dispatch: DispatchAssignment) {
    const dedupe = (items: DispatchAssignment[]) => items.filter((item, index, all) => all.findIndex(
      (candidate) => candidate.vehicleId === item.vehicleId && candidate.incidentId === item.incidentId && candidate.dispatchedAt === item.dispatchedAt,
    ) === index);

    this.driverDispatchesByVehicle.update((current) => ({
      ...current,
      [dispatch.vehicleId]: dedupe([dispatch, ...(current[dispatch.vehicleId] ?? [])]).slice(0, 10),
    }));

    this.generalDriverDispatches.update((items) => dedupe([dispatch, ...items]).slice(0, 20));
  }

  private pushNotification(notification: Omit<NotificationItem, 'id' | 'read' | 'timestamp'>) {
    const next: NotificationItem = {
      id: crypto.randomUUID(),
      read: false,
      timestamp: new Date(),
      ...notification,
    };
    this.notifications.update((items) => [next, ...items].slice(0, 25));
  }
}
