export interface Report {
  id: string;
  name: string;
  category: string;
  date: string;
  status: 'Ready' | 'Archived';
}
