package com.desarrollo.raffy.util;

import java.util.Base64;

public class ImageUtils {
    
    /**
     * Convierte un array de bytes a una cadena Base64
     * @param imageBytes Array de bytes de la imagen
     * @return Cadena Base64 o null si el input es null
     */
    public static String bytesToBase64(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            return null;
        }
        return Base64.getEncoder().encodeToString(imageBytes);
    }
    
    /**
     * Convierte una cadena Base64 a un array de bytes
     * @param base64String Cadena Base64
     * @return Array de bytes o null si el input es null o vacío
     */
    public static byte[] base64ToBytes(String base64String) {
        if (base64String == null || base64String.trim().isEmpty()) {
            return null;
        }
        try {
            // Remover el prefijo data:image si existe
            String cleanBase64 = base64String;
            if (base64String.contains(",")) {
                cleanBase64 = base64String.split(",")[1];
            }
            return Base64.getDecoder().decode(cleanBase64);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Formato Base64 inválido: " + e.getMessage());
        }
    }
    
    /**
     * Valida si una cadena es un Base64 válido
     * @param base64String Cadena a validar
     * @return true si es válido, false en caso contrario
     */
    public static boolean isValidBase64(String base64String) {
        if (base64String == null || base64String.trim().isEmpty()) {
            return false;
        }
        try {
            String cleanBase64 = base64String;
            if (base64String.contains(",")) {
                cleanBase64 = base64String.split(",")[1];
            }
            Base64.getDecoder().decode(cleanBase64);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Obtiene el tipo MIME de una imagen Base64
     * @param base64String Cadena Base64 con prefijo data:image
     * @return Tipo MIME o "image/jpeg" por defecto
     */
    public static String getMimeType(String base64String) {
        if (base64String != null && base64String.startsWith("data:image/")) {
            int semicolonIndex = base64String.indexOf(";");
            if (semicolonIndex > 0) {
                return base64String.substring(5, semicolonIndex); // Remover "data:"
            }
        }
        return "image/jpeg"; // Tipo por defecto
    }
}