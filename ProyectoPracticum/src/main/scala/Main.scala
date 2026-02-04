import cats.effect.*
import cats.implicits.*
import data.{CSVReader, Database}
import services.MovieService
import models.*
import utilities.{Cleaners, _}
import io.circe.parser.*
import java.time.{LocalDate, LocalDateTime, Instant, ZoneId}
import scala.io.Source
import doobie.*
import doobie.implicits.*

object Main extends IOApp.Simple {

  val path = "src/main/resources/pi_movies_complete.csv"

  // Obtiene el valor de una columna de forma segura
  def getValue(row: List[String], index: Map[String, Int], column: String): Option[String] =
    index.get(column).flatMap(i => if (i < row.length) Some(row(i)) else None)

  // Analiza valores numéricos válidos
  def analyzeNumeric(rows: List[List[String]], index: Map[String, Int], column: String): Unit = {
    val values =
      rows.flatMap(r => getValue(r, index, column))
        .flatMap(Cleaners.toDouble)
        .filter(_ >= 0)

    println(s"\nColumna numérica: $column")
    if (values.nonEmpty) {
      println(s"Cantidad: ${values.size}")
      println(s"Mínimo: ${values.min}")
      println(s"Máximo: ${values.max}")
      println(s"Promedio: ${values.sum / values.size}")
    }
  }

  // Analiza frecuencia de valores de texto
  def analyzeText(rows: List[List[String]], index: Map[String, Int], column: String, top: Int = 10): Unit = {
    val values =
      rows.flatMap(r => getValue(r, index, column))
        .flatMap(Cleaners.cleanString)

    println(s"\nColumna texto: $column")
    values.groupBy(identity).view.mapValues(_.size).toList
      .sortBy(-_._2).take(top)
      .foreach { case (v, c) => println(f"$v%-20s -> $c") }
  }

  // Analiza fechas válidas
  def analyzeDate(rows: List[List[String]], index: Map[String, Int], column: String): Unit = {
    val dates =
      rows.flatMap(r => getValue(r, index, column))
        .flatMap(Cleaners.cleanDate)

    println(s"\nColumna fecha: $column")
    if (dates.nonEmpty) {
      println(s"Cantidad: ${dates.size}")
      println(s"Mínima: ${dates.min}")
      println(s"Máxima: ${dates.max}")
    }
  }

  // Analiza tamaño de arreglos JSON
  def analyzeJsonArray(rows: List[List[String]], index: Map[String, Int], column: String): Unit = {
    val sizes =
      rows.flatMap(r => getValue(r, index, column))
        .flatMap(Cleaners.jsonArraySize)

    println(s"\nJSON: $column")
    if (sizes.nonEmpty) {
      println(s"Promedio: ${sizes.sum.toDouble / sizes.size}")
      println(s"Máximo: ${sizes.max}")
    }
  }

  // Analiza frecuencia de campos dentro de JSON
  def analyzeJsonNames(
                        rows: List[List[String]],
                        index: Map[String, Int],
                        column: String,
                        field: String,
                        top: Int = 10
                      ): Unit = {

    val values =
      rows.flatMap(r => getValue(r, index, column))
        .flatMap(json => Cleaners.jsonNames(json, field))

    println(s"\nJSON $column -> $field")
    values.groupBy(identity).view.mapValues(_.size).toList
      .sortBy(-_._2).take(top)
      .foreach { case (v, c) => println(f"$v%-25s -> $c") }
  }

  // Analiza cantidad de miembros del crew
  def analyzeCrew(rows: List[List[String]], index: Map[String, Int]): Unit = {
    val crewSizes =
      rows.flatMap(r => getValue(r, index, "crew"))
        .map(_.split(",").length)

    println("\nColumna crew")
    println(s"Promedio: ${crewSizes.sum.toDouble / crewSizes.size}")
    println(s"Máximo: ${crewSizes.max}")
  }

  // Filtra filas con datos mínimos válidos
  def cleanRows(
                 rows: List[List[String]],
                 index: Map[String, Int]
               ): List[List[String]] = {

    rows.filter { r =>
      val idOk =
        getValue(r, index, "id").exists(_.trim.nonEmpty)

      val titleOk =
        getValue(r, index, "title")
          .flatMap(Cleaners.cleanString)
          .nonEmpty

      val budgetOk =
        getValue(r, index, "budget")
          .flatMap(Cleaners.toDouble)
          .exists(_ >= 0)

      val revenueOk =
        getValue(r, index, "revenue")
          .flatMap(Cleaners.toDouble)
          .exists(_ >= 0)

      idOk && titleOk && (budgetOk || revenueOk)
    }
  }


  // MÉTODOS DE PARSEO

  def parsearDatosCSV(csvPath: String): IO[(
    List[Genero],
      List[PalabraClave],
      List[Productora],
      List[Pais],
      List[Idioma],
      List[Persona],
      List[Coleccion],
      List[Pelicula],
      List[PeliculaGenero],
      List[PeliculaPalabraClave],
      List[PeliculaProductora],
      List[PeliculaPais],
      List[Reparto],
      List[EquipoTecnico],
      List[Calificacion]
    )] = IO {

    val lines = Source.fromFile(csvPath, "UTF-8").getLines().toList

    if (lines.isEmpty) {
      throw new Exception("CSV vacío")
    }

    val headerLine = lines.head
    val headers = headerLine.split(";").map(_.trim).toList
    val rows = lines.tail

    val generosMap = scala.collection.mutable.Map[Int, Genero]()
    val palabrasMap = scala.collection.mutable.Map[Int, PalabraClave]()
    val productorasMap = scala.collection.mutable.Map[Int, Productora]()
    val paisesMap = scala.collection.mutable.Map[String, Pais]()
    val idiomasMap = scala.collection.mutable.Map[String, Idioma]()
    val personasMap = scala.collection.mutable.Map[Int, Persona]()
    val coleccionesMap = scala.collection.mutable.Map[Int, Coleccion]()

    val peliculas = scala.collection.mutable.ListBuffer[Pelicula]()
    val peliculaGeneros = scala.collection.mutable.ListBuffer[PeliculaGenero]()
    val peliculaPalabras = scala.collection.mutable.ListBuffer[PeliculaPalabraClave]()
    val peliculaProductoras = scala.collection.mutable.ListBuffer[PeliculaProductora]()
    val peliculaPaises = scala.collection.mutable.ListBuffer[PeliculaPais]()
    val repartos = scala.collection.mutable.ListBuffer[Reparto]()
    val equipos = scala.collection.mutable.ListBuffer[EquipoTecnico]()
    val calificaciones = scala.collection.mutable.ListBuffer[Calificacion]()

    val colIndex = headers.zipWithIndex.toMap

    var rowsProcessed = 0
    var rowsSuccess = 0
    var calificacionId = 1

    rows.foreach { line =>
      rowsProcessed += 1
      val cols = line.split(";", -1).map(_.trim)

      try {
        val peliculaId = cols(colIndex("id")).toInt

        // PARSEAR GÉNEROS

        Cleaners.cleanString(cols(colIndex.getOrElse("genres", -1))).foreach { genresJson =>
          val fixed = Cleaners.fixJsonQuotes(genresJson)
          parse(fixed).toOption.flatMap(_.asArray).foreach { genresArray =>
            genresArray.foreach { genero =>
              val cursor = genero.hcursor
              for {
                id <- cursor.get[Int]("id").toOption
                nombre <- cursor.get[String]("name").toOption
              } {
                generosMap.getOrElseUpdate(id, Genero(id, nombre))
                peliculaGeneros += PeliculaGenero(peliculaId, id)
              }
            }
          }
        }

        // PARSEAR PALABRAS CLAVE (keywords)
        Cleaners.cleanString(cols(colIndex.getOrElse("keywords", -1))).foreach { keywordsJson =>
          val fixed = Cleaners.fixJsonQuotes(keywordsJson)
          parse(fixed).toOption.flatMap(_.asArray).foreach { keywordsArray =>
            keywordsArray.foreach { keyword =>
              val cursor = keyword.hcursor
              for {
                id <- cursor.get[Int]("id").toOption
                nombre <- cursor.get[String]("name").toOption
              } {
                palabrasMap.getOrElseUpdate(id, PalabraClave(id, nombre))
                peliculaPalabras += PeliculaPalabraClave(peliculaId, id)
              }
            }
          }
        }

        // PARSEAR PRODUCTORAS (production_companies)
        Cleaners.cleanString(cols(colIndex.getOrElse("production_companies", -1))).foreach { companiesJson =>
          val fixed = Cleaners.fixJsonQuotes(companiesJson)
          parse(fixed).toOption.flatMap(_.asArray).foreach { companiesArray =>
            companiesArray.foreach { company =>
              val cursor = company.hcursor
              for {
                id <- cursor.get[Int]("id").toOption
                nombre <- cursor.get[String]("name").toOption
              } {
                productorasMap.getOrElseUpdate(id, Productora(id, nombre))
                peliculaProductoras += PeliculaProductora(peliculaId, id)
              }
            }
          }
        }

        // PARSEAR PAÍSES (production_countries)
        Cleaners.cleanString(cols(colIndex.getOrElse("production_countries", -1))).foreach { countriesJson =>
          val fixed = Cleaners.fixJsonQuotes(countriesJson)
          parse(fixed).toOption.flatMap(_.asArray).foreach { countriesArray =>
            countriesArray.foreach { country =>
              val cursor = country.hcursor
              for {
                iso <- cursor.get[String]("iso_3166_1").toOption
                nombre <- cursor.get[String]("name").toOption
              } {
                paisesMap.getOrElseUpdate(iso, Pais(iso, nombre))
                peliculaPaises += PeliculaPais(peliculaId, iso)
              }
            }
          }
        }

        // PARSEAR IDIOMAS (spoken_languages)
        Cleaners.cleanString(cols(colIndex.getOrElse("spoken_languages", -1))).foreach { languagesJson =>
          val fixed = Cleaners.fixJsonQuotes(languagesJson)
          parse(fixed).toOption.flatMap(_.asArray).foreach { languagesArray =>
            languagesArray.foreach { language =>
              val cursor = language.hcursor
              for {
                iso <- cursor.get[String]("iso_639_1").toOption
                nombre <- cursor.get[String]("name").toOption
              } {
                idiomasMap.getOrElseUpdate(iso, Idioma(iso, nombre))
              }
            }
          }
        }

        // PARSEAR COLECCIÓN (belongs_to_collection)
        Cleaners.cleanString(cols(colIndex.getOrElse("belongs_to_collection", -1))).foreach { collectionJson =>
          val fixed = Cleaners.fixJsonQuotes(collectionJson)
          parse(fixed).toOption.foreach { collectionObj =>
            val cursor = collectionObj.hcursor
            for {
              id <- cursor.get[Int]("id").toOption
              nombre <- cursor.get[String]("name").toOption
            } {
              val poster = cursor.get[String]("poster_path").toOption
              val backdrop = cursor.get[String]("backdrop_path").toOption
              coleccionesMap.getOrElseUpdate(id, Coleccion(id, nombre, poster, backdrop))
            }
          }
        }

        // PARSEAR REPARTO (cast)
        Cleaners.cleanString(cols(colIndex.getOrElse("cast", -1))).foreach { castJson =>
          val fixed = Cleaners.fixJsonQuotes(castJson)
          parse(fixed).toOption.flatMap(_.asArray).foreach { castArray =>
            castArray.foreach { actor =>
              val cursor = actor.hcursor
              for {
                personaId <- cursor.get[Int]("id").toOption
                nombre <- cursor.get[String]("name").toOption
                personaje <- cursor.get[String]("character").toOption
              } {
                val genero = cursor.get[Int]("gender").toOption.map(_.toString)
                val rutaPerfil = cursor.get[String]("profile_path").toOption
                val orden = cursor.get[Int]("order").toOption
                val creditoId = cursor.get[String]("credit_id").toOption

                personasMap.getOrElseUpdate(personaId, Persona(personaId, nombre, genero, rutaPerfil))
                repartos += Reparto(peliculaId, personaId, personaje, orden, creditoId)
              }
            }
          }
        }


        // PARSEAR EQUIPO TÉCNICO (crew)
        Cleaners.cleanString(cols(colIndex.getOrElse("crew", -1))).foreach { crewJson =>
          val fixed = Cleaners.fixJsonQuotes(crewJson)
          parse(fixed).toOption.flatMap(_.asArray).foreach { crewArray =>
            crewArray.foreach { member =>
              val cursor = member.hcursor
              for {
                personaId <- cursor.get[Int]("id").toOption
                nombre <- cursor.get[String]("name").toOption
                cargo <- cursor.get[String]("job").toOption
              } {
                val genero = cursor.get[Int]("gender").toOption.map(_.toString)
                val rutaPerfil = cursor.get[String]("profile_path").toOption
                val departamento = cursor.get[String]("department").toOption
                val creditoId = cursor.get[String]("credit_id").toOption

                personasMap.getOrElseUpdate(personaId, Persona(personaId, nombre, genero, rutaPerfil))
                equipos += EquipoTecnico(peliculaId, personaId, cargo, departamento, creditoId)
              }
            }
          }
        }

        // PARSEAR CALIFICACIONES (ratings)
        // Formato: [{"userId":228943,"rating":3.0,"timestamp":1359432677},...]
        Cleaners.cleanString(cols(colIndex.getOrElse("ratings", -1))).foreach { ratingsJson =>
          val fixed = Cleaners.fixJsonQuotes(ratingsJson)
          parse(fixed).toOption.flatMap(_.asArray).foreach { ratingsArray =>
            ratingsArray.foreach { rating =>
              val cursor = rating.hcursor
              for {
                usuarioId <- cursor.get[Int]("userId").toOption
                puntuacion <- cursor.get[Double]("rating").toOption
                timestamp <- cursor.get[Long]("timestamp").toOption
              } {
                // Convertir timestamp Unix a LocalDateTime
                val marcaTiempo = LocalDateTime.ofInstant(
                  Instant.ofEpochSecond(timestamp),
                  ZoneId.systemDefault()
                )

                calificaciones += Calificacion(
                  calificacionId = calificacionId,
                  peliculaId = peliculaId,
                  usuarioId = usuarioId,
                  puntuacion = puntuacion,
                  marcaTiempo = marcaTiempo
                )
                calificacionId += 1
              }
            }
          }
        }

        // CREAR PELÍCULA

        val coleccionId = Cleaners.cleanString(cols(colIndex.getOrElse("belongs_to_collection", -1)))
          .flatMap { collectionJson =>
            val fixed = Cleaners.fixJsonQuotes(collectionJson)
            parse(fixed).toOption.flatMap { collectionObj =>
              collectionObj.hcursor.get[Int]("id").toOption
            }
          }

        val pelicula = Pelicula(
          peliculaId = peliculaId,
          imdbId = Cleaners.cleanString(cols(colIndex.getOrElse("imdb_id", -1))),
          titulo = cols(colIndex("title")),
          tituloOriginal = cols(colIndex.getOrElse("original_title", colIndex("title"))),
          resumen = Cleaners.cleanString(cols(colIndex.getOrElse("overview", -1))),
          eslogan = Cleaners.cleanString(cols(colIndex.getOrElse("tagline", -1))),
          duracion = Cleaners.toInt(cols(colIndex.getOrElse("runtime", -1))),
          fechaEstreno = Cleaners.cleanDate(cols(colIndex.getOrElse("release_date", -1))),
          presupuesto = Cleaners.toDouble(cols(colIndex.getOrElse("budget", -1))).map(BigDecimal(_)),
          ingresos = Cleaners.toDouble(cols(colIndex.getOrElse("revenue", -1))).map(BigDecimal(_)),
          popularidad = Cleaners.toDouble(cols(colIndex.getOrElse("popularity", -1))),
          promedioVotos = Cleaners.toDouble(cols(colIndex.getOrElse("vote_average", -1))),
          cantidadVotos = Cleaners.toInt(cols(colIndex.getOrElse("vote_count", -1))),
          estado = Cleaners.cleanString(cols(colIndex.getOrElse("status", -1))),
          paginaWeb = Cleaners.cleanString(cols(colIndex.getOrElse("homepage", -1))),
          rutaPoster = Cleaners.cleanString(cols(colIndex.getOrElse("poster_path", -1))),
          esVideo = cols(colIndex.getOrElse("video", -1)).toLowerCase == "true",
          esAdulto = cols(colIndex.getOrElse("adult", -1)).toLowerCase == "true",
          coleccionId = coleccionId
        )

        peliculas += pelicula
        rowsSuccess += 1

      } catch {
        case e: Exception =>
          if (rowsProcessed <= 3) {
            println(s"Error en fila $rowsProcessed: ${e.getMessage}")
          }
      }
    }

    println(s"\n✓ Resumen del parseo:")
    println(s"  - Filas totales: $rowsProcessed")
    println(s"  - Películas exitosas: $rowsSuccess")
    println(s"  - Géneros únicos: ${generosMap.size}")
    println(s"  - Palabras clave únicas: ${palabrasMap.size}")
    println(s"  - Productoras únicas: ${productorasMap.size}")
    println(s"  - Países únicos: ${paisesMap.size}")
    println(s"  - Idiomas únicos: ${idiomasMap.size}")
    println(s"  - Personas únicas: ${personasMap.size}")
    println(s"  - Colecciones únicas: ${coleccionesMap.size}")
    println(s"  - Reparto total: ${repartos.size}")
    println(s"  - Equipo técnico total: ${equipos.size}")

    (
      generosMap.values.toList,
      palabrasMap.values.toList,
      productorasMap.values.toList,
      paisesMap.values.toList,
      idiomasMap.values.toList,
      personasMap.values.toList,
      coleccionesMap.values.toList,
      peliculas.toList,
      peliculaGeneros.toList,
      peliculaPalabras.toList,
      peliculaProductoras.toList,
      peliculaPaises.toList,
      repartos.toList,
      equipos.toList,
      calificaciones.toList
    )
  }

  def commitTransaction(using xa: Transactor[IO]): IO[Unit] = {
    sql"COMMIT".update.run.transact(xa).void
  }

  def run: IO[Unit] = {

    // Carga el CSV en memoria
    CSVReader.load(path)
    val lines = CSVReader.rawLines.now

    if (lines.isEmpty) {
      println("CSV vacío")
      return IO.unit
    }

    // Separación de encabezados y filas
    val headers = lines.head.split(";").toList
    val rows = lines.tail.map(_.split(";").toList)
    val index = headers.zipWithIndex.toMap

    // Análisis de columnas numéricas
    analyzeNumeric(rows, index, "budget")
    analyzeNumeric(rows, index, "revenue")
    analyzeNumeric(rows, index, "popularity")

    // Análisis de columnas de texto
    analyzeText(rows, index, "original_language")
    analyzeText(rows, index, "status")

    // Análisis de fechas
    analyzeDate(rows, index, "release_date")

    // Análisis de estructuras JSON
    analyzeJsonArray(rows, index, "genres")
    analyzeJsonArray(rows, index, "production_companies")
    analyzeJsonNames(rows, index, "genres", "name")

    // Análisis de tripulación
    analyzeCrew(rows, index)

    // Limpieza de filas inválidas
    val cleanedRows = cleanRows(rows, index)

    println("\nResumen limpieza")
    println(s"Filas originales: ${rows.size}")
    println(s"Filas válidas: ${cleanedRows.size}")

    // Escritura del CSV limpio
    val cleanPath = "src/main/resources/pi_movies_clean.csv"
    CSVReader.write(cleanPath, headers, cleanedRows)

    println(s"CSV limpio generado en: $cleanPath")

    Database.transactor.use { implicit xa =>
      val service = new MovieService()

      for {
        _ <- IO.println("=" * 60)
        _ <- IO.println("SISTEMA DE CARGA DE PELÍCULAS A BASE DE DATOS")
        _ <- IO.println("=" * 60)

        _ <- IO.println("\n[1] Verificando conexión a la base de datos...")
        connected <- Database.checkConnection
        _ <- if (connected) IO.println("✓ Conexión exitosa")
        else IO.raiseError(new Exception("No se pudo conectar a la base de datos"))

        _ <- IO.println("\n[2] Parseando datos del CSV...")
        datos <- parsearDatosCSV(path)
        (generos, palabrasClave, productoras, paises, idiomas, personas, colecciones,
        peliculas, peliculaGeneros, peliculaPalabras, peliculaProductoras, peliculaPaises,
        repartos, equipos, calificaciones) = datos

        _ <- IO.println(s"\n✓ Datos parseados correctamente")
        _ <- IO.println(s"  - Géneros: ${generos.size}")
        _ <- IO.println(s"  - Palabras clave: ${palabrasClave.size}")
        _ <- IO.println(s"  - Productoras: ${productoras.size}")
        _ <- IO.println(s"  - Países: ${paises.size}")
        _ <- IO.println(s"  - Idiomas: ${idiomas.size}")
        _ <- IO.println(s"  - Personas: ${personas.size}")
        _ <- IO.println(s"  - Colecciones: ${colecciones.size}")
        _ <- IO.println(s"  - Películas: ${peliculas.size}")
        _ <- IO.println(s"  - Calificaciones: ${calificaciones.size}")

        _ <- IO.println("\n[3] Iniciando carga a base de datos...")

        _ <- service.cargaCompletaOptimizada(
          generos, palabrasClave, productoras, paises, idiomas, personas, colecciones,
          peliculas, peliculaGeneros, peliculaPalabras, peliculaProductoras, peliculaPaises,
          repartos, equipos, calificaciones
        )

        _ <- IO.println("\n[4] Confirmando transacciones...")
        _ <- commitTransaction
        _ <- IO.println("✓ Transacciones confirmadas")

        _ <- IO.println("\n[5] Verificando datos insertados en la base de datos...")
        _ <- IO.println("=" * 60)

        _ <- IO.println("\n CATÁLOGOS:")
        totalGeneros <- sql"SELECT COUNT(*) FROM Genero".query[Int].unique.transact(xa)
        _ <- IO.println(s"  ✓ Géneros: $totalGeneros")

        totalPalabras <- sql"SELECT COUNT(*) FROM PalabraClave".query[Int].unique.transact(xa)
        _ <- IO.println(s"  ✓ Palabras Clave: $totalPalabras")

        totalProductoras <- sql"SELECT COUNT(*) FROM Productora".query[Int].unique.transact(xa)
        _ <- IO.println(s"  ✓ Productoras: $totalProductoras")

        totalPaises <- sql"SELECT COUNT(*) FROM Pais".query[Int].unique.transact(xa)
        _ <- IO.println(s"  ✓ Países: $totalPaises")

        totalIdiomas <- sql"SELECT COUNT(*) FROM Idioma".query[Int].unique.transact(xa)
        _ <- IO.println(s"  ✓ Idiomas: $totalIdiomas")

        totalPersonas <- sql"SELECT COUNT(*) FROM Persona".query[Int].unique.transact(xa)
        _ <- IO.println(s"  ✓ Personas: $totalPersonas")

        totalColecciones <- sql"SELECT COUNT(*) FROM Coleccion".query[Int].unique.transact(xa)
        _ <- IO.println(s"  ✓ Colecciones: $totalColecciones")

        _ <- IO.println("\n PELÍCULAS:")
        totalPeliculas <- sql"SELECT COUNT(*) FROM Pelicula".query[Int].unique.transact(xa)
        _ <- IO.println(s"  ✓ Total de Películas: $totalPeliculas")

        _ <- IO.println("\n RELACIONES:")
        totalPeliculaGenero <- sql"SELECT COUNT(*) FROM PeliculaGenero".query[Int].unique.transact(xa)
        _ <- IO.println(s"  ✓ Película-Género: $totalPeliculaGenero")

        totalPeliculaPalabra <- sql"SELECT COUNT(*) FROM PeliculaPalabraClave".query[Int].unique.transact(xa)
        _ <- IO.println(s"  ✓ Película-Palabras Clave: $totalPeliculaPalabra")

        totalPeliculaProductora <- sql"SELECT COUNT(*) FROM PeliculaProductora".query[Int].unique.transact(xa)
        _ <- IO.println(s"  ✓ Película-Productoras: $totalPeliculaProductora")

        totalPeliculaPais <- sql"SELECT COUNT(*) FROM PeliculaPais".query[Int].unique.transact(xa)
        _ <- IO.println(s"  ✓ Película-Países: $totalPeliculaPais")

        _ <- IO.println("\n REPARTO Y EQUIPO:")
        totalReparto <- sql"SELECT COUNT(*) FROM Reparto".query[Int].unique.transact(xa)
        _ <- IO.println(s"  ✓ Reparto: $totalReparto")

        totalEquipo <- sql"SELECT COUNT(*) FROM EquipoTecnico".query[Int].unique.transact(xa)
        _ <- IO.println(s"  ✓ Equipo Técnico: $totalEquipo")

        _ <- IO.println("\n CALIFICACIONES:")
        totalCalificaciones <- sql"SELECT COUNT(*) FROM Calificacion".query[Int].unique.transact(xa)
        _ <- IO.println(s"  ✓ Calificaciones: $totalCalificaciones")

        _ <- IO.println("\n" + "=" * 60)
        _ <- IO.println("✓ PROCESO COMPLETADO EXITOSAMENTE")
        _ <- IO.println("=" * 60)

      } yield ()
    }.handleErrorWith { error =>
      IO.println(s"\nERROR: ${error.getMessage}") *>
        IO.println(s"\nDetalles: ${error.getStackTrace.take(5).mkString("\n")}") *>
        IO.raiseError(error)
    }
  }
}