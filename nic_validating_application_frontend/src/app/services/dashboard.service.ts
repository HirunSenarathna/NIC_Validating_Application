import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ChartData {
  label: string;
  value: number;
}

export interface DashboardSummary {
  totalRecords: number;
  validRecords: number;
  invalidRecords: number;
  maleUsers: number;
  femaleUsers: number;
  totalFiles: number;
  totalUsers: number;
  validationSuccessRate: number;
  genderDistribution: ChartData[];
  validationStatusDistribution: ChartData[];
  fileProcessingStats: ChartData[];
  monthlyProcessingTrends: ChartData[];
  userActivityStats: ChartData[];
  ageDistribution: ChartData[];
  additionalMetrics: { [key: string]: any };
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

   private apiUrl = 'http://localhost:8080/api/analytics';

  constructor(private http: HttpClient) {}

  getDashboardSummary(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${this.apiUrl}/dashboard`);
  }
}
