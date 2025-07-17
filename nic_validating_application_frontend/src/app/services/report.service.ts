import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// export interface ReportRequest {
//   reportType: string;
//   format: string;
//   fileName?: string;
//   processedBy?: string;
//   startDate?: string;  
//   endDate?: string;    
//   includeInvalid: boolean;
// }

// export interface ReportResponse {
//   content: any;
//   fileName: string;
//   contentType: string;
//   fileSize: number;
//   generatedBy: string;
//   reportType: string;
//   format: string;
// }

export interface ReportRequest {
  reportType: string;
  format: string;
  fileName?: string;
  processedBy?: string;
  startDate?: string;  
  endDate?: string;    
  includeInvalid: boolean;
  
  // New filter properties
  minAge?: number;
  maxAge?: number;
  genders?: string[];  // Array to support multiple gender selections
  validityFilter?: 'ALL' | 'VALID_ONLY' | 'INVALID_ONLY';
}

export interface ReportResponse {
  content: any;
  fileName: string;
  contentType: string;
  fileSize: number;
  generatedBy: string;
  reportType: string;
  format: string;
}

@Injectable({
  providedIn: 'root'
})
export class ReportService {

  private apiUrl = 'http://localhost:8080/api/analytics/reports';

  constructor(private http: HttpClient) {}

 generateReport(request: ReportRequest): Observable<Blob> {
    return this.http.post(`${this.apiUrl}/generate`, request, {
      responseType: 'blob'
    });
  }

  previewReport(request: ReportRequest): Observable<ReportResponse> {
    return this.http.post<ReportResponse>(`${this.apiUrl}/preview`, request);
  }
}
