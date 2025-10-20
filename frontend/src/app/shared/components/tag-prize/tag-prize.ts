import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-tag-prize',
  imports: [CommonModule],
  templateUrl: './tag-prize.html',
  styleUrl: './tag-prize.css'
})
export class TagPrize {

  @Input() position!: number;
  labelPosition: string = "";

  // determina el color de fondo
  getClassColor(n: number|string): string {
      if(n == 1) return 'bg-green-100 text-green-800';
      if (n == 2) return 'bg-orange-100 text-orange-800';
      if (n == 3) return 'bg-yellow-100 text-yellow-800';
      return '';
  }

  getLabelGoal(position: number): string{
    // POR AHORA SOLO HASTA EL 3er premio
    if (position == 1) {
        return "1er PREMIO";
    }
    if (position == 2) {
        return "2do PREMIO";
    }
    if (position == 3) {
        return "3er PREMIO";
    }
    return "-";
  }
}
