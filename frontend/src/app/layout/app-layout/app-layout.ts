import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthenticatedHeader } from '../../shared/ui/authenticated-header/authenticated-header';
import { Footer } from '../../shared/ui/footer/footer';

@Component({
  selector: 'app-app-layout',
  standalone: true,
  imports: [RouterOutlet, AuthenticatedHeader, Footer],
  templateUrl: './app-layout.html',
  styleUrl: './app-layout.css'
})
export class AppLayout {}
