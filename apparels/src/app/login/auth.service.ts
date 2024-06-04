import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private loginUrl = 'http://35.200.157.92:9005/users/login';

  constructor(private http: HttpClient) {}

  login(email: string, password: string): Observable<boolean> {
    return this.http.post<{ token: string,id:string }>(this.loginUrl, { email, password })
      .pipe(
        map(response => {
          // Store the token in local storage or any other storage
          localStorage.setItem('token', response.token);
          localStorage.setItem('userId', response.id);
          return true;
        }),
        catchError(error => {
          console.error('Login error', error);
          return of(false);
        })
      );
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  logout(): void {
    localStorage.removeItem('token');
  }
}