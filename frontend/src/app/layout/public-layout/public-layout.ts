import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {PublicHeader} from '../../shared/ui/public-header/public-header';
import {Footer} from '../../shared/ui/footer/footer';

@Component({
  selector: 'app-public-layout',
  standalone: true,
  imports: [RouterOutlet, PublicHeader, Footer],
  templateUrl: './public-layout.html',
  styleUrl: './public-layout.css'
})
export class PublicLayout {

}
