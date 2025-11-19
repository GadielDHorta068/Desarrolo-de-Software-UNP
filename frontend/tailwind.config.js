/** @type {import('tailwindcss').Config} */
module.exports = {
  // Configuración para Angular - especifica dónde buscar clases de Tailwind
  content: [
    "./src/**/*.{html,ts}",
    "./src/**/*.component.{html,ts}",
    "./src/**/*.component.html",
    "./src/**/*.component.ts"
  ],
  // Cambiamos a 'class' para que el toggle del tema pueda controlar el modo oscuro
  darkMode: 'class',
  safelist: ['bg-red-500', 'bg-green-500', 'text-white', 'dark'],
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
    
  ],
}

