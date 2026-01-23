# Análisis y Limpieza de Dataset de Películas (Scala)

Proyecto académico desarrollado en **Scala**, orientado a la **limpieza, validación y análisis básico de un dataset de películas en formato CSV**.  
El sistema utiliza **procesamiento por streaming** mediante **Cats Effect** y **FS2**, lo que permite trabajar eficientemente con archivos de gran tamaño sin cargarlos completamente en memoria.

---

## Tecnologías utilizadas

- **Scala**
- **Cats Effect**
- **FS2**
- **fs2-data-csv**

Estas herramientas permiten aplicar principios de **programación funcional** y procesar grandes volúmenes de datos de forma eficiente.

---

## Objetivos del proyecto

- Leer un archivo CSV de películas.
- Validar campos críticos del dataset.
- Limpiar datos textuales y numéricos inconsistentes.
- Manejar columnas con estructuras complejas como `crew`, `cast` y `keywords`.
- Procesar el archivo mediante streaming para optimizar el uso de memoria.
- Generar un nuevo archivo CSV limpio.
- Obtener estadísticas básicas del dataset.

---

## Proceso de limpieza de datos

### Validaciones aplicadas

Se eliminan registros que no cumplen con las siguientes condiciones:

- Campos obligatorios no nulos:
  - `title`
  - `genre`
  - `runtime`
  - `crew`
- El campo `runtime` debe:
  - Ser numérico.
  - Tener un valor entre **1 y 400 minutos**.

---

### Transformaciones realizadas

- Normalización de texto:
  - Conversión a minúsculas.
  - Eliminación de espacios innecesarios.
- Limpieza de columnas con estructura tipo JSON:
  - `crew`
  - `cast`
  - `keywords`
- Conversión segura de valores numéricos (`runtime`, `budget`, `revenue`).
- Procesamiento del archivo CSV mediante **streams**, evitando cargar el archivo completo en memoria.

---

## Manejo de columnas complejas

Las columnas `crew`, `cast` y `keywords` contenían información no estructurada con caracteres especiales como corchetes, llaves y comillas.  
Durante el proceso de limpieza, estos caracteres fueron eliminados, dejando el contenido en un **formato de texto limpio, legible y consistente**, adecuado para análisis posteriores.

---

## Análisis realizado

- Cálculo de la **frecuencia de géneros** presentes en el dataset.
- Visualización de los **10 géneros más comunes**.
- Generación de un **nuevo archivo CSV limpio**, listo para análisis estadístico o modelado de datos.

---

## Estructura del proyecto

### AnalisisYLimpieza
Módulo encargado de:
- Lectura del archivo CSV.
- Validación de filas.
- Limpieza de datos.
- Análisis de frecuencia de géneros.
- Generación del archivo CSV limpio.

---

### DataUtils
Contiene funciones utilitarias reutilizables para:
- Limpieza de texto.
- Limpieza de columnas con estructura tipo JSON.
- Conversión segura de valores numéricos.
- Validación general de filas.

Este módulo favorece la **modularidad** y el **reuso de código**.

---

### MovieCleanerService
Servicio responsable de:
- Aplicar la limpieza completa a cada fila del CSV.
- Validar los registros antes de procesarlos.
- Generar el stream de datos limpios.
- Guardar el archivo CSV final.

Permite separar claramente la **lógica de negocio** del punto de entrada del programa.

---

### Main
Punto de entrada de la aplicación.
- Inicia el proceso de limpieza.
- Ejecuta el servicio principal.
- Muestra mensajes de estado al usuario.

---

## Enfoque de programación funcional

El proyecto aplica programación funcional mediante:

- Uso de **streams inmutables**.
- Funciones puras para validación y limpieza de datos.
- Composición de operaciones (`map`, `filter`, `fold`).
- Manejo explícito de efectos con `IO`.
- Separación clara de responsabilidades.

Este enfoque permite obtener un código **más claro, seguro, escalable y fácil de mantener**.

---

## Resultado final

Como resultado del proceso se obtiene un **dataset limpio, validado y consistente**, almacenado en un nuevo archivo CSV, listo para ser utilizado en análisis avanzados o procesos de modelado de datos.
