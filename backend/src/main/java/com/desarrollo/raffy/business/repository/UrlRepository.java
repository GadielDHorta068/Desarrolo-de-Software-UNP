package com.desarrollo.raffy.business.repository;

import org.springframework.data.repository.CrudRepository;

import com.desarrollo.raffy.model.Url;

public interface UrlRepository extends CrudRepository<Url, Long> {
    /**
     * Obtener una URL por su shortcode
     * @param shortcode Shortcode de la URL
     * @return URL encontrada
     */
    Url findByShortcode(String shortcode);

    /**
     * Obtener una URL por su original URL
     * @param originalUrl Original URL de la URL
     * @return URL encontrada
     */
    Url findByOriginalUrl(String originalUrl);

    /**
     * Verificar si existe una URL por su original URL
     * @param originalUrl Original URL de la URL
     * @return true si existe, false en caso contrario
     */
    boolean existsByOriginalUrl(String originalUrl);

}
