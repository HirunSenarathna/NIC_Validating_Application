import { Component } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Inject } from '@angular/core';
import { NicRecord } from '../records/records.component'; 
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { CommonModule } from '@angular/common';

import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-nic-details-dialog',
  imports: [MatIconModule,MatChipsModule,CommonModule, MatButtonModule],
  templateUrl: './nic-details-dialog.component.html',
  styleUrl: './nic-details-dialog.component.css'
})
export class NicDetailsDialogComponent {

  constructor(
    public dialogRef: MatDialogRef<NicDetailsDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: NicRecord
  ) {}

  formatDate(dateString: string): string {
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });
    } catch (error) {
      return dateString;
    }
  }

  getBirthYear(): number {
    try {
      return new Date(this.data.birthday).getFullYear();
    } catch (error) {
      return 0;
    }
  }

  getCentury(): string {
    const year = this.getBirthYear();
    if (year === 0) return 'Unknown';
    
    const century = Math.ceil(year / 100);
    const suffix = this.getOrdinalSuffix(century);
    return `${century}${suffix} Century`;
  }

  getAgeGroup(): string {
    const age = this.data.age;
    if (age < 13) return 'Child';
    if (age < 20) return 'Teenager';
    if (age < 60) return 'Adult';
    return 'Senior';
  }

  getAdditionalInfo(): boolean {
    return Boolean(this.data.isValid && this.data.birthday && this.data.age > 0);
  }

  private getOrdinalSuffix(num: number): string {
    const lastDigit = num % 10;
    const lastTwoDigits = num % 100;
    
    if (lastTwoDigits >= 11 && lastTwoDigits <= 13) {
      return 'th';
    }
    
    switch (lastDigit) {
      case 1: return 'st';
      case 2: return 'nd';
      case 3: return 'rd';
      default: return 'th';
    }
  }

  copyToClipboard(): void {
    if (navigator.clipboard?.writeText) {
      navigator.clipboard.writeText(this.data.nicNumber)
        .then(() => console.log('NIC number copied to clipboard'))
        .catch(err => console.error('Failed to copy to clipboard', err));
    } else {
      const textArea = document.createElement('textarea');
      textArea.value = this.data.nicNumber;
      document.body.appendChild(textArea);
      textArea.select();
      document.execCommand('copy');
      document.body.removeChild(textArea);
      console.log('NIC number copied to clipboard');
    }
  }

}
