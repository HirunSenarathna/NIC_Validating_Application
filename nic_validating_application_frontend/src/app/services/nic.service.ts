import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';
import { HttpHeaders } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { HttpErrorResponse } from '@angular/common/http';

export interface NICRecord {
    id?: number;
    nicNumber: string;
    birthday: string;  
    age: number;
    gender: string;
    isValid: boolean;
    fileName: string;
    validationTime?: string;  
    processedBy: string;
}

export interface ValidationStatistics {
    totalRecords: number;
    validRecords: number;
    invalidRecords: number;
    uniqueFiles: number;
    uniqueUsers: number;
}

@Injectable({
  providedIn: 'root'
})
export class NicService {

  // private apiUrl = 'http://localhost:8083/api/nic';

  // constructor(
  //   private http: HttpClient,
  //   private authService: AuthService
  // ) { }

  // validateNICNumbers(fileData: any[], processedBy: string): Observable<any> {
  //   const payload = { fileData, processedBy };
  //   return this.http.post(`${this.apiUrl}/validate`, payload, {
  //     headers: this.authService.getAuthorizationHeaders()
  //   });
  // }

  // getAllRecords(): Observable<any> {
  //   return this.http.get(`${this.apiUrl}/records`, {
  //     headers: this.authService.getAuthorizationHeaders()
  //   });
  // }

  // getRecordsByFile(fileName: string): Observable<any> {
  //   return this.http.get(`${this.apiUrl}/records/file/${fileName}`, {
  //     headers: this.authService.getAuthorizationHeaders()
  //   });
  // }

  // getRecordsByUser(processedBy: string): Observable<any> {
  //   return this.http.get(`${this.apiUrl}/records/user/${processedBy}`, {
  //     headers: this.authService.getAuthorizationHeaders()
  //   });
  // }

  // getValidationStatistics(): Observable<any> {
  //   return this.http.get(`${this.apiUrl}/statistics`, {
  //     headers: this.authService.getAuthorizationHeaders()
  //   });
  // }

  // getFileStatistics(): Observable<any> {
  //   return this.http.get(`${this.apiUrl}/statistics/files`, {
  //     headers: this.authService.getAuthorizationHeaders()
  //   });
  // }

  // getGenderStatistics(): Observable<any> {
  //   return this.http.get(`${this.apiUrl}/statistics/gender`, {
  //     headers: this.authService.getAuthorizationHeaders()
  //   });
  // }

  // getAgeDistribution(): Observable<any> {
  //   return this.http.get(`${this.apiUrl}/statistics/age-distribution`, {
  //     headers: this.authService.getAuthorizationHeaders()
  //   });
  // }

  // getMonthlyTrends(months?: number): Observable<any> {
  //   const url = months 
  //     ? `${this.apiUrl}/statistics/monthly-trends?months=${months}`
  //     : `${this.apiUrl}/statistics/monthly-trends`;
    
  //   return this.http.get(url, {
  //     headers: this.authService.getAuthorizationHeaders()
  //   });
  // }

   private apiUrl = 'http://localhost:8080/api/nic';

  constructor(private http: HttpClient) {}

  validateNICNumbers(fileData: any[], processedBy: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/validate`, { fileData, processedBy });
  }

  getAllRecords(): Observable<NICRecord[]> {
    return this.http.get<NICRecord[]>(`${this.apiUrl}/records`);
  }

  getRecordsByFile(fileName: string): Observable<NICRecord[]> {
    return this.http.get<NICRecord[]>(`${this.apiUrl}/records/file/${fileName}`);
  }

  getRecordsByUser(processedBy: string): Observable<NICRecord[]> {
    return this.http.get<NICRecord[]>(`${this.apiUrl}/records/user/${processedBy}`);
  }

  getValidationStatistics(): Observable<ValidationStatistics> {
    return this.http.get<ValidationStatistics>(`${this.apiUrl}/statistics`);
  }

  getGenderStatistics(): Observable<any> {
    return this.http.get(`${this.apiUrl}/statistics/gender`);
  }

  getAgeDistribution(): Observable<any> {
    return this.http.get(`${this.apiUrl}/statistics/age-distribution`);
  }

   getFileStatistics(): Observable<any> {
    return this.http.get(`${this.apiUrl}/statistics/files`, {
    });
  }
}
