export interface Personnel {
  id: string;
  name: string;
  email: string;
  role: string;
  status: string;
  statusType: 'success' | 'normal' | 'urgent';
  iconName: string;
  color: string;
}
