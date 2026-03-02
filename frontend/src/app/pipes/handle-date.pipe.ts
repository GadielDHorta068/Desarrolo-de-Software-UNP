import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'handleDatePipe',
  standalone: true
})
export class HandleDatePipe implements PipeTransform {

  transform(value: string | Date | number[] | undefined, format: 'short' | 'long' = 'long'): string {
    if (!value) return 'Fecha desconocida';

    try {
      let date: Date;

      // Si es un array [year, month, day, hour, minute, second]
      if (Array.isArray(value)) {
        const [year, month, day, hour = 0, minute = 0, second = 0] = value;
        date = new Date(year, month - 1, day, hour, minute, second);
      } else {
        date = new Date(value);
      }

      if (isNaN(date.getTime())) {
        return 'Fecha inválida';
      }

      const options: Intl.DateTimeFormatOptions = format === 'short' 
        ? { year: 'numeric', month: '2-digit', day: '2-digit' }
        : { 
            year: 'numeric', 
            month: 'long', 
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            timeZone: 'America/Argentina/Buenos_Aires' // Ajusta tu zona horaria
          };

      return new Intl.DateTimeFormat('es-ES', options).format(date);
    } catch (error) {
      console.error('Error al formatear fecha:', error);
      return 'Fecha inválida';
    }
  }

}
