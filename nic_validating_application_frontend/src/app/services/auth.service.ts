import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { HttpHeaders } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { HttpErrorResponse } from '@angular/common/http';
import { throwError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

//  private apiUrl = 'http://localhost:8083/api/auth';
//   private tokenKey = 'auth_token';

//   constructor(private http: HttpClient) {}

//   private getHeaders(): HttpHeaders {
//     return new HttpHeaders()
//       .set('Content-Type', 'application/json')
//       .set('Accept', 'application/json');
//   }

//    getAuthorizationHeaders(): HttpHeaders {
//     const token = this.getToken();
//     return new HttpHeaders()
//       .set('Content-Type', 'application/json')
//       .set('Accept', 'application/json')
//       .set('Authorization', `Bearer ${token}`);
//   }

//   login(identifier: string, password: string): Observable<any> {
//     const payload = { identifier, password };
//     return this.http.post(`${this.apiUrl}/login`, payload, { headers: this.getHeaders() })
//       .pipe(
//         tap((response: any) => {
//           if (response?.token) {
//             localStorage.setItem(this.tokenKey, response.token);
//           }
//         }),
//         catchError(this.handleError)
//       );
//   }

//   register(user: any): Observable<any> {
//     return this.http.post(`${this.apiUrl}/register`, user, { headers: this.getHeaders() })
//       .pipe(catchError(this.handleError));
//   }

//   logout(): void {
//     localStorage.removeItem(this.tokenKey);
//   }

//   isLoggedIn(): boolean {
//     return !!localStorage.getItem(this.tokenKey);
//   }

//   getToken(): string | null {
//     return localStorage.getItem(this.tokenKey);
//   }

//    private handleError(error: HttpErrorResponse) {
//     let errorMessage = 'An error occurred';
//     if (error.error instanceof ErrorEvent) {
//       errorMessage = `Error: ${error.error.message}`;
//     } else {
//       errorMessage = `Server returned code: ${error.status}, error message is: ${error.error?.message || error.message}`;
//     }
//     return throwError(() => errorMessage);
//   }

 private apiUrl = 'http://localhost:8080/api/auth';
  private tokenKey = 'auth_token';
  private userKey = 'currentUser';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    });
  }

  getAuthorizationHeaders(): HttpHeaders {
    const token = this.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  login(identifier: string, password: string): Observable<any> {
  const payload = { identifier, password };
  return this.http.post(`${this.apiUrl}/login`, payload, { headers: this.getHeaders() })
    .pipe(
      tap((response: any) => {
        console.log('Login response:', response);
        if (response?.token) {
          localStorage.setItem(this.tokenKey, response.token);
        }
        // Store the complete user object
        const userData = {
          id: response.id,
          username: response.username,
          email: response.email,
          firstname: response.firstname,
          lastname: response.lastname,
          phone: response.phone
        };
        localStorage.setItem(this.userKey, JSON.stringify(userData));
        console.log('Stored user:', userData); 
      }),
      catchError(this.handleError)
    );
}

  // login(identifier: string, password: string): Observable<any> {
  //   const payload = { identifier, password };
  //   return this.http.post(`${this.apiUrl}/login`, payload, { headers: this.getHeaders() })
  //     .pipe(
  //       tap((response: any) => {
  //          console.log('Login response:', response); 
  //         if (response?.token) {
  //           localStorage.setItem(this.tokenKey, response.token);
  //         }
  //         if (response?.user) {
  //           localStorage.setItem(this.userKey, JSON.stringify(response.user));
  //            console.log('Stored user:', this.currentUserValue);
  //         }
  //       }),
  //       catchError(this.handleError)
  //     );
  // }

 register(user: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, user, { headers: this.getHeaders() })
      .pipe(catchError(this.handleError));
  }

  forgotPassword(email: string): Observable<any> {
    const payload = { email };
    return this.http.post(`${this.apiUrl}/forgot-password`, payload, { headers: this.getHeaders() })
      .pipe(catchError(this.handleError));
  }

  resetPassword(token: string, password: string): Observable<any> {
    const payload = { token, password };
    return this.http.post(`${this.apiUrl}/reset-password`, payload, { headers: this.getHeaders() })
      .pipe(catchError(this.handleError));
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  get currentUserValue(): any {
    const userJson = localStorage.getItem(this.userKey);
    return userJson ? JSON.parse(userJson) : null;
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An unknown error occurred';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Client error: ${error.error.message}`;
    } else {
      errorMessage = `Server error (${error.status}): ${error.error?.message || error.message}`;
    }
    return throwError(() => errorMessage);
  }
}
