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
    // style({ position: 'relative' }), // Removed to let CSS Grid handle layout
    query(':enter, :leave', [
      style({
        width: '100%'
      })
    ], { optional: true }),
    query(':enter', [
      style({ transform: 'translateX(-100%)' })
    ], { optional: true }),
    query(':leave', animateChild(), { optional: true }),
    group([
      query(':leave', [
        animate('200ms ease-out', style({ transform: 'translateX(100%)', opacity: 0 }))
      ], { optional: true }),
      query(':enter', [
        animate('200ms ease-out', style({ transform: 'translateX(0%)' }))
      ], { optional: true }),
    ]),
  ]),
  transition('home => eventManagement', [
    // style({ position: 'relative' }),
    query(':enter, :leave', [
      style({ width: '100%' })
    ], { optional: true }),
    query(':enter', [
      style({ opacity: 0, transform: 'translateY(-8px) scale(0.98)' })
    ], { optional: true }),
    group([
      query(':leave', [
        animate('240ms cubic-bezier(0.4,0,0.2,1)', style({ opacity: 0, transform: 'translateY(8px) scale(0.98)' }))
      ], { optional: true }),
      query(':enter', [
        animate('240ms cubic-bezier(0.4,0,0.2,1)', style({ opacity: 1, transform: 'translateY(0) scale(1)' }))
      ], { optional: true }),
    ])
  ]),
  transition('eventManagement => home', [
    // style({ position: 'relative' }),
    query(':enter, :leave', [
      style({ width: '100%' })
    ], { optional: true }),
    query(':enter', [
      style({ opacity: 0, transform: 'translateY(8px) scale(0.98)' })
    ], { optional: true }),
    group([
      query(':leave', [
        animate('240ms cubic-bezier(0.4,0,0.2,1)', style({ opacity: 0, transform: 'translateY(-8px) scale(0.98)' }))
      ], { optional: true }),
      query(':enter', [
        animate('240ms cubic-bezier(0.4,0,0.2,1)', style({ opacity: 1, transform: 'translateY(0) scale(1)' }))
      ], { optional: true }),
    ])
  ]),

  // Transición específica de login a register
  transition('login => register', [
    // style({ position: 'relative' }),
    query(':enter, :leave', [
      style({
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
        animate('250ms ease-out',
          style({
            transform: 'translateX(-100%) scale(0.8)',
            opacity: 0
          })
        )
      ], { optional: true }),
      query(':enter', [
        animate('250ms ease-out',
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
    // style({ position: 'relative' }),
    query(':enter, :leave', [
      style({
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
        animate('250ms ease-out',
          style({
            transform: 'translateX(100%) scale(0.8)',
            opacity: 0
          })
        )
      ], { optional: true }),
      query(':enter', [
        animate('250ms ease-out',
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
      transform: 'translateY(20px)'
    }),
    animate('200ms ease-out',
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