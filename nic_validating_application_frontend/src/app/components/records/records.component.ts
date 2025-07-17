import { Component, OnInit } from '@angular/core';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NicService } from '../../services/nic.service';
import { MatPaginatorModule,MatPaginator } from '@angular/material/paginator';
import { MatSortModule,MatSort } from '@angular/material/sort';
import { MatMenuModule } from '@angular/material/menu';
import { NicDetailsDialogComponent } from '../nic-details-dialog/nic-details-dialog.component';
import { MatChipsModule } from '@angular/material/chips';


import { ViewChild, AfterViewInit } from '@angular/core';


// Define an interface for your record type
export interface NicRecord {
  nicNumber: string;
  birthday: string;
  age: number;
  gender: string;
  isValid: boolean;
  fileName: string;
}

interface ActiveFilters {
  gender: string;
  validity: string;
  fileName: string;
}

interface AgeFilter {
  min: number | null;
  max: number | null;
}

@Component({
  selector: 'app-records',
  imports: [CommonModule, FormsModule, MatTableModule, MatButtonModule, MatInputModule, MatFormFieldModule, MatProgressSpinnerModule, MatIconModule,MatPaginatorModule,MatSortModule,MatMenuModule,MatButtonModule, MatChipsModule],
  templateUrl: './records.component.html',
  styleUrl: './records.component.css'
})
export class RecordsComponent implements OnInit {
 @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  dataSource = new MatTableDataSource<NicRecord>();
  originalData: NicRecord[] = [];
  displayedColumns: string[] = ['nicNumber', 'birthday', 'age', 'gender', 'isValid', 'fileName', 'actions'];
  isLoading = true;
  filterValue = '';

  activeFilters: ActiveFilters = {
    gender: 'all',
    validity: 'all',
    fileName: 'all'
  };

  ageFilter: AgeFilter = {
    min: null,
    max: null
  };

  constructor(private nicService: NicService, private dialog: MatDialog) {}

  ngOnInit(): void {
    this.loadRecords();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  loadRecords(): void {
    this.isLoading = true;
    this.nicService.getAllRecords().subscribe({
      next: (records: NicRecord[]) => {
        this.originalData = records;
        this.dataSource.data = records;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load records', err);
        this.isLoading = false;
      }
    });
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.filterValue = filterValue;
    this.applyAllFilters();
  }

  filterByGender(gender: string): void {
    this.activeFilters.gender = gender;
    this.applyAllFilters();
  }

  filterByValidity(validity: string): void {
    this.activeFilters.validity = validity;
    this.applyAllFilters();
  }

  filterByFile(fileName: string): void {
    this.activeFilters.fileName = fileName;
    this.applyAllFilters();
  }

  applyAgeFilter(): void {
    this.applyAllFilters();
  }

  clearAgeFilter(): void {
    this.ageFilter.min = null;
    this.ageFilter.max = null;
    this.applyAllFilters();
  }

  applyAllFilters(): void {
    let filteredData = [...this.originalData];

    // Apply text search filter
    if (this.filterValue) {
      const searchTerm = this.filterValue.toLowerCase();
      filteredData = filteredData.filter(record => 
        record.nicNumber.toLowerCase().includes(searchTerm) ||
        record.birthday.toLowerCase().includes(searchTerm) ||
        record.age.toString().includes(searchTerm) ||
        record.gender.toLowerCase().includes(searchTerm) ||
        record.fileName.toLowerCase().includes(searchTerm)
      );
    }

    // Apply gender filter
    if (this.activeFilters.gender !== 'all') {
      filteredData = filteredData.filter(record => record.gender === this.activeFilters.gender);
    }

    // Apply validity filter
    if (this.activeFilters.validity !== 'all') {
      const isValid = this.activeFilters.validity === 'valid';
      filteredData = filteredData.filter(record => record.isValid === isValid);
    }

    // Apply file filter
    if (this.activeFilters.fileName !== 'all') {
      filteredData = filteredData.filter(record => record.fileName === this.activeFilters.fileName);
    }

    // Apply age range filter
    if (this.ageFilter.min !== null || this.ageFilter.max !== null) {
      filteredData = filteredData.filter(record => {
        const age = record.age;
        const minAge = this.ageFilter.min !== null ? this.ageFilter.min : 0;
        const maxAge = this.ageFilter.max !== null ? this.ageFilter.max : 150;
        return age >= minAge && age <= maxAge;
      });
    }

    this.dataSource.data = filteredData;
  }

  viewDetails(record: NicRecord): void {
    this.dialog.open(NicDetailsDialogComponent, {
      width: '600px',
      data: record
    });
  }

  resetFilters(): void {
    this.filterValue = '';
    this.dataSource.filter = '';
    this.loadRecords();
  }

  resetAllFilters(): void {
    this.filterValue = '';
    this.activeFilters = {
      gender: 'all',
      validity: 'all',
      fileName: 'all'
    };
    this.ageFilter = {
      min: null,
      max: null
    };
    this.dataSource.data = this.originalData;
  }

  hasActiveFilters(): boolean {
    return this.activeFilters.gender !== 'all' ||
           this.activeFilters.validity !== 'all' ||
           this.activeFilters.fileName !== 'all' ||
           this.ageFilter.min !== null ||
           this.ageFilter.max !== null;
  }

  getUniqueFiles(): string[] {
    const files = this.originalData.map(record => record.fileName);
    return [...new Set(files)].sort();
  }

  getRecordCountByFile(fileName: string): number {
    return this.originalData.filter(record => record.fileName === fileName).length;
  }

  getValidRecordCountByFile(fileName: string): number {
    return this.originalData.filter(record => record.fileName === fileName && record.isValid).length;
  }

}
