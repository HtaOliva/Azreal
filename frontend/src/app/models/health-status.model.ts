export interface HealthStatus {
  status: string;
  application: string;
  environment: string;
  timestamp: string;
  uptimeMillis: number;
}
