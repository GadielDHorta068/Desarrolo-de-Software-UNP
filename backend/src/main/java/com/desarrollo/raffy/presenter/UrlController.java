package com.desarrollo.raffy.presenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.desarrollo.raffy.business.services.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.desarrollo.raffy.model.Url;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/url")
@Tag(name = "URLs", description = "Acortador y enlaces de invitación: guardar, redirigir y uso único")
public class UrlController {
    @Autowired
    private UrlService urlService;

    /**
     * Guardar una URL
     * @param url URL a guardar
     * @return URL guardada
     */
    @PostMapping("/save")
    @Operation(summary = "Guardar URL", description = "Guarda una URL y devuelve el shortcode y un QR")
    public Map<String, Object> saveUrl(@RequestBody String url) {
        Map<String, Object> map = new HashMap<>();
        map.put("url", urlService.saveUrl(url));
        map.put("qr", urlService.convertLinkToQr(url));
        return map;
    }

    @PostMapping("/event/{eventId}/save")
    @Operation(summary = "Guardar URL para evento", description = "Guarda una URL asociada a un evento y devuelve el QR")
    public Map<String, Object> saveUrlForEvent(@PathVariable Long eventId, @RequestBody String url) {
        Map<String, Object> map = new HashMap<>();
        map.put("url", urlService.saveUrlForEvent(eventId, url));
        map.put("qr", urlService.convertLinkToQr(url));
        return map;
    }

    /**
     * Obtener una URL por su shortcode
     * @param shortcode Shortcode de la URL
     * @return URL encontrada
     */
    @GetMapping("/{shortcode}")
    @Operation(summary = "Obtener URL por shortcode", description = "Obtiene la URL original y metadatos por su shortcode")
    public ResponseEntity<Url> getUrlByShortcode(@PathVariable String shortcode) {
        return ResponseEntity.ok(urlService.getUrlByShortcode(shortcode));
    }

    /**
     * Redirigir a la URL original
     * @param shortcode Shortcode de la URL
     * @return Redirección a la URL original
     */
    @GetMapping("/redirect/{shortcode}")
    @Operation(summary = "Redirigir a URL", description = "Redirige (302) a la URL original y contabiliza clics")
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable String shortcode) {
        Url url = urlService.incrementClickCount(shortcode);
        if (url == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(302).location(java.net.URI.create(url.getOriginalUrl())).build();
    }

    /**
     * Generar una URL de uso único
     * @param url URL a acortar como uso único
     * @return URL acortada con código QR de uso único
     */
    @PostMapping("/single-use")
    @Operation(summary = "Crear URL de uso único", description = "Genera un enlace de un solo uso con QR")
    public Map<String, Object> createSingleUseUrl(@RequestBody String url) {
        Map<String, Object> map = new HashMap<>();
        map.put("url", urlService.createSingleUseUrl(url));
        map.put("qr", urlService.convertLinkToQr(url));
        return map;
    }

    /**
     * Redirigir a una URL de uso único (se marca como usada después del primer acceso)
     * @param shortcode Shortcode de la URL de uso único
     * @return Redirección a la URL original o error si ya fue usada
     */
    @GetMapping("/single-use/redirect/{shortcode}")
    @Operation(summary = "Redirigir URL de uso único", description = "Redirige (302) y marca el enlace como usado; responde 410 si ya fue utilizado")
    public ResponseEntity<Void> redirectToSingleUseUrl(@PathVariable String shortcode) {
        Url url = urlService.getSingleUseUrlByShortcode(shortcode);
        if (url == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (url.getIsUsed() != null && url.getIsUsed()) {
            return ResponseEntity.status(410).build(); // Gone - URL already used
        }
        
        // Marcar como usada y redirigir
        urlService.markSingleUseUrlAsUsed(shortcode);
        return ResponseEntity.status(302).location(java.net.URI.create(url.getOriginalUrl())).build();
    }

    
    
}
