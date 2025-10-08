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
     * @param title Título de la notificación
     * @param message Mensaje principal
     * @param actionText Texto del botón de acción (opcional)
     * @param actionUrl URL del botón de acción (opcional)
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
     * Genera una plantilla de bienvenida profesional para nuevos usuarios.
     * 
     * @param userName Nombre del usuario
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
     * @param userName Nombre del usuario
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
     * @param userName Nombre del usuario
     * @param eventName Nombre del evento
     * @param eventType Tipo de evento (sorteo/rifa)
     * @param eventUrl URL del evento
     * @param message Mensaje personalizado
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
                    "<text x=\"50\" y=\"20\" fill=\"white\" font-family=\"Arial, sans-serif\" font-size=\"14\" font-weight=\"bold\">Rafify</text>" +
                    "<text x=\"50\" y=\"35\" fill=\"white\" font-family=\"Arial, sans-serif\" font-size=\"10\" opacity=\"0.8\">Sorteos & Rifas</text>" +
                    "</svg>";
        return java.util.Base64.getEncoder().encodeToString(svg.getBytes());
    }



    // Carga un archivo de plantilla desde classpath: resources/templates/email/{templateName}
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
        if (actionText == null || actionUrl == null) return "";
        if (actionText.trim().isEmpty() || actionUrl.trim().isEmpty()) return "";
        return "<div class=\"action-section\">" +
               "<a href=\"" + escapeHtml(actionUrl) + "\" class=\"action-button\">" +
               escapeHtml(actionText) +
               "</a></div>";
    }

    // Renderiza la notificación inyectando messageHtml ya construido/escapado en la plantilla
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
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
}
