# PROYECTO-PRACTICUM
# Análisis y Limpieza de Dataset de Películas (Scala)

Proyecto académico desarrollado en **Scala** utilizando **Cats Effect** y **FS2** para el análisis y limpieza de un archivo CSV de películas, aplicando procesamiento por *streaming* para manejar archivos de gran tamaño de forma eficiente.

---

## Tecnologías utilizadas

- Scala
- Cats Effect
- FS2
- fs2-data-csv

Estas herramientas permiten procesar archivos CSV grandes sin cargarlos completamente en memoria.

---

## Objetivos del proyecto

- Leer un archivo CSV de películas
- Limpiar datos textuales y numéricos
- Manejar correctamente la columna `crew`
- Validar datos inconsistentes
- Generar un nuevo archivo CSV limpio
- Obtener estadísticas básicas del dataset

---

## Proceso de limpieza de datos

### Validaciones aplicadas
- Eliminación de registros con valores nulos en:
  - `title`
  - `genre`
  - `runtime`
  - `crew`
- Validación del campo `runtime` (valores entre 1 y 400 minutos)

### Transformaciones realizadas
- Normalización del texto del género
- Limpieza de la columna `crew`
- Procesamiento del archivo mediante streaming (chunks)

---

## Manejo de la columna `crew`

La columna `crew` contenía información no estructurada con caracteres especiales como corchetes, llaves y comillas.  
Para facilitar su análisis, se realizó una limpieza eliminando dichos caracteres, dejando el contenido en un formato más legible y consistente.

---

## Análisis realizado

- Cálculo de la frecuencia de géneros
- Visualización de los 10 géneros más comunes
- Generación de un nuevo archivo CSV limpio listo para análisis posteriores

## Código de Análisis y Limpieza del Dataset

A continuación se muestra el código principal del proyecto, desarrollado en **Scala** utilizando **Cats Effect** y **FS2**, el cual permite realizar la lectura, limpieza y análisis básico de un archivo CSV de películas mediante procesamiento por *streaming*.

```scala
import cats.effect.{IO, IOApp}
import fs2._
import fs2.data.csv._
import fs2.io.file.{Files, Path}

/**
 * Librerías: Cats Effect, FS2, fs2-data-csv
 *
 * El programa procesa un archivo CSV grande sin cargarlo
 * completamente en memoria, aplicando validaciones y limpieza
 * de datos, incluyendo el manejo de la columna crew.
 */
object AnalisisYLimpieza extends IOApp.Simple {

  // ===============================
  // RUTAS DE ARCHIVOS
  // ===============================

  // Archivo CSV original
  val inputPath: Path = Path("data/pi_movies_complete.csv")

  // Archivo CSV limpio
  val outputPath: Path = Path("data/pi_movies_limpio.csv")

  // ===============================
  // FUNCIONES DE UTILIDAD
  // ===============================

  // Limpia texto eliminando espacios y pasando a minúsculas
  def limpiarTexto(s: String): String =
    s.trim.toLowerCase

  // Limpia la columna crew eliminando caracteres especiales
  def limpiarCrew(crew: String): String =
    crew
      .replace("[", "")
      .replace("]", "")
      .replace("{", "")
      .replace("}", "")
      .replace("\"", "")
      .trim

  // Verifica que ciertas columnas no tengan valores nulos
  def sinNulos(row: Row[String], columnas: Set[String]): Boolean =
    columnas.forall(col => row.get(col).exists(_.nonEmpty))

  // Valida que el runtime sea un número válido
  def runtimeValido(row: Row[String]): Boolean =
    row.get("runtime")
      .flatMap(_.toIntOption)
      .exists(runtime => runtime > 0 && runtime <= 400)

  // ===============================
  // ANÁLISIS DE FRECUENCIA
  // ===============================

  // Calcula la frecuencia de valores de una columna de texto
  def frecuenciaTexto(columna: String): IO[Map[String, Long]] =
    Files[IO]
      .readAll(inputPath)
      .through(text.utf8.decode)
      .through(csv.rows[IO, String]())
      .map(_.get(columna))
      .collect {
        case Some(valor) if valor.nonEmpty =>
          limpiarTexto(valor)
      }
      .fold(Map.empty[String, Long]) { (acum, valor) =>
        acum.updated(valor, acum.getOrElse(valor, 0L) + 1)
      }
      .compile
      .lastOrError

  // ===============================
  // LIMPIEZA DEL DATASET
  // ===============================

  // Stream que limpia y transforma el dataset
  def limpiarDatos: Stream[IO, Row[String]] =
    Files[IO]
      .readAll(inputPath)
      .through(text.utf8.decode)
      .through(csv.rows[IO, String]())
      .filter(row => sinNulos(row, Set("title", "genre", "runtime", "crew")))
      .filter(runtimeValido)
      .map { row =>
        row
          .updated("genre", limpiarTexto(row("genre")))
          .updated("crew", limpiarCrew(row("crew")))
      }

  // Guarda el archivo CSV limpio
  def guardarCSV: IO[Unit] =
    limpiarDatos
      .through(csv.render[String]())
      .through(text.utf8.encode)
      .through(Files[IO].writeAll(outputPath))
      .compile
      .drain

  // ===============================
  // MÉTODO PRINCIPAL
  // ===============================

  // Ejecuta el análisis y la limpieza del dataset
  def run: IO[Unit] =
    for {
      freqGenero <- frecuenciaTexto("genre")
      _ <- IO.println("Top 10 géneros más frecuentes:")
      _ <- IO.println(freqGenero.toSeq.sortBy(-_._2).take(10))
      _ <- guardarCSV
    } yield ()
}
```
---
## Contexto del Proceso de Limpieza de Datos

En este proyecto se realizó un proceso de **limpieza completa del dataset de películas** a partir de un archivo CSV original que contenía datos inconsistentes, valores nulos y columnas con estructuras complejas.

El trabajo incluyó la **validación de campos esenciales** como el título y la duración de las películas, asegurando que los valores numéricos sean coherentes y estén dentro de rangos válidos. Además, se normalizaron columnas de texto para mantener consistencia en el formato de los datos.

Un aspecto importante del proceso fue el **manejo de columnas con información anidada**, como *Crew*, *Cast* y *Keywords*, las cuales contenían datos con estructuras similares a JSON. Estas columnas fueron limpiadas eliminando caracteres innecesarios, permitiendo que la información sea tratada como texto limpio para futuros análisis.

Finalmente, los datos procesados fueron almacenados en un nuevo archivo CSV limpio, garantizando un dataset más confiable, consistente y listo para ser utilizado en análisis posteriores o modelado de datos.

---

## UTILIDADES DE LIMPIEZA
```scala
import fs2.data.csv.Row

object DataUtils {

  // Limpia texto general
  def limpiarTexto(s: String): String =
    s.trim.toLowerCase

  // Limpia texto opcional
  def limpiarTextoOpt(opt: Option[String]): Option[String] =
    opt.map(_.trim).filter(_.nonEmpty)

  // Limpia columnas con estructura tipo JSON (crew, cast, keywords)
  def limpiarJsonLike(s: String): String =
    s.replace("[", "")
     .replace("]", "")
     .replace("{", "")
     .replace("}", "")
     .replace("\"", "")
     .trim

  // Conversión segura a Int
  def toIntOpt(s: String): Option[Int] =
    s.toIntOption.filter(_ > 0)

  // Conversión segura a Double
  def toDoubleOpt(s: String): Option[Double] =
    s.toDoubleOption.filter(_ >= 0)

  // Validación general de una fila
  def filaValida(row: Row[String]): Boolean =
    row.get("title").exists(_.nonEmpty) &&
    row.get("runtime").flatMap(toIntOpt).exists(_ <= 400)
}
```
## SERVICIO DE LIMPIEZA DEL CSV

```scala
import cats.effect.IO
import fs2._
import fs2.data.csv._
import fs2.io.file.{Files, Path}
import DataUtils._

object MovieCleanerService {

  // Rutas de archivos
  val inputPath: Path  = Path("data/pi_movies_complete.csv")
  val outputPath: Path = Path("data/pi_movies_clean.csv")

  // Limpieza de una fila completa
  def limpiarFila(row: Row[String]): Row[String] = {

    val runtimeClean  = row.get("runtime").flatMap(toIntOpt).map(_.toString)
    val budgetClean   = row.get("budget").flatMap(toDoubleOpt).map(_.toString)
    val revenueClean  = row.get("revenue").flatMap(toDoubleOpt).map(_.toString)

    val genreClean    = row.get("genre").map(limpiarTexto)
    val crewClean     = row.get("crew").map(limpiarJsonLike)
    val castClean     = row.get("cast").map(limpiarJsonLike)
    val keywordsClean = row.get("keywords").map(limpiarJsonLike)

    row
      .updated("runtime", runtimeClean.getOrElse(""))
      .updated("budget", budgetClean.getOrElse(""))
      .updated("revenue", revenueClean.getOrElse(""))
      .updated("genre", genreClean.getOrElse(""))
      .updated("crew", crewClean.getOrElse(""))
      .updated("cast", castClean.getOrElse(""))
      .updated("keywords", keywordsClean.getOrElse(""))
  }

  // Stream principal de limpieza
  def limpiarCSV: Stream[IO, Row[String]] =
    Files[IO]
      .readAll(inputPath)
      .through(text.utf8.decode)
      .through(csv.rows[IO, String]())
      .filter(filaValida)
      .map(limpiarFila)

  // Guardar CSV limpio
  def guardarCSV: IO[Unit] =
    limpiarCSV
      .through(csv.render[String]())
      .through(text.utf8.encode)
      .through(Files[IO].writeAll(outputPath))
      .compile
      .drain
}

```

## PROGRAMA PRINCIPAL

```scala
import cats.effect.{IO, IOApp}
import MovieCleanerService._

object Main extends IOApp.Simple {

  def run: IO[Unit] =
    for {
      _ <- IO.println("Iniciando limpieza completa del dataset...")
      _ <- guardarCSV
      _ <- IO.println("Limpieza finalizada. Archivo CSV limpio generado.")
    } yield ()
}

