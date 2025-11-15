import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { EventsTemp, EventTypes, RaffleEvent, StatusEvent } from '../../../models/events.model';
import { HandleDatePipe } from '../../../pipes/handle-date.pipe';
import { CommonModule } from '@angular/common';
import { HandleTypePipe } from '../../../pipes/handle-type.pipe';
import { HandleIconTypePipe } from '../../../pipes/handle-icon-type.pipe';
import { TagCategory } from '../tag-category/tag-category';
import { HandleStatusPipe } from '../../../pipes/handle-status.pipe';
import { WinnerDTO } from '../../../models/winner.model';
import { TagPrize } from '../tag-prize/tag-prize';
import { StarRatingComponent } from '../../../pages/star-rating.component/star-rating.component';
import { ReviewService } from '../../../services/review.service';

@Component({
  selector: 'app-info-event',
  imports: [CommonModule, HandleDatePipe, HandleTypePipe, HandleIconTypePipe, HandleStatusPipe, TagCategory, TagPrize, StarRatingComponent],
  templateUrl: './info-event.html',
  styleUrl: './info-event.css'
})
export class InfoEvent implements OnInit, OnChanges{
  
  eventTypes = EventTypes;
  eventStatus = StatusEvent;

  @Input() event!: EventsTemp|null;
  // @Input() event!: RaffleEvent|null;
  @Input() winners: WinnerDTO[] = [];

  avgUserScore: number = 0;

  constructor(
    private reviewService: ReviewService
  ) {

  }

  ngOnInit(): void {
    if(this.event) {
        this.reviewService.getAvgScoreByUserEmail(this.event.creator.email).subscribe({
            next: (response) => {
                this.avgUserScore = response;
            },
            error: (error) => {
                console.error('Error al obtener promedio de reviews:', error);
                this.avgUserScore = 0; // por si falla, mostrar nada o 0
            }
        });
    } 
    // console.log("[infoEvent] => ganadores recibidos: ", this.winners);
  }

  ngOnChanges(changes: SimpleChanges): void {
    // console.log("[infoEvent] => cambios detectados: ", changes);
    // ordenamos los ganadores segun su posicion
    if (changes['winners']) {
      this.winners = this.winners.sort((a, b) => a.position - b.position);
    }
  }

}
