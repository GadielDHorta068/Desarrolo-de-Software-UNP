package com.desarrollo.raffy.presenter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.desarrollo.raffy.business.services.AuthService;
import com.desarrollo.raffy.dto.UserResponse;

@RestController
@RequestMapping("/share")
public class ShareController {

    @Autowired
    private AuthService authService;

    @GetMapping(value = "/profile/{nickname}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> shareProfile(@PathVariable String nickname, HttpServletRequest request) {
        try {
            UserResponse user = authService.getUserByNickname(nickname);

            String baseUrl = resolveBaseUrl(request);
            String profileUrl = baseUrl + "/profile/" + escape(nickname);

            String displayName = buildDisplayName(user);
            String description = (user.getDescription() != null && !user.getDescription().isBlank())
                    ? user.getDescription()
                    : ("Perfil de " + displayName + " en Raffy");

            // Preferimos avatar; si no hay, usar portada
            String imageUrl = null;
            if (user.getImagen() != null && !user.getImagen().isBlank()) {
                imageUrl = baseUrl + "/auth/users/" + escape(nickname) + "/avatar";
            } else if (user.getCoverImage() != null && !user.getCoverImage().isBlank()) {
                imageUrl = baseUrl + "/auth/users/" + escape(nickname) + "/cover";
            }

            String html = buildShareHtml(profileUrl, displayName, description, imageUrl);
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
        } catch (RuntimeException e) {
            String html = "<!doctype html><html><head><meta charset=\\\"utf-8\\\">" +
                    "<title>Perfil no encontrado</title>" +
                    "<meta name=\\\"robots\\\" content=\\\"noindex\\\">" +
                    "</head><body>Perfil no encontrado</body></html>";
            return ResponseEntity.status(404).contentType(MediaType.TEXT_HTML).body(html);
        }
    }

    private String resolveBaseUrl(HttpServletRequest request) {
        String proto = headerOr(request, "X-Forwarded-Proto", request.getScheme());
        String host = headerOr(request, "X-Forwarded-Host", request.getHeader("Host"));
        if (host == null || host.isBlank()) {
            host = request.getServerName() + (request.getServerPort() > 0 ? ":" + request.getServerPort() : "");
        }
        return proto + "://" + host;
    }

    private String headerOr(HttpServletRequest request, String header, String fallback) {
        String value = request.getHeader(header);
        return (value != null && !value.isBlank()) ? value : fallback;
    }

    private String buildDisplayName(UserResponse user) {
        String name = (user.getName() != null) ? user.getName() : "";
        String surname = (user.getSurname() != null) ? user.getSurname() : "";
        String combined = (name + " " + surname).trim();
        if (combined.isEmpty()) {
            combined = user.getNickname();
        } else {
            combined += " (" + user.getNickname() + ")";
        }
        return combined;
    }

    private String buildShareHtml(String profileUrl, String title, String description, String imageUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!doctype html><html lang=\\\"es\\\"><head>");
        sb.append("<meta charset=\\\"utf-8\\\">");
        sb.append("<meta name=\\\"viewport\\\" content=\\\"width=device-width, initial-scale=1\\\">");
        sb.append("<title").append(">").append(escape(title)).append("</title>");
        // Canonical
        sb.append("<link rel=\\\"canonical\\\" href=\\\"").append(profileUrl).append("\\\">");
        // Open Graph
        sb.append(metaProperty("og:title", title));
        sb.append(metaProperty("og:description", description));
        sb.append(metaProperty("og:type", "profile"));
        sb.append(metaProperty("og:url", profileUrl));
        if (imageUrl != null) {
            sb.append(metaProperty("og:image", imageUrl));
            sb.append(metaProperty("og:image:alt", title));
        }
        // Twitter
        sb.append(metaName("twitter:card", imageUrl != null ? "summary_large_image" : "summary"));
        sb.append(metaName("twitter:title", title));
        sb.append(metaName("twitter:description", description));
        if (imageUrl != null) {
            sb.append(metaName("twitter:image", imageUrl));
        }
        // Redirect para navegadores
        sb.append("<meta http-equiv=\\\"refresh\\\" content=\\\"0;url=")
          .append(profileUrl).append("\\\">");
        sb.append("</head><body></body></html>");
        return sb.toString();
    }

    private String metaProperty(String property, String content) {
        return "<meta property=\\\"" + property + "\\\" content=\\\"" + escape(content) + "\\\">";
    }

    private String metaName(String name, String content) {
        return "<meta name=\\\"" + name + "\\\" content=\\\"" + escape(content) + "\\\">";
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}