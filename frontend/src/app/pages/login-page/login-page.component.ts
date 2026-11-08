import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.css',
})
export class LoginPageComponent {
  email = '';
  password = '';
  loading = false;

  constructor(private readonly router: Router) {}

  login() {
    this.loading = true;
    window.setTimeout(() => {
      this.loading = false;
      void this.router.navigateByUrl('/');
    }, 800);
  }
}


