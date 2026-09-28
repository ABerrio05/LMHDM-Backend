# Cierre visual del frontend — NEXO

Esta lista no cambia ningún endpoint ni modelo del backend. Su objetivo es que la
demo de Sprint 1 se perciba como una aplicación terminada y sea fácil de usar.

## Pantallas que deben quedar listas

- Login: título claro, campos con etiquetas, botón visible, enlace a registro y
  mensaje comprensible cuando las credenciales fallen.
- Registro: nombre, correo y contraseña; validación visual para campos vacíos,
  correo inválido y error de correo duplicado.
- Tareas: estado vacío cuando no haya tareas, formulario de creación accesible,
  prioridad identificable, acción de completar y retroalimentación después de
  crear, actualizar o eliminar.
- Recordatorios: visibles dentro de su tarea, formulario con fecha/hora y una
  acción clara para eliminarlos.

## Criterios de presentación

1. Mantener una paleta y tipografía consistentes entre login, registro y tareas.
2. Mostrar carga mientras se consulta la API y mensajes de error sin exponer
   detalles técnicos ni URLs.
3. La primera tarea debe poder crearse en máximo tres acciones después de iniciar
   sesión.
4. Comprobar visualmente en navegador de escritorio y una anchura móvil
   aproximada (360 px) antes de la demo.
5. No cambiar la URL base, encabezados CORS ni los nombres de campos definidos en
   `FRONTEND_INTEGRATION.md` sin acordarlo con backend.

## Evidencia a entregar

- Captura de login/registro.
- Captura de lista de tareas con una tarea completada.
- Captura de recordatorio creado en una tarea.
- Una captura de ancho móvil o PWA instalada, si aplica.
