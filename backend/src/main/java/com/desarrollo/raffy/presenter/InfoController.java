package com.desarrollo.raffy.presenter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.lang.management.ManagementFactory;

/**
 * Controlador para el endpoint de información de la aplicación
 * Proporciona detalles sobre el estado y configuración de la aplicación
 */
@RestController
public class InfoController {

    @Value("${spring.application.name:raffy}")
    private String applicationName;

    @Value("${spring.application.version:0.0.1-SNAPSHOT}")
    private String applicationVersion;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    /**
     * Endpoint que retorna información general de la aplicación
     * @return Map con información de la aplicación
     */
    @GetMapping("/actuator/info")
    public Map<String, Object> getInfo() {
        Map<String, Object> info = new HashMap<>();
        
        // Información básica de la aplicación
        info.put("application", applicationName);
        info.put("version", applicationVersion);
        info.put("profile", activeProfile);
        info.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Información del entorno
        info.put("environment", System.getProperty("java.version"));
        info.put("os", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        
        // Información del servidor
        info.put("server", "Spring Boot " + getSpringBootVersion());
        
        // Estado de la aplicación
        info.put("status", "RUNNING");
        info.put("uptime", getUptime());
        
        return info;
    }

    /**
     * Obtiene la versión de Spring Boot desde el classpath
     * @return Versión de Spring Boot
     */
    private String getSpringBootVersion() {
        try {
            Package springBootPackage = org.springframework.boot.SpringApplication.class.getPackage();
            return springBootPackage.getImplementationVersion() != null ? 
                   springBootPackage.getImplementationVersion() : "3.3.3";
        } catch (Exception e) {
            return "3.3.3"; // Versión por defecto si no se puede obtener
        }
    }

    /**
     * Calcula el tiempo de ejecución de la aplicación
     * @return Tiempo de ejecución en formato legible
     */
    private String getUptime() {
        long uptime = System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime();
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%d días, %d horas, %d minutos", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%d horas, %d minutos", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%d minutos, %d segundos", minutes, seconds % 60);
        } else {
            return String.format("%d segundos", seconds);
        }
    }
}
