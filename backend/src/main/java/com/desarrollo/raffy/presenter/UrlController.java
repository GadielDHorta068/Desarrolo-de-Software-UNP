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
import com.desarrollo.raffy.model.Url;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/url")
public class UrlController {
    @Autowired
    private UrlService urlService;

    /**
     * Guardar una URL
     * @param url URL a guardar
     * @return URL guardada
     */
    @PostMapping("/save")
    public Map<String, Object> saveUrl(@RequestBody String url) {
        Map<String, Object> map = new HashMap<>();
        map.put("url", urlService.saveUrl(url));
        map.put("qr", urlService.convertLinkToQr(url));
        return map;
    }

    /**
     * Obtener una URL por su shortcode
     * @param shortcode Shortcode de la URL
     * @return URL encontrada
     */
    @GetMapping("/{shortcode}")
    public ResponseEntity<Url> getUrlByShortcode(@PathVariable String shortcode) {
        return ResponseEntity.ok(urlService.getUrlByShortcode(shortcode));
    }

    /**
     * Redirigir a la URL original
     * @param shortcode Shortcode de la URL
     * @return Redirección a la URL original
     */
    @GetMapping("/redirect/{shortcode}")
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable String shortcode) {
        Url url = urlService.incrementClickCount(shortcode);
        if (url == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(302).location(java.net.URI.create(url.getOriginalUrl())).build();
    }

    
    
}
