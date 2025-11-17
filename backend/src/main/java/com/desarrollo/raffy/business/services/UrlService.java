package com.desarrollo.raffy.business.services;

import com.desarrollo.raffy.business.repository.UrlRepository;
import com.desarrollo.raffy.model.Url;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.desarrollo.raffy.util.LinkTransform;
import java.time.LocalDateTime;

@Service
public class UrlService {
    @Autowired
    private UrlRepository urlRepository;
    /**
     * Guardar una URL
     * @param url URL a guardar
     * @return URL guardada
     */
    public Url saveUrl(String url) {
        if (urlRepository.existsByOriginalUrl(url)) {
            return urlRepository.findByOriginalUrl(url);
        }
        Url newUrl = new Url();
        newUrl.setOriginalUrl(url);
        newUrl.setShortcode(LinkTransform.shortenUrl(url));
        newUrl.setClickCount(0);
        newUrl.setCreatedAt(LocalDateTime.now());
        return urlRepository.save(newUrl);
    }

    /**
     * Obtener una URL por su ID
     * @param shortcode Shortcode de la URL
     * @return URL encontrada
     */
    public Url getUrlByShortcode(String shortcode) {
        return urlRepository.findByShortcode(shortcode);
    }

    /**
     * Incrementar el contador de clicks por shortcode
     * @param shortcode Shortcode de la URL
     * @return URL actualizada con el contador incrementado
     */
    public Url incrementClickCount(String shortcode) {
        Url url = urlRepository.findByShortcode(shortcode);
        if (url == null) {
            return null;
        }
        Integer current = url.getClickCount();
        url.setClickCount(current == null ? 1 : current + 1);
        return urlRepository.save(url);
    }

    /**
     * Convertir un Link en un QR
     * @param link Link a convertir
     * @return QR convertido
     */
    public String convertLinkToQr(String link) {
        return LinkTransform.linkToQr(link);
    }

    /**
     * Crear una URL de uso único
     * @param url URL original
     * @return URL de uso único creada
     */
    public Url createSingleUseUrl(String url) {
        Url newUrl = new Url();
        newUrl.setOriginalUrl(url);
        newUrl.setShortcode(LinkTransform.shortenUrl(url + System.currentTimeMillis())); // Añadir timestamp para hacerlo único
        newUrl.setClickCount(0);
        newUrl.setCreatedAt(LocalDateTime.now());
        newUrl.setIsSingleUse(true);
        newUrl.setIsUsed(false);
        return urlRepository.save(newUrl);
    }

    /**
     * Obtener una URL de uso único por su shortcode
     * @param shortcode Shortcode de la URL
     * @return URL encontrada o null si no existe
     */
    public Url getSingleUseUrlByShortcode(String shortcode) {
        Url url = urlRepository.findByShortcode(shortcode);
        if (url != null && url.getIsSingleUse() != null && url.getIsSingleUse()) {
            return url;
        }
        return null;
    }

    /**
     * Marcar una URL de uso único como usada
     * @param shortcode Shortcode de la URL
     * @return URL actualizada o null si no existe
     */
    public Url markSingleUseUrlAsUsed(String shortcode) {
        Url url = getSingleUseUrlByShortcode(shortcode);
        if (url != null && url.getIsUsed() != null && !url.getIsUsed()) {
            url.setIsUsed(true);
            url.setClickCount(url.getClickCount() + 1);
            return urlRepository.save(url);
        }
        return null;
    }
}
