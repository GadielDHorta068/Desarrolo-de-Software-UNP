import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-transparency',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './transparency.component.html',
  styleUrls: ['./transparency.component.css']
})
export class TransparencyComponent implements OnInit {
  ngOnInit(): void {
    window.scrollTo(0, 0);
  }
}