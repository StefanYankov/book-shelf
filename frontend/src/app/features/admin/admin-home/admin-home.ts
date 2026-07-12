import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-admin-home',
  imports: [CommonModule, RouterLink],
  templateUrl: './admin-home.html',
  styleUrl: './admin-home.css',
})
export class AdminHome {}
