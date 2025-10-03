# 🎨 Cómo Agregar Tu Logo Personalizado

## 📍 Ubicación del Logo

Coloca tu logo en esta carpeta:
```
backend/src/main/resources/assets/images/
```

## 🎯 Opciones Disponibles

### **Opción 1: Logo SVG (Recomendado)**
1. **Archivo**: `logo.svg`
2. **Ventajas**: Escalable, pequeño tamaño, mejor calidad
3. **Formato**: SVG válido
4. **Tamaño recomendado**: 150x50px

### **Opción 2: Logo PNG**
1. **Archivo**: `logo.png`
2. **Ventajas**: Mejor compatibilidad con clientes de email antiguos
3. **Formato**: PNG con transparencia
4. **Tamaño recomendado**: 150x50px

## 📝 Pasos para Agregar Tu Logo

### **Método 1: Reemplazar Archivo Existente**
1. Renombra tu archivo de logo a `logo.svg` o `logo.png`
2. Colócalo en: `backend/src/main/resources/assets/images/`
3. El sistema lo detectará automáticamente

### **Método 2: Crear Logo Personalizado**
1. Diseña tu logo con las dimensiones 150x50px
2. Guárdalo como SVG o PNG
3. Colócalo en la carpeta de assets
4. El sistema lo usará automáticamente

## 🔧 Configuración Automática

El sistema busca logos en este orden:
1. `logo.svg` (prioridad alta)
2. `logo.png` (fallback)
3. Logo generado programáticamente (último recurso)

## 📱 Consideraciones Técnicas

### **Para SVG:**
- Usa colores sólidos o gradientes simples
- Evita fuentes personalizadas (usa Arial, sans-serif)
- Mantén el tamaño en 150x50px
- Prueba en diferentes clientes de email

### **Para PNG:**
- Usa transparencia para mejor integración
- Optimiza el tamaño del archivo
- Mantén alta resolución (150x50px mínimo)

## 🚀 Ejemplo de Uso

Una vez que coloques tu logo, el sistema:
- ✅ Lo detectará automáticamente
- ✅ Lo convertirá a base64
- ✅ Lo incluirá en todos los emails
- ✅ Mantendrá el fallback si hay problemas

## 🎨 Diseño Recomendado

- **Colores**: Gradiente azul-púrpura (#4F46E5 a #7C3AED)
- **Tipografía**: Arial o sans-serif
- **Estilo**: Moderno, profesional
- **Elementos**: Logo + texto "Rafify" + tagline opcional

## 📧 Prueba

Después de agregar tu logo:
1. Reinicia la aplicación
2. Registra un nuevo usuario
3. Verifica que el logo aparezca en el email de bienvenida
