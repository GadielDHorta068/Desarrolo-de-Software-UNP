package com.desarrollo.raffy.presenter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.desarrollo.raffy.util.ImageUtils;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
public class ImageController {
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"};
    
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // Validar que el archivo no esté vacío
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("No se ha seleccionado ningún archivo"));
            }
            
            // Validar tamaño del archivo
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body(createErrorResponse("El archivo es demasiado grande. Máximo 5MB permitido"));
            }
            
            // Validar tipo de archivo
            String contentType = file.getContentType();
            if (contentType == null || !isAllowedImageType(contentType)) {
                return ResponseEntity.badRequest().body(createErrorResponse("Tipo de archivo no permitido. Solo se permiten imágenes (JPEG, PNG, GIF, WebP)"));
            }
            
            // Convertir a Base64
            byte[] imageBytes = file.getBytes();
            String base64Image = ImageUtils.bytesToBase64(imageBytes);
            
            // Crear respuesta con prefijo data URL
            String dataUrl = "data:" + contentType + ";base64," + base64Image;
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Imagen subida exitosamente");
            response.put("base64", base64Image);
            response.put("dataUrl", dataUrl);
            response.put("size", file.getSize());
            response.put("type", contentType);
            response.put("filename", file.getOriginalFilename());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al procesar la imagen: " + e.getMessage()));
        }
    }
    
    private boolean isAllowedImageType(String contentType) {
        for (String allowedType : ALLOWED_TYPES) {
            if (allowedType.equals(contentType)) {
                return true;
            }
        }
        return false;
    }
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        return error;
    }
}