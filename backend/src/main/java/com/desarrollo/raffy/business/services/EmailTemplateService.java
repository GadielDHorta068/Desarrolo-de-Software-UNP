package com.desarrollo.raffy.business.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Servicio para generar plantillas de correo electrónico profesionales.
 * Proporciona plantillas HTML predefinidas con estilos consistentes y branding.
 */
@Service
public class EmailTemplateService {

    @Value("${app.from.email}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * Genera una plantilla de notificación profesional con logo y branding.
     * 
     * @param title         Título de la notificación
     * @param message       Mensaje principal
     * @param actionText    Texto del botón de acción (opcional)
     * @param actionUrl     URL del botón de acción (opcional)
     * @param footerMessage Mensaje del footer (opcional)
     * @return HTML de la plantilla
     */
    public String generateNotificationTemplate(String title,
            String message,
            String actionText,
            String actionUrl,
            String footerMessage) {
        String template = loadTemplate("notification.html");
        Map<String, String> variables = new HashMap<>();
        variables.put("title", escapeHtml(title));
        variables.put("message", "<p>" + escapeHtml(message) + "</p>");
        variables.put("footerMessage", footerMessage == null ? "" : escapeHtml(footerMessage));
        variables.put("logoDataUri", getLogoBase64());
        variables.put("actionSection", buildActionSection(actionText, actionUrl));
        return replaceVariables(template, variables);
    }

    /**
     * Genera una plantilla para confirmar un pago exitoso de múltiples
     * boletos/tickets.
     * Pensada para cuando un participante compra más de una rifa en una sola
     * operación.
     * No incluye método de pago ni números de comprobante.
     *
     * @param userName     Nombre del usuario
     * @param eventName    Nombre del evento
     * @param ticketsCount Cantidad de boletos/tickets adquiridos
     * @param eventUrl     URL para ver el evento o los detalles
     * @return HTML de la plantilla
     */
    public String generateMultiTicketPaymentConfirmationTemplate(String userName,
            String eventName,
            int ticketsCount,
            String eventUrl) {
        String title = "Pago confirmado";

        StringBuilder msg = new StringBuilder();
        msg.append("<p>")
                .append("Hola ").append(escapeHtml(userName)).append(", ")
                .append("tu pago fue procesado correctamente.")
                .append("</p>");

        msg.append("<p>")
                .append("Has adquirido <strong>").append(ticketsCount).append(" boletos</strong> para el evento ")
                .append("<strong>").append(escapeHtml(eventName)).append("</strong>.")
                .append("</p>");

        String actionText = "Ver detalles del evento";
        String footerMessage = "Gracias por confiar en Rafify.";

        return renderNotificationFromTemplate(title,
                msg.toString(),
                actionText,
                eventUrl,
                footerMessage);
    }

    /**
     * Genera una plantilla de bienvenida profesional para nuevos usuarios.
     * 
     * @param userName        Nombre del usuario
     * @param verificationUrl URL de verificación (opcional)
     * @return HTML de la plantilla
     */
    public String generateWelcomeTemplate(String userName, String verificationUrl) {
        String title = "¡Bienvenido a Rafify!";
        String message = "Hola " + userName + "," +
                "¡Gracias por unirte a Rafify! Estamos emocionados de tenerte en nuestra comunidad." +
                "Con Rafify podrás crear y participar en sorteos y rifas de manera fácil y segura.";

        String actionText = verificationUrl != null ? "Verificar mi cuenta" : null;
        String actionUrl = verificationUrl;
        String footerMessage = "Si tienes alguna pregunta, no dudes en contactarnos.";

        return generateNotificationTemplate(title, message, actionText, actionUrl, footerMessage);
    }

    /**
     * Genera una plantilla de verificación de email.
     * 
     * @param userName        Nombre del usuario
     * @param verificationUrl URL de verificación
     * @return HTML de la plantilla
     */
    public String generateEmailVerificationTemplate(String userName, String verificationUrl) {
        String title = "Verifica tu dirección de email";
        String message = "Hola " + userName + "," +
                "Para completar tu registro en Rafify, necesitamos verificar tu dirección de email." +
                "Haz clic en el botón de abajo para verificar tu cuenta. Este enlace expirará en 24 horas.";

        String actionText = "Verificar mi email";
        String actionUrl = verificationUrl;
        String footerMessage = "Si no creaste una cuenta en Rafify, puedes ignorar este correo.";

        return generateNotificationTemplate(title, message, actionText, actionUrl, footerMessage);
    }

    /**
     * Genera una plantilla de restablecimiento de contraseña.
     * 
     * @param userName Nombre del usuario
     * @param resetUrl URL de restablecimiento
     * @return HTML de la plantilla
     */
    public String generatePasswordResetTemplate(String userName, String resetUrl) {
        String title = "Restablecimiento de contraseña";
        String message = "Hola " + userName + "," +
                "Hemos recibido una solicitud para restablecer tu contraseña en Rafify." +
                "Si solicitaste este cambio, haz clic en el botón de abajo. Este enlace expirará en 1 hora.";

        String actionText = "Restablecer mi contraseña";
        String actionUrl = resetUrl;
        String footerMessage = "Si no solicitaste un restablecimiento de contraseña, puedes ignorar este correo.";

        return generateNotificationTemplate(title, message, actionText, actionUrl, footerMessage);
    }

    /**
     * Genera una plantilla de notificación de sorteo/rifa.
     * 
     * @param userName  Nombre del usuario
     * @param eventName Nombre del evento
     * @param eventType Tipo de evento (sorteo/rifa)
     * @param eventUrl  URL del evento
     * @param message   Mensaje personalizado
     * @return HTML de la plantilla
     */
    public String generateEventNotificationTemplate(String userName,
            String eventName,
            String eventType,
            String eventUrl,
            String message) {
        String title = "Nueva notificación de " + escapeHtml(eventType);
        StringBuilder msg = new StringBuilder();
        msg.append("<p>")
                .append("Hola ").append(escapeHtml(userName)).append(", ")
                .append(escapeHtml(message))
                .append("</p>");
        msg.append("<p><strong>Evento:</strong> ").append(escapeHtml(eventName)).append("</p>");
        msg.append("<p><strong>Tipo:</strong> ").append(escapeHtml(eventType)).append("</p>");

        String actionText = "Ver " + escapeHtml(eventType);
        String actionUrl = eventUrl;
        String footerMessage = "Mantente al día con las últimas novedades de tus eventos favoritos.";

        return renderNotificationFromTemplate(title, msg.toString(), actionText, actionUrl, footerMessage);
    }

    /**
     * Genera una plantilla de notificación para ganadores de sorteo/rifa.
     * Se envía cuando un participante ha ganado en un evento.
     * 
     * @param winnerName Nombre completo del ganador
     * @param position   Posición del ganador (1 = primero, 2 = segundo, etc.)
     * @param eventTitle Título del evento
     * @param eventType  Tipo de evento (GIVEAWAY o GUESSING_CONTEST)
     * @param eventUrl   URL para ver el evento
     * @param reviewUrl  URL para hacer la review del evento
     * @return HTML de la plantilla
     */
    public String generateWinnerNotificationTemplate(String winnerName,
            int position,
            String eventTitle,
            String eventType,
            String eventUrl,
            String reviewUrl,
            String creatorName,
            String creatorEmail,
            String creatorPhone) {
        // Determinar el texto según el tipo de evento
        String eventTypeText = eventType != null && eventType.equals("GUESSING_CONTEST") ? "sorteo" : "rifa";

        // Determinar el título según la posición
        String title;
        String positionText;
        if (position == 1) {
            title = "🎉 ¡Felicidades! ¡Has ganado!";
            positionText = "primer lugar";
        } else if (position == 2) {
            title = "🎉 ¡Felicidades! ¡Has ganado!";
            positionText = "segundo lugar";
        } else if (position == 3) {
            title = "🎉 ¡Felicidades! ¡Has ganado!";
            positionText = "tercer lugar";
        } else {
            title = "🎉 ¡Felicidades! ¡Has ganado!";
            positionText = position + "° lugar";
        }

        // Construir el mensaje principal como HTML, incluyendo contacto del creador
        StringBuilder msg = new StringBuilder();
        msg.append("<p>")
                .append("Hola ").append(escapeHtml(winnerName)).append(", ")
                .append("¡Tenemos excelentes noticias! Has resultado ganador en el ")
                .append(escapeHtml(eventTypeText)).append(".")
                .append("</p>");

        msg.append("<p>")
                .append("📌 <strong>Evento:</strong> ").append(escapeHtml(eventTitle)).append("<br/>")
                .append("🏆 <strong>Posición:</strong> ").append(escapeHtml(positionText))
                .append("</p>");

        // Bloque de información de contacto del creador del evento
        msg.append(
                "<div style=\"margin-top:16px;padding:12px;border:1px solid #e5e7eb;border-radius:8px;background:#fafafa;\">")
                .append("<p style=\"margin:0 0 8px 0;\"><strong>Contacto del creador del evento</strong></p>");
        if (creatorName != null && !creatorName.isBlank()) {
            msg.append("<p style=\"margin:0;\"><strong>Organizador:</strong> ")
                    .append(escapeHtml(creatorName))
                    .append("</p>");
        }
        if (creatorEmail != null && !creatorEmail.isBlank()) {
            msg.append("<p style=\"margin:0;\"><strong>Email:</strong> ")
                    .append(escapeHtml(creatorEmail))
                    .append("</p>");
        }
        if (creatorPhone != null && !creatorPhone.isBlank()) {
            msg.append("<p style=\"margin:0;\"><strong>Teléfono:</strong> ")
                    .append(escapeHtml(creatorPhone))
                    .append("</p>");
        }
        msg.append("</div>");
        // bloque con el link de la review
        msg.append("<p>")
            .append("Una vez que recibas tu premio, podés dejar una reseña para ayudar a otros usuarios.")
            .append("</p>");

        msg.append("<p>")
           .append("<a href=\"")
           .append(escapeHtml(reviewUrl))
           .append("\">")
           .append("⭐ Dejar una reseña")
           .append("</a>")
           .append("</p>");


        String actionText = "Ver detalles del " + eventTypeText;
        String actionUrl = eventUrl;
        String footerMessage = "¡Gracias por participar en Rafify! Esperamos verte pronto en más eventos.";

        return renderNotificationFromTemplate(title, msg.toString(), actionText, actionUrl, footerMessage);
    }

    /**
     * Genera una plantilla HTML con el resumen de ganadores para el creador del
     * evento,
     * incluyendo nombre, email, teléfono y posición.
     *
     * @param creatorName Nombre del creador
     * @param eventTitle  Título del evento
     * @param eventType   Tipo de evento (GIVEAWAY / GUESSING_CONTEST)
     * @param eventUrl    URL del evento en el frontend
     * @param winners     Lista de ganadores con datos de contacto
     * @return HTML renderizado listo para enviar
     */
    public String generateCreatorWinnersSummaryTemplate(String creatorName,
            String eventTitle,
            String eventType,
            String eventUrl,
            Collection<com.desarrollo.raffy.dto.WinnerDTO> winners) {
        String title = "Resumen de ganadores del evento";
        String eventTypeText = eventType != null && eventType.equals("GUESSING_CONTEST") ? "sorteo" : "rifa";

        StringBuilder msg = new StringBuilder();
        if (creatorName != null && !creatorName.isBlank()) {
            msg.append("<p>")
                    .append("Hola ").append(escapeHtml(creatorName)).append(", ")
                    .append("te compartimos el contacto de los ganadores de tu ")
                    .append(escapeHtml(eventTypeText)).append(".")
                    .append("</p>");
        }
        msg.append("<p>")
                .append("📌 <strong>Evento:</strong> ").append(escapeHtml(eventTitle)).append("<br/>")
                .append("🗂️ <strong>Tipo:</strong> ").append(escapeHtml(eventTypeText))
                .append("</p>");

        // Construir tabla de ganadores
        msg.append("<table style=\"width:100%;border-collapse:collapse;margin-top:12px;\">")
                .append("<thead>")
                .append("<tr style=\"background:#f3f4f6;\">")
                .append("<th style=\"text-align:left;padding:8px;border:1px solid #e5e7eb;\">Posición</th>")
                .append("<th style=\"text-align:left;padding:8px;border:1px solid #e5e7eb;\">Nombre</th>")
                .append("<th style=\"text-align:left;padding:8px;border:1px solid #e5e7eb;\">Email</th>")
                .append("<th style=\"text-align:left;padding:8px;border:1px solid #e5e7eb;\">Teléfono</th>")
                .append("</tr>")
                .append("</thead><tbody>");

        if (winners != null && !winners.isEmpty()) {
            for (com.desarrollo.raffy.dto.WinnerDTO w : winners) {
                String name = w.getName() != null ? w.getName() : "";
                String surname = w.getSurname() != null ? w.getSurname() : "";
                String fullName = (name + (surname.isEmpty() ? "" : (" " + surname))).trim();
                msg.append("<tr>")
                        .append("<td style=\"padding:8px;border:1px solid #e5e7eb;\">")
                        .append(escapeHtml(String.valueOf(w.getPosition())))
                        .append("</td>")
                        .append("<td style=\"padding:8px;border:1px solid #e5e7eb;\">")
                        .append(escapeHtml(fullName))
                        .append("</td>")
                        .append("<td style=\"padding:8px;border:1px solid #e5e7eb;\">")
                        .append(escapeHtml(w.getEmail()))
                        .append("</td>")
                        .append("<td style=\"padding:8px;border:1px solid #e5e7eb;\">")
                        .append(escapeHtml(w.getPhone()))
                        .append("</td>")
                        .append("</tr>");
            }
        } else {
            msg.append("<tr>")
                    .append("<td colspan=\"4\" style=\"padding:8px;border:1px solid #e5e7eb;text-align:center;color:#6b7280;\">")
                    .append("No se registraron ganadores.")
                    .append("</td>")
                    .append("</tr>");
        }
        msg.append("</tbody></table>");

        String actionText = "Ver detalles del evento";
        String footerMessage = "Puedes coordinar directamente con los ganadores usando sus datos de contacto.";
        return renderNotificationFromTemplate(title, msg.toString(), actionText, eventUrl, footerMessage);
    }

    /**
     * Genera una plantilla para confirmar la compra de números de rifa.
     * Incluye detalles del evento, precio por número y lista de números adquiridos.
     * 
     * @param buyerName        Nombre del comprador
     * @param eventTitle       Título de la rifa
     * @param priceOfNumber    Precio por número
     * @param purchasedNumbers Lista de números comprados
     * @param eventUrl         URL para ver el evento en el frontend
     * @return HTML renderizado listo para enviar
     */
    public String generateRaffleNumbersPurchasedTemplate(String buyerName,
            String eventTitle,
            double priceOfNumber,
            Collection<Integer> purchasedNumbers,
            String eventUrl) {
        String title = "Confirmación de compra de números de rifa";

        // Ordenar y formatear números
        List<Integer> numbers = purchasedNumbers == null ? Collections.emptyList() : new ArrayList<>(purchasedNumbers);
        Collections.sort(numbers);
        String numbersHtml;
        if (numbers.isEmpty()) {
            numbersHtml = "<p style=\"color:#6b7280;\">No se registraron números adquiridos.</p>";
        } else {
            String items = numbers.stream()
                    .map(n -> "<li style=\"margin-bottom:4px;\"><strong>" + escapeHtml(String.valueOf(n))
                            + "</strong></li>")
                    .collect(Collectors.joining(""));
            numbersHtml = "<ul style=\"list-style:disc;padding-left:20px;margin-top:8px;\">" + items + "</ul>";
        }

        StringBuilder msg = new StringBuilder();
        msg.append("<p>")
                .append("Hola ").append(escapeHtml(buyerName)).append(", ")
                .append("¡gracias por tu compra!")
                .append("</p>");

        msg.append("<p>")
                .append("Has adquirido números para la rifa <strong>")
                .append(escapeHtml(eventTitle))
                .append("</strong>.")
                .append("</p>");

        msg.append(
                "<div style=\"margin-top:12px;padding:12px;border:1px solid #e5e7eb;border-radius:8px;background:#fafafa;\">")
                .append("<p style=\"margin:0 0 8px 0;\"><strong>Detalle de la compra</strong></p>")
                .append("<p style=\"margin:0;\">Precio por número: <strong>$")
                .append(escapeHtml(String.format("%.2f", priceOfNumber)))
                .append("</strong></p>")
                .append("<p style=\"margin:8px 0 0 0;\">Números adquiridos:</p>")
                .append(numbersHtml)
                .append("</div>");

        String actionText = "Ver detalles de la rifa";
        String footerMessage = "Conserva este correo como comprobante de tu compra.";
        return renderNotificationFromTemplate(title, msg.toString(), actionText, eventUrl, footerMessage);
    }

    /**
     * Obtiene los recursos inline necesarios para las plantillas (logo, etc.).
     * 
     * @return Mapa de recursos inline
     */
    public Map<String, EmailService.InlineResource> getDefaultInlineResources() {
        Map<String, EmailService.InlineResource> resources = new HashMap<>();

        // Aquí podrías cargar el logo desde resources/assets o base64
        // Por ahora, creamos un logo simple en base64 (SVG)
        String logoBase64 = createSimpleLogoBase64();
        byte[] logoBytes = java.util.Base64.getDecoder().decode(logoBase64);

        resources.put("logo", new EmailService.InlineResource("logo", logoBytes, "image/svg+xml"));

        return resources;
    }

    /**
     * Obtiene el logo como data URI. Intenta cargar logo personalizado primero.
     */
    private String getLogoBase64() {
        try {
            String logoBase64 = loadCustomLogo();
            if (logoBase64 != null) {
                return "data:image/png;base64," + logoBase64;
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar logo personalizado: " + e.getMessage());
        }
        String logoBase64 = createSimpleLogoBase64();
        return "data:image/svg+xml;base64," + logoBase64;
    }

    /**
     * Carga el logo personalizado desde assets/images/logo.png
     */
    private String loadCustomLogo() throws IOException {
        Resource logoResource = new ClassPathResource("assets/images/logo.png");
        if (logoResource.exists()) {
            try (InputStream is = logoResource.getInputStream()) {
                byte[] logoBytes = StreamUtils.copyToByteArray(is);
                return java.util.Base64.getEncoder().encodeToString(logoBytes);
            }
        }
        return null;
    }

    /**
     * Crea un logo simple en base64 (SVG) para usar en los emails.
     * En producción, deberías cargar esto desde un archivo.
     */
    private String createSimpleLogoBase64() {
        String svg = "<svg width=\"150\" height=\"50\" xmlns=\"http://www.w3.org/2000/svg\">" +
                "<defs>" +
                "<linearGradient id=\"grad1\" x1=\"0%\" y1=\"0%\" x2=\"100%\" y2=\"0%\">" +
                "<stop offset=\"0%\" style=\"stop-color:#4F46E5;stop-opacity:1\" />" +
                "<stop offset=\"100%\" style=\"stop-color:#7C3AED;stop-opacity:1\" />" +
                "</linearGradient>" +
                "</defs>" +
                "<rect width=\"150\" height=\"50\" fill=\"url(#grad1)\" rx=\"10\"/>" +
                "<circle cx=\"30\" cy=\"25\" r=\"8\" fill=\"white\" opacity=\"0.9\"/>" +
                "<text x=\"50\" y=\"20\" fill=\"white\" font-family=\"Arial, sans-serif\" font-size=\"14\" font-weight=\"bold\">Rafify</text>"
                +
                "<text x=\"50\" y=\"35\" fill=\"white\" font-family=\"Arial, sans-serif\" font-size=\"10\" opacity=\"0.8\">Sorteos & Rifas</text>"
                +
                "</svg>";
        return java.util.Base64.getEncoder().encodeToString(svg.getBytes());
    }

    // Carga un archivo de plantilla desde classpath:
    // resources/templates/email/{templateName}
    private String loadTemplate(String templateName) {
        Resource resource = new ClassPathResource("templates/email/" + templateName);
        if (!resource.exists()) {
            throw new IllegalStateException("No se encontró la plantilla: " + templateName);
        }
        try (InputStream is = resource.getInputStream()) {
            return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer la plantilla: " + templateName, e);
        }
    }

    // Construye el bloque del botón de acción o vacío si faltan datos
    private String buildActionSection(String actionText, String actionUrl) {
        if (actionText == null || actionUrl == null)
            return "";
        if (actionText.trim().isEmpty() || actionUrl.trim().isEmpty())
            return "";
        return "<div class=\"action-section\">" +
                "<a href=\"" + escapeHtml(actionUrl) + "\" class=\"action-button\">" +
                escapeHtml(actionText) +
                "</a></div>";
    }

    // Renderiza la notificación inyectando messageHtml ya construido/escapado en la
    // plantilla
    private String renderNotificationFromTemplate(String title,
            String messageHtml,
            String actionText,
            String actionUrl,
            String footerMessage) {
        String template = loadTemplate("notification.html");
        Map<String, String> variables = new HashMap<>();
        variables.put("title", escapeHtml(title));
        variables.put("message", messageHtml == null ? "" : messageHtml);
        variables.put("footerMessage", footerMessage == null ? "" : escapeHtml(footerMessage));
        variables.put("logoDataUri", getLogoBase64());
        variables.put("actionSection", buildActionSection(actionText, actionUrl));
        return replaceVariables(template, variables);
    }

    // Reemplaza placeholders {{key}} por sus valores
    private String replaceVariables(String template, Map<String, String> variables) {
        String rendered = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String value = entry.getValue() == null ? "" : entry.getValue();
            rendered = rendered.replace("{{" + entry.getKey() + "}}", value);
        }
        return rendered;
    }

    /**
     * Escapa caracteres HTML para prevenir XSS.
     */
    private String escapeHtml(String text) {
        if (text == null)
            return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public String generateGuessProgressTemplate(String eventTitle, String userName, String numbersTriedUser,
            LocalDateTime attemptTime, int attemptCount, int attemptEvent, Long durationSeconds) {

        String title = "Confirmación de participación en el evento";

        List<Integer> numbers = new ArrayList<>();
        for (String numStr : numbersTriedUser.split(",")) {
            try {
                numbers.add(Integer.parseInt(numStr.trim()));
            } catch (NumberFormatException e) {
                // Ignorar números inválidos
            }
        }
        Collections.sort(numbers);
        String numbersHtml;
        if (numbers.isEmpty()) {
            numbersHtml = "<p style=\"color:#6b7280;\">No se registraron números intentados.</p>";
        } else {
            String items = numbers.stream()
                    .map(n -> "<li style=\"margin-bottom:4px;\"><strong>" + escapeHtml(String.valueOf(n))
                            + "</strong></li>")
                    .collect(Collectors.joining(""));
            numbersHtml = "<ul style=\"list-style:disc;padding-left:20px;margin-top:8px;\">" + items + "</ul>";
        }

        StringBuilder msg = new StringBuilder();
        msg.append("<p>")
                .append("Hola ").append(escapeHtml(userName)).append(", ")
                .append("¡gracias por participar en el evento!")
                .append("</p>");

        msg.append("<p>")
                .append("Informe de su participación en el evento <strong>")
                .append(escapeHtml(eventTitle))
                .append("</strong>")
                .append("</p>");

        msg.append(
                "<div style=\"margin-top:12px;padding:12px;border:1px solid #e5e7eb;border-radius:8px;background:#fafafa;\">")
                .append("<p style=\"margin:0 0 8px 0;\"><strong>Detalles del participante</strong></p>")
                .append("<p style=\"margin:0;\">Fecha y hora de participación: <strong>")
                .append(escapeHtml(
                        attemptTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))))
                .append("</strong></p>")
                .append("<p style=\"margin:0;\">Cantidad de intentos realizados: <strong>")
                .append(escapeHtml(String.valueOf(attemptCount)))
                .append("/")
                .append(escapeHtml(String.valueOf(attemptEvent)))
                .append("</strong></p>")
                .append("<p style=\"margin:8px 0 0 0;\">Números intentados:</p>")
                .append(numbersHtml)
                .append("<p style=\"margin:0;\">Duración total (segundos): <strong>")
                .append(escapeHtml(String.valueOf(durationSeconds)))
                .append("</strong></p>")
                .append("</div>");

        String actionText = "Ver detalles del evento";
        String footerMessage = "¡Gracias por participar en Rafiffy! Esperamos verte pronto en más eventos.";
        return renderNotificationFromTemplate(title, msg.toString(), actionText, frontendUrl, footerMessage);

    }
}