import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

interface Integration {
  name: string;
  description: string;
  icon: string;
  color: string;
  category: string;
  status: 'active' | 'soon';
}

@Component({
  selector: 'app-integrations',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './integrations.component.html',
  styleUrls: ['./integrations.component.css']
})
export class IntegrationsComponent implements OnInit {
  integrations: Integration[] = [
    {
      name: 'Stripe',
      description: 'Acepta pagos con tarjeta de crédito y débito',
      icon: '💳',
      color: '#635BFF',
      category: 'Pagos',
      status: 'active'
    },
    {
      name: 'PayPal',
      description: 'Procesa pagos a través de PayPal',
      icon: '💰',
      color: '#0070BA',
      category: 'Pagos',
      status: 'active'
    },
    {
      name: 'Instagram',
      description: 'Sorteos basados en comentarios y likes',
      icon: '📸',
      color: '#E1306C',
      category: 'Redes Sociales',
      status: 'active'
    },
    {
      name: 'Facebook',
      description: 'Sorteos en páginas y grupos de Facebook',
      icon: '📘',
      color: '#1877F2',
      category: 'Redes Sociales',
      status: 'soon'
    },
    {
      name: 'Twitter / X',
      description: 'Sorteos basados en retweets y menciones',
      icon: '🐦',
      color: '#1DA1F2',
      category: 'Redes Sociales',
      status: 'soon'
    },
    {
      name: 'WhatsApp Business',
      description: 'Notificaciones y comunicación con participantes',
      icon: '💬',
      color: '#25D366',
      category: 'Comunicación',
      status: 'soon'
    },
    {
      name: 'Mailchimp',
      description: 'Sincroniza participantes con listas de email',
      icon: '📧',
      color: '#FFE01B',
      category: 'Email Marketing',
      status: 'soon'
    },
    {
      name: 'Google Analytics',
      description: 'Rastrea conversiones y comportamiento de usuarios',
      icon: '📊',
      color: '#E37400',
      category: 'Analytics',
      status: 'soon'
    },
    {
      name: 'Zapier',
      description: 'Conecta con miles de aplicaciones',
      icon: '⚡',
      color: '#FF4A00',
      category: 'Automatización',
      status: 'soon'
    }
  ];

  ngOnInit(): void {
    window.scrollTo(0, 0);
  }
}