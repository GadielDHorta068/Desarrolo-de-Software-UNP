/** @type {import('tailwindcss').Config} */
module.exports = {
  // Configuración para Angular - especifica dónde buscar clases de Tailwind
  content: [
    "./src/**/*.{html,ts}",
    "./src/**/*.component.{html,ts}",
    "./src/**/*.component.html",
    "./src/**/*.component.ts"
  ],
  // Configuración para modo oscuro que detecta automáticamente el tema del sistema
  darkMode: 'media',
  theme: {
    extend: {
      // Aquí puedes personalizar el tema de Tailwind
      colors: {
        // Ejemplo de colores personalizados
        'primary': '#3B82F6',
        'secondary': '#10B981',
        'accent': '#F59E0B',
      },
      fontFamily: {
        // Ejemplo de fuentes personalizadas
        'sans': ['Inter', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [
    // Aquí puedes agregar plugins de Tailwind si los necesitas
  ],
}

