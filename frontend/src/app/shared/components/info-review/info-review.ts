import { Component, Input, OnInit } from '@angular/core';
import { HandleReportStatusPipe } from '../../../pipes/handle-report-status.pipe';
import { CommonModule } from '@angular/common';
import { reviewFromBacktoFrontDTO } from '../../../models/review/reviewFromBacktoFrontDTO';

@Component({
  selector: 'app-info-review',
  imports: [CommonModule],
  templateUrl: './info-review.html',
  styleUrl: './info-review.css'
})
export class InfoReview implements OnInit{

  @Input() review!: reviewFromBacktoFrontDTO|null;

  constructor(
  ){}

  ngOnInit(): void {
    console.log("[info-review] => datos: ", this.review);
  }

}
