import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-invite-redirect',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="min-h-[40vh] flex items-center justify-center">
      <div class="text-center">
        <h2 class="text-2xl font-semibold mb-2">Redirigiendo al evento…</h2>
        <p class="text-gray-600">Si no ocurre automáticamente, <a [href]="apiRedirectUrl" class="text-blue-600 underline">haz clic aquí</a>.</p>
      </div>
    </section>
  `,
})
export class InviteRedirectComponent implements OnInit {
  apiRedirectUrl = '';

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    const shortcode = this.route.snapshot.paramMap.get('shortcode') || '';
    // Same-origin sin prefijo /api
    this.apiRedirectUrl = `/url/redirect/${shortcode}`;
    // Reemplaza la URL actual para evitar volver con back
    window.location.replace(this.apiRedirectUrl);
  }
}