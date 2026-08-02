import {Component} from '@angular/core';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {
  /** The current calendar year, shown on the reading challenge prompt. */
  protected readonly year = new Date().getFullYear();
}
