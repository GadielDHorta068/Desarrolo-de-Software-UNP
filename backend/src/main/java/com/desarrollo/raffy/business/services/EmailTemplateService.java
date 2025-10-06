package com.desarrollo.raffy.business.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.nio.file.Files;

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
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html lang=\"es\">");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        html.append("<title>").append(escapeHtml(title)).append("</title>");
        html.append("<style>");
        html.append(getEmailStyles());
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        // Header con logo
        html.append("<div class=\"email-container\">");
        html.append("<div class=\"header\">");
        html.append("<div class=\"logo-section\">");
        html.append("<img src=\"").append(getLogoBase64()).append("\" alt=\"Rafify\" class=\"logo\">");
        html.append("<h1 class=\"brand-name\">Rafify</h1>");
        html.append("</div>");
        html.append("</div>");
        
        // Contenido principal
        html.append("<div class=\"content\">");
        html.append("<h2 class=\"notification-title\">").append(escapeHtml(title)).append("</h2>");
        html.append("<div class=\"message\">");
        html.append("<p>").append(escapeHtml(message)).append("</p>");
        html.append("</div>");
        
        // Botón de acción si se proporciona
        if (actionText != null && actionUrl != null && !actionText.trim().isEmpty() && !actionUrl.trim().isEmpty()) {
            html.append("<div class=\"action-section\">");
            html.append("<a href=\"").append(escapeHtml(actionUrl)).append("\" class=\"action-button\">");
            html.append(escapeHtml(actionText));
            html.append("</a>");
            html.append("</div>");
        }
        
        html.append("</div>");
        
        // Footer
        html.append("<div class=\"footer\">");
        if (footerMessage != null && !footerMessage.trim().isEmpty()) {
            html.append("<p class=\"footer-message\">").append(escapeHtml(footerMessage)).append("</p>");
        }
        html.append("<div class=\"footer-links\">");
        html.append("<p>© 2025 Rafify. Todos los derechos reservados.</p>");
        html.append("<p>Este es un correo automático, por favor no respondas a este mensaje.</p>");
        html.append("</div>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
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
        String title = "Nueva notificación de " + eventType;
        String fullMessage = "Hola " + userName + "," +
                            message + "" +
                            "<strong>Evento:</strong> " + eventName + "" +
                            "<strong>Tipo:</strong> " + eventType;
        
        String actionText = "Ver " + eventType;
        String actionUrl = eventUrl;
        String footerMessage = "Mantente al día con las últimas novedades de tus eventos favoritos.";
        
        return generateNotificationTemplate(title, fullMessage, actionText, actionUrl, footerMessage);
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
            // Intentar cargar logo personalizado
            String logoBase64 = loadCustomLogo();
            if (logoBase64 != null) {
                return "data:image/png;base64," + logoBase64;
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar logo personalizado: " + e.getMessage());
        }
        
        // Fallback al logo generado programáticamente
        String logoBase64 = createSimpleLogoBase64();
        return "data:image/svg+xml;base64," + logoBase64;
    }

    /**
     * Carga el logo personalizado desde assets/images/logo.png
     */
    private String loadCustomLogo() throws IOException {
        Resource logoResource = new ClassPathResource("assets/images/logo.png");
        if (logoResource.exists()) {
            byte[] logoBytes = Files.readAllBytes(logoResource.getFile().toPath());
            return java.util.Base64.getEncoder().encodeToString(logoBytes);
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



    /**
     * Obtiene los estilos CSS para las plantillas de email.
     */
    private String getEmailStyles() {
        return """
            * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
            }
            
            body {
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                line-height: 1.6;
                color: #333;
                background-color: #f8fafc;
            }
            
            .email-container {
                max-width: 600px;
                margin: 0 auto;
                background-color: #ffffff;
                border-radius: 8px;
                overflow: hidden;
                box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
            }
            
            .header {
                background: linear-gradient(135deg, #4F46E5 0%, #7C3AED 100%);
                padding: 20px;
                text-align: center;
            }
            
            .logo-section {
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 12px;
            }
            
            .logo {
                width: 50px;
                height: 50px;
                object-fit: contain;
                border-radius: 8px;
            }
            
            .brand-name {
                color: white;
                font-size: 24px;
                font-weight: bold;
                margin: 0;
            }
            
            .content {
                padding: 30px;
            }
            
            .notification-title {
                color: #1F2937;
                font-size: 24px;
                font-weight: 600;
                margin-bottom: 20px;
                text-align: center;
            }
            
            .message {
                margin-bottom: 30px;
            }
            
            .message p {
                font-size: 16px;
                line-height: 1.6;
                color: #4B5563;
            }
            
            .action-section {
                text-align: center;
                margin: 30px 0;
            }
            
            .action-button {
                display: inline-block;
                background: linear-gradient(135deg, #4F46E5 0%, #7C3AED 100%);
                color: white;
                padding: 12px 30px;
                text-decoration: none;
                border-radius: 6px;
                font-weight: 600;
                font-size: 16px;
                transition: transform 0.2s ease;
            }
            
            .action-button:hover {
                transform: translateY(-2px);
                box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
            }
            
            .footer {
                background-color: #F9FAFB;
                padding: 20px 30px;
                border-top: 1px solid #E5E7EB;
            }
            
            .footer-message {
                color: #6B7280;
                font-size: 14px;
                margin-bottom: 15px;
                text-align: center;
            }
            
            .footer-links {
                text-align: center;
            }
            
            .footer-links p {
                color: #9CA3AF;
                font-size: 12px;
                margin: 5px 0;
            }
            
            @media (max-width: 600px) {
                .email-container {
                    margin: 0;
                    border-radius: 0;
                }
                
                .content {
                    padding: 20px;
                }
                
                .notification-title {
                    font-size: 20px;
                }
                
                .action-button {
                    padding: 10px 25px;
                    font-size: 14px;
                }
            }
            """;
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
