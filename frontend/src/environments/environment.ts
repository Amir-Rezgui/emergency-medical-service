export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080',
  websocket: {
    brokerUrl: 'ws://localhost:8080/ws-native',
    reconnectDelay: 5000,
    topics: {
      incidents: '/topic/incidents',
      hospitalManagerIncidents: '/topic/hospital-manager/incidents',
      hospitalManagerDispatches: '/topic/hospital-manager/dispatches',
      driverDispatches: '/topic/drivers/dispatches',
      driverDispatchesByVehicle: (vehicleId: string) => `/topic/drivers/${vehicleId}/dispatches`,
    },
  },
};
