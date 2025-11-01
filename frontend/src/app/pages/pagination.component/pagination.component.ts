import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [],
  templateUrl: './pagination.component.html',
  styleUrl: './pagination.component.css'
})
export class PaginationComponent {
    @Input() currentPage!: number;
    @Input() totalPages!: number;
    @Output() pageChange = new EventEmitter<number>();

    

    nextPage(): void {
        if (this.currentPage < this.totalPages) {
         this.pageChange.emit(this.currentPage + 1);
        }
    }

    previousPage(): void {
        if (this.currentPage > 1) {
            this.pageChange.emit(this.currentPage - 1);
        }
    }

    goToPage (aPageNumber: number): void {
        if (aPageNumber >= 1 && aPageNumber <= this.totalPages) {
            this.pageChange.emit(aPageNumber);
        }   
    }
}
