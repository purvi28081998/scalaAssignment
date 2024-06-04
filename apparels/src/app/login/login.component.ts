import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  email: string = '';
  password: string = '';

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit(): void {
    this.authService.login(this.email, this.password).subscribe(
      (success:any) => {
        if (success) {
          this.router.navigate(['/apparels']);
        } else {
          // Handle login failure
          alert('Login failed');
        }
      },
      (error:any) => {
        console.error('Login error', error);
        alert('An error occurred');
      }
    );
  }
}