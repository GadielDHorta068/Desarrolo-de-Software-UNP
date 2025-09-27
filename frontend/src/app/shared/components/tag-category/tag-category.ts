import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-tag-category',
  imports: [],
  templateUrl: './tag-category.html',
  styleUrl: './tag-category.css'
})
export class TagCategory {
  @Input() category: string|undefined = "";
}
