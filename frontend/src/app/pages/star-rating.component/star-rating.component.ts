import { CommonModule, DecimalPipe } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-star-rating',
  standalone: true,
  imports: [CommonModule, DecimalPipe],
  templateUrl: './star-rating.component.html',
  styleUrl: './star-rating.component.css'
})
export class StarRatingComponent {
    @Input() rating = 0;
    @Input() readonly = true;

    @Output() ratingChange = new EventEmitter<number>();

    stars = [1, 2, 3, 4, 5];
    hoverRating = 0;

    get currentRating(): number {
        return this.hoverRating || this.rating;
    }
    
    setRating(value: number) {
        if (this.readonly) return;

        this.rating = value;
        this.ratingChange.emit(value);
    }

    // getStarType(index: number): 'full' | 'half' | 'empty' {
    //     const starNumber = index + 1;
    //     const currentRating = this.hoverRating || this.rating;

    //     console.log({
    //         hover: this.hoverRating,
    //         rating: this.rating,
    //         current: currentRating,
    //         star: starNumber
    //     });

    //     if (currentRating >= starNumber) return  'full';
    //     if (currentRating >= starNumber - 0.5 ) return 'half';
      
    //     return 'empty';
    // }

    setHover(value: number): void {
        if (this.readonly) return;

        this.hoverRating = value;
        // console.log("hover:", this.hoverRating);
    }

    clearHover(): void {
        if (this.readonly) return;

        this.hoverRating = 0;
    }

}
