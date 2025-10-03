# Assets para Emails

## 📁 Estructura de Carpetas

```
backend/src/main/resources/assets/
├── images/
│   ├── logo.svg          # Logo principal de Rafify (recomendado)
│   ├── logo.png          # Logo en formato PNG (alternativo)
│   ├── logo-small.svg    # Logo pequeño para emails
│   └── README.md         # Este archivo
```

## 🎨 Formatos Recomendados

### **Logo Principal (logo.svg)**
- **Formato**: SVG (escalable, mejor calidad)
- **Tamaño**: 150x50px o similar
- **Colores**: Gradiente azul-púrpura (#4F46E5 a #7C3AED)
- **Uso**: Header de emails, branding principal

### **Logo PNG (logo.png)**
- **Formato**: PNG con transparencia
- **Tamaño**: 150x50px
- **Uso**: Fallback si SVG no funciona

## 📝 Cómo Agregar Tu Logo

### **Opción 1: Reemplazar el Logo SVG**
1. Coloca tu archivo `logo.svg` en esta carpeta
2. El sistema lo detectará automáticamente
3. Debe ser un SVG válido con las dimensiones correctas

### **Opción 2: Usar Logo PNG**
1. Coloca tu archivo `logo.png` en esta carpeta
2. Actualiza el código para usar PNG en lugar de SVG
3. Mejor compatibilidad con clientes de email

### **Opción 3: Logo Base64**
1. Convierte tu logo a base64
2. Reemplaza el método `createSimpleLogoBase64()` en EmailTemplateService
3. Máxima compatibilidad

## 🔧 Configuración en Código

El sistema busca automáticamente estos archivos:
- `logo.svg` (prioridad alta)
- `logo.png` (fallback)
- Logo generado programáticamente (último recurso)

## 📱 Consideraciones de Email

- **Clientes de email** pueden bloquear imágenes externas
- **SVG** funciona en la mayoría de clientes modernos
- **PNG** tiene mejor compatibilidad con clientes antiguos
- **Base64** siempre funciona pero aumenta el tamaño del email
