import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ReportService, ReportRequest } from '../../services/report.service';
import { NicService } from '../../services/nic.service';
import { AuthService } from '../../services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DatePipe } from '@angular/common';
import { OnInit } from '@angular/core';


import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';  
import { ChangeDetectorRef } from '@angular/core';


@Component({
  selector: 'app-reports',
   imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatTooltipModule
  ],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.css',
  providers: [DatePipe]
})
export class ReportsComponent implements OnInit {
 reportForm: FormGroup;
  formats = ['PDF', 'CSV', 'Excel'];
  reportTypes = ['All Records', 'By File', 'By User', 'By Date Range'];
  files: string[] = [];
  isLoading = false;
  
  // New filter options
  genderOptions = ['Male', 'Female'];
  validityOptions = [
    { value: 'ALL', label: 'All Records' },
    { value: 'VALID_ONLY', label: 'Valid Only' },
    { value: 'INVALID_ONLY', label: 'Invalid Only' }
  ];
  
  isPreviewLoading = false;
  previewData: any[] = [];
  previewColumns = ['nicNumber', 'birthday', 'age', 'gender', 'isValid', 'fileName'];

  constructor(
    private fb: FormBuilder,
    private reportService: ReportService,
    private nicService: NicService,
    private authService: AuthService,
    private snackBar: MatSnackBar,
    private datePipe: DatePipe,
    private cdr: ChangeDetectorRef
  ) {
    this.reportForm = this.fb.group({
      reportType: ['All Records', Validators.required],
      format: ['PDF', Validators.required],
      fileName: [''],
      processedBy: [''],
      startDate: [''],
      endDate: [''],
      includeInvalid: [false],
      
      // New filter form controls
      minAge: [''],
      maxAge: [''],
      genders: [[]],
      validityFilter: ['ALL']
    });
  }

  ngOnInit(): void {
    this.loadFileNames();
  }

  loadFileNames(): void {
    this.isLoading = true;
    this.nicService.getFileStatistics().subscribe({
      next: (files) => {
        this.files = files.map((f: any) => f.fileName);
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load file names', err);
        this.snackBar.open('Failed to load file names', 'Close', { duration: 3000 });
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  generateReport(): void {
    if (this.reportForm.invalid || this.isLoading) {
      return;
    }

    this.isLoading = true;
    this.cdr.detectChanges();
    const formValue = this.reportForm.value;

    const request: ReportRequest = {
      reportType: this.getReportTypeKey(formValue.reportType),
      format: formValue.format.toUpperCase(),
      fileName: formValue.fileName || undefined,
      processedBy: formValue.processedBy || undefined,
      startDate: formValue.startDate ? 
        this.formatDate(formValue.startDate) : undefined,
      endDate: formValue.endDate ? 
        this.formatDate(formValue.endDate) : undefined,
      includeInvalid: formValue.includeInvalid,
      
      // New filter parameters
      minAge: formValue.minAge || undefined,
      maxAge: formValue.maxAge || undefined,
      genders: formValue.genders && formValue.genders.length > 0 ? formValue.genders : undefined,
      validityFilter: formValue.validityFilter
    };

    this.reportService.generateReport(request).subscribe({
      next: (blob) => {
        this.downloadFile(blob, formValue.format);
        this.isLoading = false;
        this.cdr.detectChanges();
        this.snackBar.open('Report generated successfully', 'Close', { duration: 3000 });
      },
      error: (err) => {
        const message = err.status === 404 ? 'No records found for the selected criteria' : 
                       err.status === 500 ? 'Server error occurred' : 
                       'Failed to generate report';
        this.snackBar.open(message, 'Close', { duration: 3000 });
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  previewReport(): void {
    if (this.reportForm.invalid || this.reportForm.get('reportType')?.value === 'By Date Range') {
      return;
    }

    this.isPreviewLoading = true;
    this.cdr.detectChanges();
    const formValue = this.reportForm.value;

    const request: ReportRequest = {
      reportType: this.getReportTypeKey(formValue.reportType),
      format: 'CSV',
      fileName: formValue.fileName || undefined,
      processedBy: formValue.processedBy || undefined,
      startDate: undefined,
      endDate: undefined,
      includeInvalid: formValue.includeInvalid,
      
      // New filter parameters
      minAge: formValue.minAge || undefined,
      maxAge: formValue.maxAge || undefined,
      genders: formValue.genders && formValue.genders.length > 0 ? formValue.genders : undefined,
      validityFilter: formValue.validityFilter
    };

    this.reportService.previewReport(request).subscribe({
      next: () => {
        this.getPreviewRecords(request);
      },
      error: (err) => {
        this.snackBar.open('Failed to preview report', 'Close', { duration: 3000 });
        this.isPreviewLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  private formatDate(date: Date): string {
    return this.datePipe.transform(date, 'yyyy-MM-dd') || '';
  }

  private getPreviewRecords(request: ReportRequest): void {
    this.nicService.getAllRecords().subscribe({
      next: (records) => {
        this.previewData = records
          .filter(record => {
            // Existing filters
            if (request.reportType === 'BY_FILE' && request.fileName && record.fileName !== request.fileName) {
              return false;
            }
            if (request.reportType === 'BY_USER' && request.processedBy && record.processedBy !== request.processedBy) {
              return false;
            }
            if (!request.includeInvalid && !record.isValid) {
              return false;
            }
            
            // New filters
            if (request.minAge !== undefined && record.age < request.minAge) {
              return false;
            }
            if (request.maxAge !== undefined && record.age > request.maxAge) {
              return false;
            }
            if (request.genders && request.genders.length > 0 && !request.genders.includes(record.gender)) {
              return false;
            }
            if (request.validityFilter === 'VALID_ONLY' && !record.isValid) {
              return false;
            }
            if (request.validityFilter === 'INVALID_ONLY' && record.isValid) {
              return false;
            }
            
            return true;
          })
          .slice(0, 10);
        this.isPreviewLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.snackBar.open('Failed to load records for preview', 'Close', { duration: 3000 });
        this.isPreviewLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  clearPreview(): void {
    this.previewData = [];
  }

  private downloadFile(blob: Blob, format: string): void {
    const extension = format.toLowerCase() === 'excel' ? 'xlsx' : format.toLowerCase();
    const fileName = `nic_report_${new Date().toISOString().slice(0, 10)}.${extension}`;
    
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  }

  onReportTypeChange(type: string): void {
    const fileNameControl = this.reportForm.get('fileName');
    const processedByControl = this.reportForm.get('processedBy');
    const startDateControl = this.reportForm.get('startDate');
    const endDateControl = this.reportForm.get('endDate');

    // Reset all conditional controls
    [fileNameControl, processedByControl, startDateControl, endDateControl].forEach(control => {
      control?.reset();
      control?.clearValidators();
      control?.updateValueAndValidity();
    });

    // Set validators based on report type
    switch (type) {
      case 'By File':
        fileNameControl?.setValidators(Validators.required);
        fileNameControl?.updateValueAndValidity();
        break;
      case 'By User':
        processedByControl?.setValidators(Validators.required);
        processedByControl?.updateValueAndValidity();
        break;
      case 'By Date Range':
        startDateControl?.setValidators(Validators.required);
        endDateControl?.setValidators(Validators.required);
        startDateControl?.updateValueAndValidity();
        endDateControl?.updateValueAndValidity();
        break;
    }

    this.clearPreview();
    this.reportForm.updateValueAndValidity();
  }

  private getReportTypeKey(displayValue: string): string {
    return displayValue.replace(' ', '_').toUpperCase();
  }

  isButtonDisabled(): boolean {
    const disabled = this.reportForm.invalid || this.isLoading;
    return disabled;
  }

  // New helper methods for age validation
  onMinAgeChange(): void {
    const minAge = this.reportForm.get('minAge')?.value;
    const maxAge = this.reportForm.get('maxAge')?.value;
    
    if (minAge && maxAge && minAge > maxAge) {
      this.reportForm.get('maxAge')?.setValue(minAge);
    }
  }

  onMaxAgeChange(): void {
    const minAge = this.reportForm.get('minAge')?.value;
    const maxAge = this.reportForm.get('maxAge')?.value;
    
    if (minAge && maxAge && maxAge < minAge) {
      this.reportForm.get('minAge')?.setValue(maxAge);
    }
  }

  clearAllFilters(): void {
    this.reportForm.patchValue({
      minAge: '',
      maxAge: '',
      genders: [],
      validityFilter: 'ALL'
    });
    this.clearPreview();
  }

}
