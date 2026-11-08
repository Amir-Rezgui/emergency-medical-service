export interface Equipment {
  id: string;
  name: string;
  status: 'functional' | 'needs-maintenance' | 'out-of-service';
  lastChecked: string;
  quantity?: number;
}
