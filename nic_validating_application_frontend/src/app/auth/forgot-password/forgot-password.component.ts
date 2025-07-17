import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-forgot-password',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.css'
})
export class ForgotPasswordComponent {

  forgotPasswordForm: FormGroup;
  isLoading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  isEmailSent = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.forgotPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onForgotPassword() {
    if (this.forgotPasswordForm.valid) {
      this.isLoading = true;
      this.errorMessage = null;
      this.successMessage = null;

      const { email } = this.forgotPasswordForm.value;

      this.authService.forgotPassword(email).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.isEmailSent = true;
          this.successMessage = 'Password reset link has been sent to your email address.';
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = err || 'Failed to send reset email. Please try again.';
          console.error('Forgot password error:', err);
        }
      });
    }
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }

  resendEmail() {
    this.isEmailSent = false;
    this.successMessage = null;
    this.errorMessage = null;
    this.onForgotPassword();
  }

}
