import { CommonModule } from '@angular/common'
import { Component, Input } from '@angular/core'

@Component({
  selector: 'app-loading-indicator',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './loading-indicator.html',
  styleUrl: './loading-indicator.css'
})
export class LoadingIndicator {
  @Input() active: boolean = false
  @Input() message: string = ''
  @Input() type: 'inline' | 'overlay' = 'inline'
  @Input() size: 'sm' | 'md' | 'lg' = 'md'

  get spinnerSize(): string {
    if (this.size === 'sm') return 'h-4 w-4'
    if (this.size === 'lg') return 'h-8 w-8'
    return 'h-6 w-6'
  }
}