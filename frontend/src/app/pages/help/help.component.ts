import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

interface QuickGuide {
  title: string;
  description: string;
  iconPath: string;
  iconBg: string;
  steps: string[];
}

interface FAQ {
  question: string;
  answer: string;
  category: string;
  expanded?: boolean;
}

interface FAQCategory {
  id: string;
  name: string;
}

interface HelpCategory {
  title: string;
  description: string;
  iconPath: string;
  bgGradient: string;
}

@Component({
  selector: 'app-help',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './help.component.html',
  styleUrls: ['./help.component.css']
})
export class HelpComponent implements OnInit {
  selectedCategory = 'general';

  quickGuides: QuickGuide[] = [
    {
      title: '¿Cómo crear mi primer sorteo?',
      description: 'Sigue estos pasos para crear tu primer sorteo en RAFFIFY',
      iconPath: 'M12 4v16m8-8H4',
      iconBg: 'bg-gradient-to-r from-primary to-secondary',
      steps: [
        'Inicia sesión o crea tu cuenta',
        'Haz clic en "Crear Sorteo Gratis"',
        'Completa los detalles del evento',
        'Configura premios y participación',
        'Publica y comparte tu sorteo'
      ]
    },
    {
      title: '¿Cómo invitar participantes?',
      description: 'Aprende a compartir tu sorteo y aumentar la participación',
      iconPath: 'M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z',
      iconBg: 'bg-gradient-to-r from-secondary to-accent',
      steps: [
        'Accede a tu evento creado',
        'Copia el link corto o código QR',
        'Comparte en redes sociales',
        'Envía invitaciones via email o chat',
        'Monitorea los participantes en tiempo real'
      ]
    },
    {
      title: '¿Cómo auditar un evento?',
      description: 'Verifica la transparencia y fairness de cualquier sorteo',
      iconPath: 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z',
      iconBg: 'bg-gradient-to-r from-accent to-primary',
      steps: [
        'Navega a la sección de Auditorías',
        'Busca el evento por código',
        'Revisa el registro blockchain',
        'Verifica participantes y resultados',
        'Descarga el reporte de auditoría'
      ]
    },
    {
      title: 'Configurar pagos online',
      description: 'Acepta pagos automáticamente en tus rifas digitales',
      iconPath: 'M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z',
      iconBg: 'bg-gradient-to-r from-primary to-secondary',
      steps: [
        'Configura tu método de pago',
        'Define precio de cada ticket',
        'Activa pagos online en tu evento',
        'Los participantes pagan automáticamente',
        'Recibe fondos con solo 3% de comisión'
      ]
    },
    {
      title: 'Integrar con redes sociales',
      description: 'Conecta Instagram y otras plataformas para sorteos',
      iconPath: 'M17.657 18.657A8 8 0 016.343 7.343S7 9 9 10c0-2 .5-5 2.986-7C14 5 16.09 5.777 17.656 7.343A7.975 7.975 0 0120 13a7.975 7.975 0 01-2.343 5.657z',
      iconBg: 'bg-gradient-to-r from-secondary to-accent',
      steps: [
        'Ve a Configuración de evento',
        'Selecciona "Sorteo en Redes Sociales"',
        'Define requisitos (seguir, comentar, etc.)',
        'Conecta tu cuenta de Instagram',
        'RAFFIFY valida requisitos automáticamente'
      ]
    },
    {
      title: 'Ver resultados y ganadores',
      description: 'Ejecuta el sorteo y anuncia los ganadores',
      iconPath: 'M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z',
      iconBg: 'bg-gradient-to-r from-accent to-primary',
      steps: [
        'Espera la fecha de sorteo configurada',
        'Haz clic en "Ejecutar Sorteo"',
        'La ruleta selecciona ganadores aleatoriamente',
        'Los ganadores reciben notificación',
        'Comparte los resultados públicamente'
      ]
    }
  ];

  faqCategories: FAQCategory[] = [
    { id: 'general', name: 'General' },
    { id: 'sorteos', name: 'Sorteos' },
    { id: 'pagos', name: 'Pagos' },
    { id: 'seguridad', name: 'Seguridad' },
    { id: 'tecnico', name: 'Técnico' }
  ];

  faqs: FAQ[] = [
    // General
    { question: '¿Qué es RAFFIFY?', answer: 'RAFFIFY es una plataforma digital moderna que te permite crear, gestionar y ejecutar sorteos y rifas de manera transparente, segura y fácil. Utilizamos tecnología blockchain para garantizar la transparencia total de cada sorteo.', category: 'general' },
    { question: '¿Es gratis usar RAFFIFY?', answer: 'Sí, crear sorteos gratuitos es completamente gratis. Para rifas con pagos online, cobramos una pequeña comisión del 3% sobre la recaudación total.', category: 'general' },
    { question: '¿Necesito conocimientos técnicos?', answer: 'No, RAFFIFY está diseñado para ser intuitivo y fácil de usar. Cualquier persona puede crear un sorteo en menos de 5 minutos sin necesidad de conocimientos técnicos.', category: 'general' },
    { question: '¿Puedo crear múltiples sorteos?', answer: 'Sí, puedes crear tantos sorteos como necesites. No hay límite en la cantidad de eventos que puedes gestionar con tu cuenta.', category: 'general' },

    // Sorteos
    { question: '¿Cómo funciona la numeración de tickets?', answer: 'La numeración de tickets es completamente automática. Cuando un participante se registra, se le asigna un número único automáticamente. Puedes configurar el rango de números disponibles.', category: 'sorteos' },
    { question: '¿Puedo cancelar un sorteo después de crearlo?', answer: 'Sí, puedes cancelar un sorteo antes de su fecha de ejecución. Si hay participantes con pagos realizados, se procesarán los reembolsos automáticamente.', category: 'sorteos' },
    { question: '¿Cómo se seleccionan los ganadores?', answer: 'Los ganadores se seleccionan mediante un algoritmo aleatorio verificable por blockchain. El proceso es completamente transparente y puede ser auditado por cualquier persona.', category: 'sorteos' },
    { question: '¿Puedo tener múltiples ganadores?', answer: 'Sí, al crear tu sorteo puedes configurar la cantidad de ganadores y asignar diferentes premios a cada uno.', category: 'sorteos' },

    // Pagos
    { question: '¿Qué métodos de pago aceptan?', answer: 'Aceptamos tarjetas de crédito, débito, y las principales billeteras digitales. Los pagos son procesados de forma segura a través de nuestros proveedores certificados.', category: 'pagos' },
    { question: '¿Cuándo recibo mi dinero?', answer: 'Los fondos de tu rifa se transfieren a tu cuenta bancaria una vez finalizado el sorteo, después de descontar la comisión del 3%. El proceso puede tomar entre 3 a 5 días hábiles.', category: 'pagos' },
    { question: '¿Hay costos ocultos?', answer: 'No, solo cobramos el 3% de comisión sobre la recaudación en rifas pagas. No hay costos de configuración, mantenimiento ni tarifas ocultas.', category: 'pagos' },
    { question: '¿Qué pasa si hay reembolsos?', answer: 'Si necesitas cancelar el sorteo y reembolsar a los participantes, procesamos los reembolsos automáticamente. No cobramos comisión sobre los montos reembolsados.', category: 'pagos' },

    // Seguridad
    { question: '¿Cómo garantizan la transparencia?', answer: 'Utilizamos tecnología blockchain para registrar cada acción del sorteo. Esto crea un registro inmutable y verificable públicamente, garantizando total transparencia.', category: 'seguridad' },
    { question: '¿Mis datos están seguros?', answer: 'Sí, implementamos las mejores prácticas de seguridad incluyendo encriptación de datos, autenticación en dos pasos, y cumplimiento con regulaciones de protección de datos.', category: 'seguridad' },
    { question: '¿Puedo confiar en los resultados?', answer: 'Absolutamente. El algoritmo de selección de ganadores es verificable por blockchain y puede ser auditado por cualquier persona. Es imposible manipular los resultados.', category: 'seguridad' },
    { question: '¿Qué es la autenticación en dos pasos?', answer: 'Es una capa adicional de seguridad que requiere no solo tu contraseña, sino también un código temporal enviado a tu teléfono o email para acceder a tu cuenta.', category: 'seguridad' },

    // Técnico
    { question: '¿Funciona en dispositivos móviles?', answer: 'Sí, RAFFIFY está completamente optimizado para móviles. Puedes crear, gestionar y participar en sorteos desde cualquier dispositivo.', category: 'tecnico' },
    { question: '¿Ofrecen API para desarrolladores?', answer: 'Sí, ofrecemos una API REST completa para integraciones personalizadas. Visita nuestra sección de Documentación para más detalles.', category: 'tecnico' },
    { question: '¿Puedo integrar RAFFIFY con mi sitio web?', answer: 'Sí, puedes embeber sorteos en tu sitio web usando nuestros widgets o nuestra API. Consulta la documentación técnica para más información.', category: 'tecnico' },
    { question: '¿Qué navegadores son compatibles?', answer: 'RAFFIFY funciona en todos los navegadores modernos: Chrome, Firefox, Safari, Edge y Opera. Recomendamos mantener tu navegador actualizado para la mejor experiencia.', category: 'tecnico' }
  ];

  helpCategories: HelpCategory[] = [
    {
      title: 'Primeros Pasos',
      description: 'Aprende lo básico para comenzar con RAFFIFY',
      iconPath: 'M13 10V3L4 14h7v7l9-11h-7z',
      bgGradient: 'bg-gradient-to-r from-primary to-secondary'
    },
    {
      title: 'Gestión de Sorteos',
      description: 'Crea, edita y administra tus eventos',
      iconPath: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01',
      bgGradient: 'bg-gradient-to-r from-secondary to-accent'
    },
    {
      title: 'Pagos y Comisiones',
      description: 'Configura pagos y gestiona tu recaudación',
      iconPath: 'M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z',
      bgGradient: 'bg-gradient-to-r from-accent to-primary'
    },
    {
      title: 'Seguridad y Auditoría',
      description: 'Verifica transparencia y protege tu cuenta',
      iconPath: 'M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z',
      bgGradient: 'bg-gradient-to-r from-primary to-secondary'
    },
    {
      title: 'Integraciones',
      description: 'Conecta con redes sociales y servicios externos',
      iconPath: 'M11 4a2 2 0 114 0v1a1 1 0 001 1h3a1 1 0 011 1v3a1 1 0 01-1 1h-1a2 2 0 100 4h1a1 1 0 011 1v3a1 1 0 01-1 1h-3a1 1 0 01-1-1v-1a2 2 0 10-4 0v1a1 1 0 01-1 1H7a1 1 0 01-1-1v-3a1 1 0 00-1-1H4a2 2 0 110-4h1a1 1 0 001-1V7a1 1 0 011-1h3a1 1 0 001-1V4z',
      bgGradient: 'bg-gradient-to-r from-secondary to-accent'
    },
    {
      title: 'Soporte Técnico',
      description: 'Soluciona problemas y obtén ayuda técnica',
      iconPath: 'M18.364 5.636l-3.536 3.536m0 5.656l3.536 3.536M9.172 9.172L5.636 5.636m3.536 9.192l-3.536 3.536M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-5 0a4 4 0 11-8 0 4 4 0 018 0z',
      bgGradient: 'bg-gradient-to-r from-accent to-primary'
    }
  ];

  ngOnInit(): void {
    // Scroll to top when component loads
    window.scrollTo(0, 0);

    // Initialize all FAQs as collapsed
    this.faqs.forEach(faq => faq.expanded = false);
  }

  get filteredFaqs(): FAQ[] {
    return this.faqs.filter(faq => faq.category === this.selectedCategory);
  }

  selectCategory(categoryId: string): void {
    this.selectedCategory = categoryId;
    // Collapse all FAQs when changing category
    this.faqs.forEach(faq => faq.expanded = false);
  }

  toggleFaq(index: number): void {
    const faq = this.filteredFaqs[index];
    if (faq) {
      faq.expanded = !faq.expanded;
    }
  }
}