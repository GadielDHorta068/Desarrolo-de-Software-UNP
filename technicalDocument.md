# Documento Técnico Raffify

## Introducción

El presente documento describe el proyecto de una plataforma web que permite a personas, emprendedores y organizaciones crear y gestionar sorteos digitales de forma centralizada, segura y transparente. La solución abarca distintos formatos, desde rifas digitales hasta sorteos automáticos en redes sociales como Instagram, con herramientas de pago integradas y opciones para personalizar la mecánica del sorteo. </br>

A diferencia de otras plataformas que se centran exclusivamente en sorteos en redes sociales o dinámicas de azar simples, nuestro foco principal está en digitalizar el formato rifa, la modalidad más utilizada por clubes, asociaciones y organizaciones comunitarias. Esto permite modernizar un proceso tradicionalmente manual, asegurando numeración correcta de tickets, gestión de pagos, registro seguro de participantes y transparencia en los resultados.</br>

Cada organizador contará con un perfil público en el que los ganadores podrán dejar constancia de haber recibido su premio, fortaleciendo la reputación y confianza de quienes realizan los sorteos. Como refuerzo adicional, se incorpora tecnología blockchain que permite verificar públicamente cada resultado y asegura la máxima confiabilidad del sistema.</br>

Con este enfoque, la plataforma no solo digitaliza los procesos tradicionales de sorteos y rifas, sino que crea un ecosistema confiable, escalable y especialmente pensado para quienes realizan rifas regularmente, aportando transparencia y eficiencia a toda la comunidad de participantes y organizadores.

## Descripción del problema

Hoy en día, la mayoría de los sorteos y rifas se organizan de manera artesanal: con números en papel, listas escritas a mano o capturas desordenadas en redes sociales. Esto genera varios inconvenientes:</br>
+ **Gestión Ineficiente**: los organizadores deben registrar a mano los pagos, controlar los números disponibles y notificar a los ganadores.


+ **Limitaciones en el alcance**: las rifas físicas dependen de la venta presencial, reduciendo el público potencial.


+ **Falta de transparencia**: los participantes no siempre confían en que el proceso sea justo.


+ **Ausencia de integración digital**: métodos de pago, registros automáticos y notificaciones suelen estar dispersos o directamente ausentes.

En el caso de los sorteos en redes sociales, el problema se amplifica: falta de validación de requisitos (ej: que realmente sigan a una cuenta), riesgo de fraude y dificultad para manejar gran cantidad de participantes.

## Propósito

El propósito del proyecto es transformar la manera en que se realizan sorteos y rifas, ofreciendo un espacio digital que elimine las barreras de la desconfianza y la complejidad. Buscamos que cualquier persona u organización pueda organizar sorteos de forma sencilla, transparente y segura, logrando que los participantes confíen plenamente en el proceso y los resultados.

# Arquitectura del sistema

### Estructura del Backend
```
src/
├── app/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com/
│   │   │   │   ├── desarrollo/
│   │   │   │   │   ├── raffy/
│   │   │   │   │   │   ├── business/
│   │   │   │   │   │   │   │   ├── repository/
│   │   │   │   │   │   │   │   ├── service/
│   │   │   │   │   │   │   │   ├── utils/
│   │   │   │   │   │   ├── config/
│   │   │   │   │   │   ├── dto/
│   │   │   │   │   │   │   ├── evolution/
│   │   │   │   │   │   │   ├── twofa/
│   │   │   │   │   │   ├── exception/
│   │   │   │   │   │   ├── model/
│   │   │   │   │   │   │   ├── auditlog/
│   │   │   │   │   │   ├── presenter/
│   │   │   │   │   │   ├── util/
│   │   │   │   │   │   ├──RaffyAplication.java
│   │   │   │   │   │   ├──Response.java
│   │   │   │   │   │   ├──resources/
```
### Estructura del Frontend
```
src/
├── app/
│   │   ├── animations/
│   │   ├── assets/
│   │   ├── guards/
│   │   ├── interceptors/
│   │   ├── models/
│   │   ├── pages/
│   │   │   ├── api/
│   │   │   ├── audit/
│   │   │   │   ├── audit-detail/
│   │   │   │   ├── audit-list/
│   │   │   ├── chat/
│   │   │   ├── contact/
│   │   │   ├── docs/
│   │   │   ├── edit-event/
│   │   │   ├── event/
│   │   │   ├── features/
│   │   │   ├── guessprogress/
│   │   │   ├── help/
│   │   │   ├── home/
│   │   │   ├── integrations/
│   │   │   ├── make-review.component/
│   │   │   ├── management-event/
│   │   │   ├── pagination.component/
│   │   │   ├── panel-list/
│   │   │   ├── payments/
│   │   │   ├── pricing/
│   │   │   ├── profile/
│   │   │   ├── public-events/
│   │   │   ├── questionary/
│   │   │   ├── raffle-numbers.component/
│   │   │   ├── raffles-panel/
│   │   │   ├── register/
│   │   │   ├── reported-events/
│   │   │   ├── reports/
│   │   │   ├── reviews/
│   │   │   ├── settings/
│   │   │   ├── star-rating.component/
│   │   │   ├── status/
│   │   │   ├── winners/
│   │   ├── pipes/
│   │   ├── services/
│   │   │   ├── admin/
│   │   │   ├── utils/
│   │   ├── shared/
│   │   │   ├── components/
│   │   │   ├── event-share-card/
│   │   ├── app.component.css
│   │   ├── app.component.html
│   │   ├── app.component.ts
│   │   ├── app.config.server.ts
│   │   ├── app.config.ts
│   │   ├── app.routes.ts
```
### Frameworks y librerías
+ **Frontend**: Angular v20, Tailwind CSS.
+ **Backend**: Java 20+, Spring Boot 3.3.3
+ **Base de Datos**: PostgreSQL
+ **Infraestructura**: Docker

# Modelos entidad-relación

# Diagrama de clases

# Lógica de negocio

# Endpoints (API Rest)

# Seguridad

# Pruebas

# Despligue

1. Requisitos previos
    + **JDK** v21.0.8
    + **Java** v21.0.8
    + **Maven** v3.8.7
    + **Node.js** v18.19.0
    + **npm** v9.2.0
    + **Angular CLI** (npm install -g @angular/cli)
    + **Docker** y **Docker Compose**
    + **PostgreSQL** v16
    + **Git**
    + **Postman**
2. Configuración del entorno
    + Clonar el repositorio
    + Entrar a la carpeta Desarrolo-de-Software-UNP
    + Entrar al backend y ejecutar ```mvn clean install```
    + Entrar al frontend y ejecutar ```npm install``` 

3. Despliegue
    + Levantar servicios:
        + ```docker compose up --build``` o ```docker-compose up --build```
