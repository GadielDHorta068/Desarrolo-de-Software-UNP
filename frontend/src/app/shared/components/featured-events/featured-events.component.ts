import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { EventsService } from '../../../services/events.service';
import { AdminEventService } from '../../../services/admin/adminEvent.service';
import { EventsTemp, EventTypes, StatusEvent } from '../../../models/events.model';

@Component({
    selector: 'app-featured-events',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './featured-events.component.html',
    styleUrls: ['./featured-events.component.css']
})
export class FeaturedEventsComponent implements OnInit, OnDestroy {
    featuredEvents: EventsTemp[] = [];
    selectedType: EventTypes = EventTypes.GIVEAWAY;
    eventTypes: EventTypes[] = [EventTypes.GIVEAWAY, EventTypes.RAFFLES];
    loadingFeatured = false;
    currentIndex = 0;
    autoplayMs = 4000;
    autoplayTimer: any;

    constructor(
        private eventsService: EventsService,
        private adminEventService: AdminEventService,
        private router: Router,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.loadFeaturedEvents(this.selectedType);
    }

    ngOnDestroy(): void {
        this.stopAutoplay();
    }

    loadFeaturedEvents(type?: EventTypes): void {
        this.loadingFeatured = true;
        this.eventsService.getFeaturedEvents(type).subscribe({
            next: (events) => {
                this.featuredEvents = events || [];
                this.loadingFeatured = false;
                this.currentIndex = 0;
                this.cdr.markForCheck();
                this.restartAutoplay();
            },
            error: () => {
                this.featuredEvents = [];
                this.loadingFeatured = false;
                this.currentIndex = 0;
                this.cdr.markForCheck();
                this.stopAutoplay();
            }
        });
    }

    onChangeType(type: EventTypes): void {
        this.selectedType = type;
        this.loadFeaturedEvents(type);
        this.cdr.markForCheck();
    }

    setSlide(i: number): void {
        if (i < 0 || i >= this.featuredEvents.length) return;
        this.currentIndex = i;
        this.cdr.markForCheck();
    }

    restartAutoplay(): void {
        this.stopAutoplay();
        if (this.featuredEvents.length > 1) {
            this.autoplayTimer = setInterval(() => this.nextSlide(), this.autoplayMs);
        }
    }

    stopAutoplay(): void {
        if (this.autoplayTimer) {
            clearInterval(this.autoplayTimer);
            this.autoplayTimer = undefined;
        }
    }

    nextSlide(): void {
        if (this.featuredEvents.length === 0) return;
        this.currentIndex = (this.currentIndex + 1) % this.featuredEvents.length;
        this.cdr.markForCheck();
    }

    formatDate(date?: number[]): string {
        if (!date || date.length < 3) return '';
        const [y, m, d] = date;
        const mm = String(m).padStart(2, '0');
        const dd = String(d).padStart(2, '0');
        return `${dd}/${mm}/${y}`;
    }

    getCreatorDisplay(ev: EventsTemp): string {
        const c: any = ev.creator as any;
        if (!c) return '';
        return c.nickname || `${c.name || ''} ${c.surname || ''}`.trim();
    }

    statusClass(s: StatusEvent): string {
        if (s === StatusEvent.ACTIVE) return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200';
        if (s === StatusEvent.OPEN) return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200';
        if (s === StatusEvent.CLOSED) return 'bg-gray-200 text-gray-800 dark:bg-gray-700 dark:text-gray-200';
        if (s === StatusEvent.FINISHED) return 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200';
        if (s === StatusEvent.BLOCKED) return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200';
        return 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200';
    }

    getStatusLabel(s: StatusEvent): string {
        if (s === StatusEvent.ACTIVE) return 'Activo';
        if (s === StatusEvent.OPEN) return 'Abierto';
        if (s === StatusEvent.CLOSED) return 'Cerrado';
        if (s === StatusEvent.FINISHED) return 'Finalizado';
        if (s === StatusEvent.BLOCKED) return 'Bloqueado';
        return String(s);
    }

    onParticipate(ev: EventsTemp): void {
        if (!ev || !ev.id) return;
        this.adminEventService.setSelectedEvent(ev);
        this.router.navigate([`/event/management/${ev.id}`]);
    }
}
