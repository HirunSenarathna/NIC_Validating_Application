import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HttpEvent, HttpRequest } from '@angular/common/http';

export interface FileUploadResponse {
  success: boolean;
  message: string;
  fileIds?: number[];
}

export interface FileMetadata {
    id: number;
    fileName: string;
    originalName: string;
    fileSize: number;
    filePath: string;
    uploadedBy: string;
    uploadTime: string;
    processedStatus: string;
    totalRecords: number;
    validRecords: number;
    invalidRecords: number;
    processedTime?: string;
}
@Injectable({
  providedIn: 'root'
})
export class FileService {

//    private apiUrl = 'http://localhost:8084/api/files';

//   constructor(private http: HttpClient) {}

  // uploadFiles(files: File[], uploadedBy: string): Observable<any> {
  //   const formData = new FormData();
  //   files.forEach(file => {
  //     formData.append('files', file);
  //   });
  //   formData.append('uploadedBy', uploadedBy);
  //   return this.http.post(`${this.apiUrl}/upload`, formData);
  // }

  // parseFiles(fileIds: number[]): Observable<any> {
  //   return this.http.post(`${this.apiUrl}/parse`, { fileIds });
  // }

  // getFileMetadata(uploadedBy: string): Observable<any> {
  //   return this.http.get(`${this.apiUrl}/metadata`, { params: { uploadedBy } });
  // }

  // getFileStatistics(uploadedBy: string): Observable<any> {
  //   return this.http.get(`${this.apiUrl}/statistics`, { params: { uploadedBy } });
  // }

  // deleteFile(fileId: number): Observable<any> {
  //   return this.http.delete(`${this.apiUrl}/${fileId}`);
  // }

  private apiUrl = 'http://localhost:8080/api/files';

  constructor(private http: HttpClient) {}

  uploadFiles(files: File[], uploadedBy: string): Observable<HttpEvent<FileUploadResponse>> {
    const formData = new FormData();
    files.forEach(file => formData.append('files', file));
    formData.append('uploadedBy', uploadedBy);

    const req = new HttpRequest('POST', `${this.apiUrl}/upload`, formData, {
      reportProgress: true,
      responseType: 'json'
    });

    return this.http.request(req);
  }

  parseFiles(fileIds: number[]): Observable<any> {
    return this.http.post(`${this.apiUrl}/parse`, { fileIds });
  }

  getFileMetadata(uploadedBy: string): Observable<FileMetadata[]> {
    return this.http.get<FileMetadata[]>(`${this.apiUrl}/metadata?uploadedBy=${uploadedBy}`);
  }

  getFileSummary(fileId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${fileId}/summary`);
  }

  deleteFile(fileId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${fileId}`);
  }
}
