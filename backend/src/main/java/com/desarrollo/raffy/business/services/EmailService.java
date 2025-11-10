package com.desarrollo.raffy.business.services;

import com.desarrollo.raffy.dto.WinnerDTO;
import com.desarrollo.raffy.model.StatusReport;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Servicio para el envío de correos electrónicos.
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Value("${app.from.email}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;


    /**
     * Envía un correo de verificación a la dirección de correo electrónico especificada.
     *
     * @param to la dirección de correo electrónico del destinatario.
     * @param verificationToken el token de verificación a incluir en el correo.
     */
    public void sendVerificationEmail(String to, String verificationToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Verificación de Email - Rafify");
        
        String verificationUrl = frontendUrl + "/verify-email?token=" + verificationToken;
        
        String messageText = "Hola,\n\n" +
                "Gracias por registrarte en Rafify.\n\n" +
                "Para completar tu registro, por favor haz clic en el siguiente enlace:\n" +
                verificationUrl + "\n\n" +
                "Este enlace expirará en 24 horas.\n\n" +
                "Si no te registraste en Rafify, puedes ignorar este email.\n\n" +
                "Saludos,\n" +
                "El equipo de Rafify";
        
        message.setText(messageText);
        
        emailSender.send(message);
    }

    /**
     * Envía correos de notificación a todos los ganadores de un evento.
     * Utiliza la plantilla HTML profesional con el logo y branding de Rafify.
     * 
     * @param winners Colección de ganadores con su información (nombre, email, posición)
     * @param eventId ID del evento
     * @param eventTitle Título del evento
     * @param eventType Tipo de evento (GIVEAWAY o GUESSING_CONTEST)
     */
    @Async
    public void sendWinnerEmails(Collection<WinnerDTO> winners,
                                 Long eventId,
                                 String eventTitle,
                                 String eventType,
                                 String creatorName,
                                 String creatorEmail,
                                 String creatorPhone) {
        // Validar que hay ganadores
        if (winners == null || winners.isEmpty()) {
            System.out.println("No hay ganadores a los que enviar correos");
            return;
        }
        
        System.out.println("Enviando correos a " + winners.size() + " ganadores del evento: " + eventTitle);
        
        // Construir la URL del evento en el frontend
        String eventUrl = frontendUrl + "/event/management/" + eventId;
        
        // Enviar correo a cada ganador
        winners.forEach(winner -> {
            try {
                // Validar que el ganador tiene email
                if (winner.getEmail() == null || winner.getEmail().trim().isEmpty()) {
                    System.err.println("⚠️ El ganador " + winner.getName() + " " + winner.getSurname() + 
                                     " no tiene email registrado. No se puede enviar notificación.");
                    return;
                }
                
                // Construir el nombre completo del ganador
                String winnerFullName = winner.getName() + " " + winner.getSurname();
                
                // Generar la plantilla HTML
                String htmlContent = emailTemplateService.generateWinnerNotificationTemplate(
                    winnerFullName,
                    winner.getPosition(),
                    eventTitle,
                    eventType,
                    eventUrl,
                    creatorName,
                    creatorEmail,
                    creatorPhone
                );
                
                // Determinar el asunto del correo según el tipo de evento
                String eventTypeText = eventType != null && eventType.equals("GUESSING_CONTEST") ? "sorteo" : "rifa";
                String subject = "🎉 ¡Felicidades! Has ganado en el " + eventTypeText + " - Rafify";
                
                // Enviar el correo HTML sin adjuntos ni recursos inline
                sendHtmlEmail(
                    winner.getEmail(),
                    subject,
                    htmlContent
                );
                
                System.out.println("✅ Correo de ganador enviado exitosamente a: " + winner.getEmail() + 
                                 " (Posición: " + winner.getPosition() + ")");
                
            } catch (Exception e) {
                System.err.println("❌ Error al enviar correo al ganador " + winner.getName() + " " + 
                                 winner.getSurname() + ": " + e.getMessage());
                e.printStackTrace();
                // Continuar con los demás ganadores aunque falle uno
            }
        });
        
        System.out.println("✅ Proceso de envío de correos a ganadores completado");
    }

    /**
     * Envía al creador del evento un resumen con el contacto de los ganadores.
     * @param winners Colección de ganadores
     * @param eventId ID del evento
     * @param eventTitle Título del evento
     * @param eventType Tipo del evento (GIVEAWAY / GUESSING_CONTEST)
     * @param creatorName Nombre del creador (para saludo)
     * @param creatorEmail Email del creador (destinatario)
     */
    @Async
    public void sendWinnersContactToCreator(Collection<WinnerDTO> winners,
                                            Long eventId,
                                            String eventTitle,
                                            String eventType,
                                            String creatorName,
                                            String creatorEmail) {
        if (creatorEmail == null || creatorEmail.isBlank()) {
            System.err.println("⚠️ El evento no tiene email de creador. No se enviará el resumen.");
            return;
        }

        if (winners == null) {
            System.err.println("⚠️ La lista de ganadores es nula. Enviaré el correo indicando que no hay ganadores.");
        }

        String eventUrl = frontendUrl + "/event/management/" + eventId;

        String htmlContent = emailTemplateService.generateCreatorWinnersSummaryTemplate(
                creatorName,
                eventTitle,
                eventType,
                eventUrl,
                winners
        );

        String eventTypeText = eventType != null && eventType.equals("GUESSING_CONTEST") ? "sorteo" : "rifa";
        String subject = "📋 Resumen de ganadores de tu " + eventTypeText + " - Rafify";

        try {
            sendHtmlEmail(creatorEmail, subject, htmlContent);
            System.out.println("✅ Resumen de ganadores enviado al creador: " + creatorEmail);
        } catch (Exception e) {
            System.err.println("❌ Error al enviar resumen al creador " + creatorEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envía un correo de verificación usando plantilla HTML.
     *
     * @param to la dirección de correo electrónico del destinatario.
     * @param userName nombre del usuario.
     * @param verificationToken el token de verificación a incluir en el correo.
     */
    public void sendVerificationEmailWithTemplate(String to, String userName, String verificationToken) {
        String verificationUrl = frontendUrl + "/verify-email?token=" + verificationToken;
        String htmlContent = emailTemplateService.generateEmailVerificationTemplate(userName, verificationUrl);
        
        try {
            sendEmailWithInlineResources(to, "Verificación de Email - Rafify", htmlContent, 
                                       emailTemplateService.getDefaultInlineResources());
        } catch (Exception e) {
            // Fallback al método simple si hay error con la plantilla
            sendVerificationEmail(to, verificationToken);
        }
    }

    /**
     * Envía un correo de restablecimiento de contraseña.
     *
     * @param to la dirección de correo electrónico del destinatario.
     * @param token el token de restablecimiento a incluir en el correo.
     */
    public void sendPasswordResetEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Restablecimiento de Contraseña - Rafify");

        String resetUrl = frontendUrl + "/reset-password?token=" + token;

        String messageText = "Hola,\n\n" +
                "Hemos recibido una solicitud para restablecer tu contraseña en Rafify.\n\n" +
                "Para restablecer tu contraseña, haz clic en el siguiente enlace:\n" +
                resetUrl + "\n\n" +
                "Este enlace expirará en 1 hora.\n\n" +
                "Si no solicitaste un restablecimiento de contraseña, puedes ignorar este email.\n\n" +
                "Saludos,\n" +
                "El equipo de Rafify";

        message.setText(messageText);

        emailSender.send(message);
    }

    /**
     * Envía un correo de restablecimiento de contraseña usando plantilla HTML.
     *
     * @param to la dirección de correo electrónico del destinatario.
     * @param userName nombre del usuario.
     * @param token el token de restablecimiento a incluir en el correo.
     */
    public void sendPasswordResetEmailWithTemplate(String to, String userName, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        String htmlContent = emailTemplateService.generatePasswordResetTemplate(userName, resetUrl);
        
        try {
            sendEmailWithInlineResources(to, "Restablecimiento de Contraseña - Rafify", htmlContent, 
                                       emailTemplateService.getDefaultInlineResources());
        } catch (Exception e) {
            // Fallback al método simple si hay error con la plantilla
            sendPasswordResetEmail(to, token);
        }
    }

    /**
     * Envía un correo de bienvenida usando plantilla HTML.
     *
     * @param to la dirección de correo electrónico del destinatario.
     * @param userName nombre del usuario.
     * @param verificationToken token de verificación (opcional).
     */
    public void sendWelcomeEmailWithTemplate(String to, String userName, String verificationToken) {
        String verificationUrl = verificationToken != null ? 
                               frontendUrl + "/verify-email?token=" + verificationToken : null;
        String htmlContent = emailTemplateService.generateWelcomeTemplate(userName, verificationUrl);
        
        System.out.println("Enviando correo de bienvenida HTML a: " + to);
        System.out.println("Contenido HTML generado: " + htmlContent.substring(0, Math.min(200, htmlContent.length())) + "...");
        
        sendHtmlEmail(to, "¡Bienvenido a Rafify!", htmlContent);
    }

    /**
     * Envía una notificación de evento usando plantilla HTML.
     *
     * @param to la dirección de correo electrónico del destinatario.
     * @param userName nombre del usuario.
     * @param eventName nombre del evento.
     * @param eventType tipo de evento (sorteo/rifa).
     * @param eventUrl URL del evento.
     * @param message mensaje personalizado.
     */
    public void sendEventNotificationWithTemplate(String to, String userName, String eventName, 
                                                  String eventType, String eventUrl, String message) {
        String htmlContent = emailTemplateService.generateEventNotificationTemplate(
            userName, eventName, eventType, eventUrl, message);
        
        try {
            sendEmailWithInlineResources(to, "Nueva notificación de " + eventType, htmlContent, 
                                       emailTemplateService.getDefaultInlineResources());
        } catch (Exception e) {
            // Fallback al método simple
            sendEmail(to, "Nueva notificación de " + eventType, message);
        }
    }


     /**
     * Envía un correo electrónico simple (texto plano).
     * @param to Correo electrónico del destinatario.
     * @param subject Asunto del correo.
     * @param text Cuerpo del mensaje.
     */
    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        
        message.setFrom(fromEmail);
        
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        try {
            emailSender.send(message);
            System.out.println("Correo enviado a: " + to + " con el asunto: " + subject + " y el texto: " + text);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar correo: " + e.getMessage());
        }
    }


    /**
     * Envía un correo con contenido HTML.
     * @param to destinatario
     * @param subject asunto
     * @param html contenido HTML (UTF-8)
     */
    public void sendHtmlEmail(String to, String subject, String html) {
        try {
            // Para correos HTML sin adjuntos, usar modo no-multipart evita encabezados y límites innecesarios
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            // Indicar explícitamente que el cuerpo es HTML
            helper.setText(html, true);

            // No establecer manualmente Content-Type/MIME-Version.
            // JavaMail los genera correctamente según la estructura del mensaje.

            emailSender.send(mimeMessage);
            System.out.println("✅ Correo HTML enviado correctamente a: " + to);
        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar correo HTML: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al enviar correo HTML: " + e.getMessage(), e);
        }
    }

    /**
     * Envía un correo (texto o HTML) con un único archivo adjunto en memoria.
     * @param to destinatario
     * @param subject asunto
     * @param body cuerpo (texto plano o HTML)
     * @param isHtml si true, se interpreta como HTML
     * @param attachmentFilename nombre del archivo adjunto
     * @param attachmentBytes contenido del adjunto
     * @param contentType tipo de contenido del adjunto (ej: application/pdf)
     */
    public void sendEmailWithAttachment(String to,
                                        String subject,
                                        String body,
                                        boolean isHtml,
                                        String attachmentFilename,
                                        byte[] attachmentBytes,
                                        String contentType) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, isHtml);

            InputStreamSource source = new ByteArrayResource(attachmentBytes);
            helper.addAttachment(attachmentFilename, source, contentType);

            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar correo con adjunto: " + e.getMessage(), e);
        }
    }

    /**
     * Envía un correo HTML con recursos inline (imágenes incrustadas) referenciados por CID en el HTML.
     * En el HTML use por ejemplo: <img src="cid:logo" /> y en el mapa agregue la clave "logo".
     * @param to destinatario
     * @param subject asunto
     * @param html contenido HTML
     * @param inlineResources mapa cid -> recurso inline
     */
    public void sendEmailWithInlineResources(String to,
                                             String subject,
                                             String html,
                                             Map<String, InlineResource> inlineResources) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            if (inlineResources != null) {
                for (Map.Entry<String, InlineResource> entry : inlineResources.entrySet()) {
                    InlineResource res = entry.getValue();
                    if (res != null && res.content != null && res.content.length > 0) {
                        try {
                            helper.addInline(entry.getKey(), new ByteArrayResource(res.content), res.contentType);
                        } catch (Exception e) {
                            System.err.println("Error al agregar recurso inline " + entry.getKey() + ": " + e.getMessage());
                            // Continuar sin este recurso
                        }
                    }
                }
            }

            emailSender.send(mimeMessage);
            System.out.println("Correo HTML enviado exitosamente a: " + to);
        } catch (MessagingException e) {
            System.err.println("Error al enviar correo con recursos inline: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al enviar correo con recursos inline: " + e.getMessage(), e);
        }
    }

    /**
     * Envío avanzado: soporta múltiples destinatarios, CC/BCC, reply-to,
     * prioridad, adjuntos múltiples y recursos inline.
     */
    public void sendAdvancedEmail(EmailMessage email) {
        Objects.requireNonNull(email, "email no puede ser null");
        if ((email.htmlBody == null || email.htmlBody.isBlank()) && (email.textBody == null || email.textBody.isBlank())) {
            throw new IllegalArgumentException("Debe proveer textBody o htmlBody");
        }
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail);
            setAddresses(helper, email);
            helper.setSubject(email.subject);

            if (email.htmlBody != null && !email.htmlBody.isBlank()) {
                if (email.textBody != null && !email.textBody.isBlank()) {
                    helper.setText(email.textBody, email.htmlBody);
                } else {
                    helper.setText(email.htmlBody, true);
                }
            } else {
                helper.setText(email.textBody, false);
            }

            if (email.priority != null) {
                // 1 (Alta) .. 5 (Baja)
                mimeMessage.setHeader("X-Priority", String.valueOf(email.priority));
            }

            if (email.attachments != null) {
                for (Attachment attachment : email.attachments) {
                    if (attachment != null && attachment.content != null) {
                        helper.addAttachment(attachment.filename, new ByteArrayResource(attachment.content), attachment.contentType);
                    }
                }
            }

            if (email.inlineResources != null) {
                for (InlineResource res : email.inlineResources) {
                    if (res != null && res.content != null && res.contentId != null) {
                        helper.addInline(res.contentId, new ByteArrayResource(res.content), res.contentType);
                    }
                }
            }

            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar correo avanzado: " + e.getMessage(), e);
        }
    }

    private void setAddresses(MimeMessageHelper helper, EmailMessage email) throws MessagingException {
        if (email.to != null && !email.to.isEmpty()) {
            helper.setTo(email.to.toArray(String[]::new));
        }
        if (email.cc != null && !email.cc.isEmpty()) {
            helper.setCc(email.cc.toArray(String[]::new));
        }
        if (email.bcc != null && !email.bcc.isEmpty()) {
            helper.setBcc(email.bcc.toArray(String[]::new));
        }
        if (email.replyTo != null && !email.replyTo.isBlank()) {
            helper.setReplyTo(email.replyTo);
        }
    }

    /**
     * Recurso adjunto (archivo) en memoria.
     */
    public static class Attachment {
        public final String filename;
        public final byte[] content;
        public final String contentType;

        public Attachment(String filename, byte[] content, String contentType) {
            this.filename = filename;
            this.content = content;
            this.contentType = contentType;
        }
    }

    /**
     * Notifica un pago correcto de múltiples boletos/tickets en un solo envío.
     * Reutiliza la plantilla HTML y soporta adjuntos opcionales para los comprobantes.
     * No incluye método de pago ni números de comprobante.
     *
     * @param to Correo del destinatario
     * @param userName Nombre del usuario
     * @param eventName Nombre del evento
     * @param ticketsCount Cantidad de boletos/tickets adquiridos
     * @param eventUrl URL para ver detalles del evento
     * @param attachments Adjuntos opcionales (por ejemplo, comprobantes en PDF)
     */
    public void sendMultiTicketPaymentConfirmation(String to,
                                                   String userName,
                                                   String eventName,
                                                   int ticketsCount,
                                                   String eventUrl,
                                                   Collection<Attachment> attachments) {
        String htmlContent = emailTemplateService.generateMultiTicketPaymentConfirmationTemplate(
                userName, eventName, ticketsCount, eventUrl);

        String textBody = "Hola " + userName + ",\n\n" +
                "Tu pago fue procesado correctamente.\n" +
                "Has adquirido " + ticketsCount + " boletos para el evento " + eventName + ".\n" +
                "Adjuntamos los comprobantes correspondientes.\n\n" +
                "Puedes ver los detalles aquí: " + eventUrl + "\n\n" +
                "Gracias por confiar en Rafify.";

        EmailMessage email = new EmailMessage(
                List.of(to),
                null,
                null,
                null,
                "Pago confirmado - Rafify",
                textBody,
                htmlContent,
                null,
                attachments,
                emailTemplateService.getDefaultInlineResources() != null
                        ? emailTemplateService.getDefaultInlineResources().values()
                        : null
        );

        sendAdvancedEmail(email);
    }

    /**
     * Envía confirmación de compra de números de rifa al comprador.
     * @param to Email del comprador
     * @param buyerName Nombre completo del comprador
     * @param eventId ID de la rifa para construir URL
     * @param eventTitle Título del evento
     * @param priceOfNumber Precio por número
     * @param purchasedNumbers Lista de números adquiridos
     */
    public void sendRaffleNumbersPurchasedEmail(String to,
                                                String buyerName,
                                                Long eventId,
                                                String eventTitle,
                                                double priceOfNumber,
                                                Collection<Integer> purchasedNumbers) {
        if (to == null || to.isBlank()) return;
        String eventUrl = frontendUrl + "/event/management/" + eventId;
        String htmlContent = emailTemplateService.generateRaffleNumbersPurchasedTemplate(
                buyerName, eventTitle, priceOfNumber, purchasedNumbers, eventUrl);

        String textBody = "Hola " + buyerName + ",\n\n" +
                "Gracias por tu compra. Has adquirido números para la rifa '" + eventTitle + "'.\n" +
                "Precio por número: $" + String.format("%.2f", priceOfNumber) + "\n" +
                "Números: " + (purchasedNumbers == null ? "" : purchasedNumbers.stream().sorted().map(String::valueOf).reduce((a,b) -> a + ", " + b).orElse("")) + "\n\n" +
                "Puedes ver los detalles aquí: " + eventUrl + "\n\n" +
                "Conserva este correo como comprobante de tu compra.";

        try {
            sendEmailWithInlineResources(to, "Confirmación de compra - Rafify", htmlContent,
                    emailTemplateService.getDefaultInlineResources());
        } catch (Exception e) {
            // Fallback a HTML simple si falla inline resources
            sendHtmlEmail(to, "Confirmación de compra - Rafify", htmlContent);
        }
    }
    /**
     * Notifica al usuario el estado de su reporte de evento.
     * @param emailReport
     * @param eventTitle
     * @param status
     * @param reason
     */
    public void sendEmailToReporter(
                                    String emailReport,
                                    String eventTitle,
                                    StatusReport status,
                                    String reason) {
        if (emailReport == null || emailReport.isBlank()) return;
        String subject = "Actualización de tu reporte - Raffify";
        String statusText = status == StatusReport.APPROVED ? "aprobado" : "rechazado";
        String htmlContent = generateReportStatusTemplate(
            eventTitle, 
            statusText, 
            reason
        );

        String textBody = String.format(
            """
            Hola,
            Te informamos que el estado de tu reporte sobre el evento "%s" ha sido %s.
            
            %s

            Si tienes alguna pregunta, no dudes en contactarnos.

            Saludos,
            Equipo de Raffify
            """, 
            eventTitle, statusText, reason
        );

        try {
            sendEmailWithInlineResources(
                emailReport, 
                subject, 
                htmlContent, 
                emailTemplateService.getDefaultInlineResources()
            );
        } catch (Exception e) {
            // Fallback a correo simple si falla el HTML
           sendEmail(emailReport, subject, textBody);
        }

    }

    /**
     * Genera la plantilla HTML para el estado del reporte.
     * @param eventTitle
     * @param status
     * @param reason
     * @return
     */
    public String generateReportStatusTemplate(String eventTitle, String status, String reason) {
        return String.format(
            """
            <!DOCTYPE html>
            <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <title>Estado de su reporte - Raffify</title>
                </head>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;">
                        <h2 style="color: #2c3e50;">Estado de su reporte</h2>
                        <p>Estimado usuario,</p>
                        <p>Le informamos que el estado de su reporte sobre el evento "<strong>%s</strong>" ha sido <strong>%s</strong>.</p>
                        <p>%s</p>
                        <p style="margin-top: 30px;">Gracias por ayudarnos a mantener la comunidad segura y confiable.</p>
                        <p>Atentamente,<br/>El equipo de Raffify</p>
                        <p style="font-size: 0.9em; color: #888;">Este es un correo automático, por favor no responda.</p>
                    </div>
                </body>
            </html>
            """,eventTitle, status, reason);
    }

    /**
     * Recurso inline para referenciar via CID en HTML.
     */
    public static class InlineResource {
        public final String contentId;
        public final byte[] content;
        public final String contentType;

        public InlineResource(String contentId, byte[] content, String contentType) {
            this.contentId = contentId;
            this.content = content;
            this.contentType = contentType;
        }
    }

    /**
     * Modelo de correo .
     */
    public static class EmailMessage {
        public final List<String> to;
        public final List<String> cc;
        public final List<String> bcc;
        public final String replyTo;
        public final String subject;
        public final String textBody;  // opcional
        public final String htmlBody;  // opcional
        public final Integer priority; // 1 (alta) .. 5 (baja)
        public final Collection<Attachment> attachments; // opcional
        public final Collection<InlineResource> inlineResources; // opcional

        public EmailMessage(List<String> to,
                            List<String> cc,
                            List<String> bcc,
                            String replyTo,
                            String subject,
                            String textBody,
                            String htmlBody,
                            Integer priority,
                            Collection<Attachment> attachments,
                            Collection<InlineResource> inlineResources) {
            this.to = to;
            this.cc = cc;
            this.bcc = bcc;
            this.replyTo = replyTo;
            this.subject = subject;
            this.textBody = textBody;
            this.htmlBody = htmlBody;
            this.priority = priority;
            this.attachments = attachments;
            this.inlineResources = inlineResources;
        }
    }

}