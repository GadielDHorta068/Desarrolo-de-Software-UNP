import { CommonModule, DecimalPipe } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-star-rating',
  standalone: true,
  imports: [CommonModule, DecimalPipe],
  templateUrl: './star-rating.component.html',
  styleUrl: './star-rating.component.css'
})
export class StarRatingComponent {
    @Input() rating: number = 3.5;
    stars = [1, 2, 3, 4, 5];

    getStarType(index: number): 'full' | 'half' | 'empty' {
        const starNumber = index + 1;
        if (this.rating >= starNumber) return  'full';
        if (this.rating >= starNumber - 0.5 ) return 'half';
        return 'empty';
    }
}
