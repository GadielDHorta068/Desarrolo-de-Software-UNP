import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

interface Service {
  name: string;
  description: string;
  status: 'operational' | 'degraded' | 'down';
  statusText: string;
  uptime: number;
}

interface Incident {
  title: string;
  description: string;
  date: string;
  resolved: boolean;
}

@Component({
  selector: 'app-status',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './status.component.html',
  styleUrls: ['./status.component.css']
})
export class StatusComponent implements OnInit {
  lastCheckTime = 'hace 2 minutos';

  services: Service[] = [
    {
      name: 'API REST',
      description: 'API principal de RAFFIFY',
      status: 'operational',
      statusText: 'Operativo',
      uptime: 99.98
    },
    {
      name: 'Base de Datos',
      description: 'PostgreSQL principal',
      status: 'operational',
      statusText: 'Operativo',
      uptime: 99.99
    },
    {
      name: 'Frontend',
      description: 'Aplicación web Angular',
      status: 'operational',
      statusText: 'Operativo',
      uptime: 99.97
    },
    {
      name: 'WhatsApp Business',
      description: 'Mensajería con usuarios',
      status: 'operational',
      statusText: 'Operativo',
      uptime: 99.94
    },
    {
      name: 'Email/Notificaciones',
      description: 'Servicio de correo electrónico',
      status: 'operational',
      statusText: 'Operativo',
      uptime: 99.92
    },
    {
      name: 'Almacenamiento',
      description: 'Archivos y recursos multimedia',
      status: 'operational',
      statusText: 'Operativo',
      uptime: 99.96
    }
  ];

  incidents: Incident[] = [
    // Placeholder: sin incidentes recientes
  ];

  ngOnInit(): void {
    window.scrollTo(0, 0);
  }
}
