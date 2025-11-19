import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { reviewFromBacktoFrontDTO } from '../../models/review/reviewFromBacktoFrontDTO';
import { DeliveryStatus } from '../../models/review/DeliveryStatus';
import { InfoReview } from '../../shared/components/info-review/info-review';
import { ReviewService } from '../../services/review.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-reviews',
  imports: [CommonModule, InfoReview],
  templateUrl: './reviews.html',
  styleUrl: './reviews.css'
})
export class Reviews implements OnInit{

  reviews: reviewFromBacktoFrontDTO[] = [];
  // reviews: reviewFromBacktoFrontDTO[] = [
  //   {
  //     name: "Leandro Daniel",
  //     surname: "Juarez",
  //     eventTitle: "Sorteo de pascua",
  //     delivery: DeliveryStatus.ANTES,
  //     score: 2,
  //     comment: "Esto funca??"
  //   },
  //   {
  //     name: "Oscar Pedro",
  //     surname: "Remunsi",
  //     eventTitle: "Sorteo magico",
  //     delivery: DeliveryStatus.A_TIEMPO,
  //     score: 5,
  //     comment: "El comentario del evento 2"
  //   },
  //   {
  //     name: "Gaston",
  //     surname: "Cruz",
  //     eventTitle: "Sorteo 2",
  //     delivery: DeliveryStatus.ANTES,
  //     score: 3.5,
  //     comment: "El comentario del evento 3"
  //   },
  //   {
  //     name: "Pedro Juan",
  //     surname: "Tores",
  //     eventTitle: "La rifa del 10",
  //     delivery: DeliveryStatus.DESPUES,
  //     score: 5,
  //     comment: "El comentario del evento 4"
  //   },
  //   {
  //     name: "Claudia",
  //     surname: "Del Rio",
  //     eventTitle: "Sorteamos todo",
  //     delivery: DeliveryStatus.A_TIEMPO,
  //     score: 1,
  //     comment: "El comentario del evento 5"
  //   }
  // ];

  constructor(
    private cdr: ChangeDetectorRef,
    private authService: AuthService,
    private reviewService: ReviewService
  ){}

  ngOnInit() {
    let mailOperator = this.authService.getCurrentUserValue()?.email ? this.authService.getCurrentUserValue()?.email: null;
    if(mailOperator){
      this.reviewService.getReviewsByUserEmail(mailOperator).subscribe(
        resp => {
          this.reviews = resp.data;
          console.log("[reviews] => reviews recuperadas: ", this.reviews);
          this.cdr.detectChanges();
        }
      )
    }
  }


}
