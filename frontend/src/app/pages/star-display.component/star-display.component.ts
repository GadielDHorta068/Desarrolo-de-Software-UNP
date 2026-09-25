import { Component, Input } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';

@Component({
  selector: 'app-star-display',
   standalone: true,
  imports: [CommonModule, DecimalPipe],
  templateUrl: './star-display.component.html',
  styleUrl: './star-display.component.css'
})
export class StarDisplayComponent {
    @Input() rating!: number;
    stars = [1, 2, 3, 4, 5];

    getStarType(index: number): 'full' | 'half' | 'empty' {
        const starNumber = index + 1;
        if (this.rating >= starNumber) return  'full';
        if (this.rating >= starNumber - 0.5 ) return 'half';
        return 'empty';
    }

}
