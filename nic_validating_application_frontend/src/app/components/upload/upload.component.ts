import { Component } from '@angular/core';
import { FileService } from '../../services/file.service';
import { OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatListModule } from '@angular/material/list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { AuthService } from '../../services/auth.service';
import { NicService } from '../../services/nic.service';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';

interface ValidationResult {
  fileName: string;
  totalRecords: number;
  validNics: number;
  invalidNics: number;
}

@Component({
  selector: 'app-upload',
  imports: [CommonModule,MatListModule,MatProgressBarModule,MatIconModule,MatButtonModule,MatListModule,MatTableModule ],
  templateUrl: './upload.component.html',
  styleUrl: './upload.component.css'
})
export class UploadComponent{
files: File[] = [];
  uploadProgress: number | null = null;
  isUploading = false;
  uploadedFiles: any[] = [];
  currentSessionFiles: any[] = []; // New property to track current session files
  validationResults: any[] = [];

  constructor(
    private fileUploadService: FileService,
    private authService: AuthService,
    private snackBar: MatSnackBar,
    private nicService: NicService
  ) {}

  displayedColumns: string[] = [
    'nicNumber',
    'birthday',
    'age',
    'gender',
    'isValid',
    'fileName'
  ]

  onFileSelect(event: any): void {
    const selectedFiles = event.target.files;
    if (selectedFiles.length + this.files.length > 4) {
      this.snackBar.open('You can upload a maximum of 4 files', 'Close', { duration: 3000 });
      return;
    }
    
    for (let i = 0; i < selectedFiles.length; i++) {
      const file = selectedFiles[i];
      if (file.type === 'text/csv' || file.name.endsWith('.csv')) {
        this.files.push(file);
      } else {
        this.snackBar.open('Only CSV files are allowed', 'Close', { duration: 3000 });
      }
    }
  }

  removeFile(index: number): void {
    this.files.splice(index, 1);
  }

  uploadFiles(): void {
    if (this.files.length !== 4) {
      this.snackBar.open('Please upload exactly 4 files', 'Close', { duration: 3000 });
      return;
    }

    const currentUser = this.authService.currentUserValue;
    console.log('Current user:', currentUser);
    if (!currentUser?.username) {
      this.snackBar.open('Please login to upload files', 'Close', { duration: 3000 });
      return;
    }

    this.isUploading = true;
    this.uploadProgress = 0;

    this.fileUploadService.uploadFiles(this.files, currentUser.username).subscribe({
      next: (event: any) => {
        if (event.type === 1 && event.loaded && event.total) {
          this.uploadProgress = Math.round((event.loaded / event.total) * 100);
        } else if (event.body) {
          // Store the current session files
          this.currentSessionFiles = event.body.files;
          this.snackBar.open('Files uploaded successfully', 'Close', { duration: 3000 });
          
          // Reset the file input
          this.files = [];
          
          // Optionally auto-validate the uploaded files
          this.autoValidateCurrentFiles();
        }
      },
      error: (err) => {
        this.snackBar.open(`Upload failed: ${err.error?.error || 'Unknown error'}`, 'Close', { duration: 5000 });
        this.isUploading = false;
        this.uploadProgress = null;
      },
      complete: () => {
        this.isUploading = false;
        this.uploadProgress = null;
      }
    });
  }

  // Auto-validate the current session files
  autoValidateCurrentFiles(): void {
    if (this.currentSessionFiles.length === 0) {
      return;
    }

    const fileIds = this.currentSessionFiles.map(file => file.id);
    this.fileUploadService.parseFiles(fileIds).subscribe({
      next: (response) => {
        const fileData = response.results.map((result: any) => ({
          fileName: result.fileName,
          nicNumbers: result.nicNumbers
        }));
        
        const processedBy = this.authService.currentUserValue.username;
        this.nicService.validateNICNumbers(fileData, processedBy).subscribe({
          next: (validationResponse) => {
            this.validationResults = validationResponse.records;
            this.snackBar.open(`Validation completed: ${validationResponse.totalValid} valid, ${validationResponse.totalInvalid} invalid`, 'Close', { duration: 5000 });
          },
          error: (err) => {
            this.snackBar.open(`Validation failed: ${err.error.error}`, 'Close', { duration: 5000 });
          }
        });
      },
      error: (err) => {
        this.snackBar.open(`File parsing failed: ${err.error.error}`, 'Close', { duration: 5000 });
      }
    });
  }

  // Optional: Manual validation if needed
  validateCurrentFiles(): void {
    if (this.currentSessionFiles.length === 0) {
      this.snackBar.open('No files uploaded in current session', 'Close', { duration: 3000 });
      return;
    }

    this.autoValidateCurrentFiles();
  }

  // Clear current session (optional)
  clearCurrentSession(): void {
    this.currentSessionFiles = [];
    this.validationResults = [];
  }

}
