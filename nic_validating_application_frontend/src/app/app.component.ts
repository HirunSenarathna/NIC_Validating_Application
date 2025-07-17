import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';

import { RouterOutlet } from '@angular/router';

import { AuthService } from './services/auth.service';
import { Router } from '@angular/router';
import { NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet,CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'nic_validating_application_frontend';
 showNavbar = true;

  constructor(public authService: AuthService, private router: Router) {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      // Hide navbar for login, register, and forgot-password routes
      const hiddenRoutes = ['/login', '/register', '/forgot-password'];
      this.showNavbar = !hiddenRoutes.includes(event.urlAfterRedirects);
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
