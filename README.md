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

## Relación del proyecto con Bases de Datos

Aunque el proyecto no utiliza un sistema gestor de bases de datos (DBMS) de forma directa, **sí aplica conceptos fundamentales de bases de datos**, especialmente en la etapa de **preparación y calidad de los datos**.

### Preparación del dataset
El archivo CSV procesado puede considerarse una **tabla de base de datos**, donde cada fila representa un registro y cada columna un atributo.  
El proceso de limpieza garantiza que los datos cumplan condiciones necesarias para ser almacenados posteriormente en una base de datos relacional o NoSQL.

### Integridad de los datos
Durante la limpieza se aplican reglas de integridad similares a las utilizadas en bases de datos:
- Eliminación de registros con valores nulos en campos clave.
- Validación de dominios (por ejemplo, duración de películas entre 1 y 400 minutos).
- Conversión de tipos de datos a formatos consistentes.

### Normalización y consistencia
La normalización de textos y la limpieza de columnas complejas permiten:
- Reducir inconsistencias en los datos.
- Evitar duplicidad lógica (por ejemplo, géneros escritos de distintas formas).
- Facilitar procesos posteriores de normalización y diseño de esquemas.

### Columnas complejas y modelado
Columnas como `crew`, `cast` y `keywords` contienen información anidada similar a estructuras JSON.  
Su limpieza prepara los datos para:
- Separación en tablas independientes.
- Relaciones uno a muchos.
- Diseño de un modelo lógico más estructurado.

### Uso del dataset limpio
El archivo CSV limpio generado puede ser utilizado para:
- Cargar datos en una base de datos relacional (MySQL, PostgreSQL).
- Alimentar bases de datos NoSQL.
- Ejecutar consultas SQL sin problemas de integridad o formato.

En este sentido, el proyecto funciona como una **etapa previa esencial al diseño e implementación de una base de datos**, asegurando calidad, consistencia y confiabilidad de la información.


---

## Resultado final

Como resultado del proceso se obtiene un **dataset limpio, validado y consistente**, almacenado en un nuevo archivo CSV, listo para ser utilizado en análisis avanzados o procesos de modelado de datos.
