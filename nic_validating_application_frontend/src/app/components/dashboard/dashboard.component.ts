import { Component } from '@angular/core';
import { NicService } from '../../services/nic.service';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { OnInit } from '@angular/core';
import { DashboardService } from '../../services/dashboard.service';
import { ChartData, ChartType, ChartConfiguration } from 'chart.js';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { NgChartsModule } from 'ng2-charts';



@Component({
  selector: 'app-dashboard',
  imports: [CommonModule,MatCardModule,MatProgressBarModule,MatIconModule,MatListModule,NgChartsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit{

  dashboardData: any;
  isLoading = true;

  // Gender Pie Chart
  genderPieChartData: ChartData<'pie'> = {
    labels: [],
    datasets: [{
      data: [],
      backgroundColor: ['#3B82F6', '#EF4444']
    }]
  };
  genderPieChartType: ChartType = 'pie';

  // Validation Bar Chart
  validationBarChartData: ChartConfiguration<'bar'>['data'] = {
    labels: ['Valid', 'Invalid'],
    datasets: [{
      data: [],
      backgroundColor: ['#10B981', '#EF4444']
    }]
  };
  validationBarChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    scales: {
      y: {
        beginAtZero: true
      }
    }
  };

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.isLoading = true;
    this.dashboardService.getDashboardSummary().subscribe({
      next: (data) => {
        this.dashboardData = data;
        this.updateCharts();
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load dashboard data', err);
        this.isLoading = false;
      }
    });
  }

  updateCharts(): void {
    // Update gender pie chart
    this.genderPieChartData = {
      labels: ['Male', 'Female'],
      datasets: [{
        data: [
          this.dashboardData.maleUsers,
          this.dashboardData.femaleUsers,
        ],
        backgroundColor: ['#3B82F6', '#EF4444']
      }]
    };

    // Update validation bar chart
    this.validationBarChartData = {
      labels: ['Valid', 'Invalid'],
      datasets: [{
        data: [
          this.dashboardData.validRecords,
          this.dashboardData.invalidRecords
        ],
        backgroundColor: ['#10B981', '#EF4444']
      }]
    };
  }

}
