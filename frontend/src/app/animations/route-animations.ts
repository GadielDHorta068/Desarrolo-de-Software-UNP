import {
  trigger,
  transition,
  style,
  query,
  group,
  animateChild,
  animate,
  keyframes,
} from '@angular/animations';

export const slideInAnimation = trigger('routeAnimations', [
  transition('* <=> *', [
    style({ position: 'relative' }),
    query(':enter, :leave', [
      style({
        position: 'absolute',
        top: 0,
        left: 0,
        width: '100%'
      })
    ], { optional: true }),
    query(':enter', [
      style({ left: '-100%' })
    ], { optional: true }),
    query(':leave', animateChild(), { optional: true }),
    group([
      query(':leave', [
        animate('300ms ease-out', style({ left: '100%', opacity: 0 }))
      ], { optional: true }),
      query(':enter', [
        animate('300ms ease-out', style({ left: '0%' }))
      ], { optional: true }),
    ]),
  ]),
  
  // Transición específica de login a register
  transition('login => register', [
    style({ position: 'relative' }),
    query(':enter, :leave', [
      style({
        position: 'absolute',
        top: 0,
        left: 0,
        width: '100%'
      })
    ], { optional: true }),
    query(':enter', [
      style({ 
        transform: 'translateX(100%) scale(0.8)',
        opacity: 0
      })
    ], { optional: true }),
    query(':leave', animateChild(), { optional: true }),
    group([
      query(':leave', [
        animate('400ms cubic-bezier(0.25, 0.8, 0.25, 1)', 
          style({ 
            transform: 'translateX(-100%) scale(0.8)',
            opacity: 0
          })
        )
      ], { optional: true }),
      query(':enter', [
        animate('400ms cubic-bezier(0.25, 0.8, 0.25, 1)', 
          style({ 
            transform: 'translateX(0%) scale(1)',
            opacity: 1
          })
        )
      ], { optional: true }),
    ]),
  ]),
  
  // Transición específica de register a login
  transition('register => login', [
    style({ position: 'relative' }),
    query(':enter, :leave', [
      style({
        position: 'absolute',
        top: 0,
        left: 0,
        width: '100%'
      })
    ], { optional: true }),
    query(':enter', [
      style({ 
        transform: 'translateX(-100%) scale(0.8)',
        opacity: 0
      })
    ], { optional: true }),
    query(':leave', animateChild(), { optional: true }),
    group([
      query(':leave', [
        animate('400ms cubic-bezier(0.25, 0.8, 0.25, 1)', 
          style({ 
            transform: 'translateX(100%) scale(0.8)',
            opacity: 0
          })
        )
      ], { optional: true }),
      query(':enter', [
        animate('400ms cubic-bezier(0.25, 0.8, 0.25, 1)', 
          style({ 
            transform: 'translateX(0%) scale(1)',
            opacity: 1
          })
        )
      ], { optional: true }),
    ]),
  ]),
]);

// Animación para elementos que aparecen
export const fadeInUp = trigger('fadeInUp', [
  transition(':enter', [
    style({ 
      opacity: 0, 
      transform: 'translateY(30px)' 
    }),
    animate('600ms cubic-bezier(0.25, 0.8, 0.25, 1)', 
      style({ 
        opacity: 1, 
        transform: 'translateY(0)' 
      })
    )
  ])
]);

// Animación para botones hover
export const buttonHover = trigger('buttonHover', [
  transition(':enter', [
    style({ transform: 'scale(1)' }),
    animate('150ms ease-in-out', style({ transform: 'scale(1.05)' }))
  ]),
  transition(':leave', [
    animate('150ms ease-in-out', style({ transform: 'scale(1)' }))
  ])
]);

// Animación para inputs focus
export const inputFocus = trigger('inputFocus', [
  transition('* => focused', [
    animate('200ms ease-out', 
      keyframes([
        style({ transform: 'scale(1)', offset: 0 }),
        style({ transform: 'scale(1.02)', offset: 0.5 }),
        style({ transform: 'scale(1)', offset: 1 })
      ])
    )
  ])
]);