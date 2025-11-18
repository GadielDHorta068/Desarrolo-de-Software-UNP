package com.desarrollo.raffy.presenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para el endpoint de salud de la aplicación
 * Proporciona información sobre el estado de salud de la aplicación y sus dependencias
 */
@RestController
@Tag(name = "Salud", description = "Estado de salud de la aplicación y dependencias")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Endpoint personalizado para verificar la salud de la aplicación
     * @return Map con el estado de salud de la aplicación
     */
    @GetMapping("/actuator/health")
    @Operation(summary = "Salud", description = "Devuelve estado general, base de datos y sistema")
    public Map<String, Object> getHealth() {
        Map<String, Object> health = new HashMap<>();
        
        // Estado general de la aplicación
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        
        // Verificación de la base de datos
        Map<String, Object> database = checkDatabaseHealth();
        health.put("database", database);
        
        // Verificación del sistema
        Map<String, Object> system = checkSystemHealth();
        health.put("system", system);
        
        // Estado general basado en las verificaciones
        if ("UP".equals(database.get("status")) && "UP".equals(system.get("status"))) {
            health.put("overall", "UP");
        } else {
            health.put("overall", "DOWN");
        }
        
        return health;
    }

    /**
     * Verifica la salud de la base de datos
     * @return Map con el estado de la base de datos
     */
    private Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> dbHealth = new HashMap<>();
        
        try {
            // Verificar conexión
            if (dataSource != null) {
                try (var connection = dataSource.getConnection()) {
                    if (connection.isValid(5)) {
                        dbHealth.put("status", "UP");
                        dbHealth.put("message", "Conexión a la base de datos exitosa");
                        
                        // Verificar que se puede ejecutar una consulta simple
                        try {
                            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                            dbHealth.put("query", "OK");
                        } catch (Exception e) {
                            dbHealth.put("query", "ERROR");
                            dbHealth.put("queryError", e.getMessage());
                        }
                    } else {
                        dbHealth.put("status", "DOWN");
                        dbHealth.put("message", "Conexión a la base de datos no válida");
                    }
                }
            } else {
                dbHealth.put("status", "DOWN");
                dbHealth.put("message", "DataSource no disponible");
            }
        } catch (Exception e) {
            dbHealth.put("status", "DOWN");
            dbHealth.put("message", "Error al conectar con la base de datos");
            dbHealth.put("error", e.getMessage());
        }
        
        return dbHealth;
    }

    /**
     * Verifica la salud del sistema
     * @return Map con el estado del sistema
     */
    private Map<String, Object> checkSystemHealth() {
        Map<String, Object> systemHealth = new HashMap<>();
        
        try {
            // Verificar memoria del sistema
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            long maxMemory = runtime.maxMemory();
            
            double memoryUsagePercent = ((double) usedMemory / maxMemory) * 100;
            
            systemHealth.put("status", "UP");
            systemHealth.put("memory", Map.of(
                "total", formatBytes(totalMemory),
                "used", formatBytes(usedMemory),
                "free", formatBytes(freeMemory),
                "max", formatBytes(maxMemory),
                "usagePercent", String.format("%.2f%%", memoryUsagePercent)
            ));
            
            // Verificar que el uso de memoria no sea crítico (>90%)
            if (memoryUsagePercent > 90) {
                systemHealth.put("status", "WARNING");
                systemHealth.put("message", "Uso de memoria crítico");
            } else if (memoryUsagePercent > 80) {
                systemHealth.put("status", "WARNING");
                systemHealth.put("message", "Uso de memoria alto");
            } else {
                systemHealth.put("message", "Sistema funcionando normalmente");
            }
            
        } catch (Exception e) {
            systemHealth.put("status", "DOWN");
            systemHealth.put("message", "Error al verificar el estado del sistema");
            systemHealth.put("error", e.getMessage());
        }
        
        return systemHealth;
    }

    /**
     * Formatea bytes en formato legible
     * @param bytes Bytes a formatear
     * @return String formateado
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
