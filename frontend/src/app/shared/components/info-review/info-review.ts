import { Component, Input, OnInit } from '@angular/core';
import { HandleReportStatusPipe } from '../../../pipes/handle-report-status.pipe';
import { CommonModule } from '@angular/common';
import { reviewFromBacktoFrontDTO } from '../../../models/review/reviewFromBacktoFrontDTO';
import { Router } from '@angular/router';
import { HandleDeliveryPipe } from '../../../pipes/handle-delivery.pipe';
import { StarDisplayComponent } from '../../../pages/star-display.component/star-display.component';

@Component({
  selector: 'app-info-review',
  imports: [CommonModule, StarDisplayComponent, HandleDeliveryPipe],
  templateUrl: './info-review.html',
  styleUrl: './info-review.css'
})
export class InfoReview implements OnInit{

  @Input() review!: reviewFromBacktoFrontDTO|null;

  constructor(
    private router: Router
  ){}

  ngOnInit(): void {
    // console.log("[info-review] => datos: ", this.review);
  }

  redirectDetailsEvent(eventId: string|undefined){
    if(eventId){
      this.router.navigateByUrl("/event/management/"+eventId);
    }
  }

}
