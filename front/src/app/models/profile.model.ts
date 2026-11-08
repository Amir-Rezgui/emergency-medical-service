export interface UserStat {
  label: string;
  value: string;
  iconName: string;
  color: string;
}

export interface UserProfile {
  name: string;
  role: string;
  id: string;
  email: string;
  phone: string;
  location: string;
  joined: string;
  specialization: string;
  stats: UserStat[];
}
