package repositories

import cats.effect.*
import cats.implicits.*
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import doobie.implicits.javatimedrivernative.*
import models.*
import java.time.LocalDate

object MovieRepository {

  // DOOBIE META INSTANCES

  // Meta instances para tipos personalizados si es necesario
  implicit val bigDecimalMeta: Meta[BigDecimal] =
    Meta[java.math.BigDecimal].imap(BigDecimal(_))(_.bigDecimal)

  // Read instance para Pelicula
  implicit val peliculaRead: Read[Pelicula] = Read[
    (Int, Option[String], String, String, Option[String], Option[String],
      Option[Int], Option[LocalDate], Option[BigDecimal], Option[BigDecimal], Option[Double],
      Option[Double], Option[Int], Option[String], Option[String], Option[String],
      Boolean, Boolean, Option[Int])
  ].map { case (
    peliculaId, imdbId, titulo, tituloOriginal, resumen, eslogan,
    duracion, fechaEstreno, presupuesto, ingresos, popularidad,
    promedioVotos, cantidadVotos, estado, paginaWeb, rutaPoster,
    esVideo, esAdulto, coleccionId
    ) =>
    Pelicula(
      peliculaId, imdbId, titulo, tituloOriginal, resumen, eslogan,
      duracion, fechaEstreno, presupuesto, ingresos, popularidad,
      promedioVotos, cantidadVotos, estado, paginaWeb, rutaPoster,
      esVideo, esAdulto, coleccionId
    )
  }

  // Read instance para Genero
  implicit val generoRead: Read[Genero] = Read[(Int, String)].map {
    case (id, nombre) => Genero(id, nombre)
  }

  // INSERCIONES OPTIMIZADAS CON BATCH

  // Generos - Batch Insert
  def insertGeneros(generos: List[Genero]): ConnectionIO[Int] = {
    val sql = "INSERT IGNORE INTO Genero (genero_id, nombre) VALUES (?, ?)"
    Update[(Int, String)](sql).updateMany(
      generos.map(g => (g.generoId, g.nombre))
    )
  }

  // Palabras Clave - Batch Insert
  def insertPalabrasClave(palabras: List[PalabraClave]): ConnectionIO[Int] = {
    val sql = "INSERT IGNORE INTO PalabraClave (palabra_clave_id, nombre) VALUES (?, ?)"
    Update[(Int, String)](sql).updateMany(
      palabras.map(p => (p.palabraClaveId, p.nombre))
    )
  }

  // Productoras - Batch Insert
  def insertProductoras(productoras: List[Productora]): ConnectionIO[Int] = {
    val sql = "INSERT IGNORE INTO Productora (empresa_id, nombre) VALUES (?, ?)"
    Update[(Int, String)](sql).updateMany(
      productoras.map(p => (p.empresaId, p.nombre))
    )
  }

  // Países - Batch Insert
  def insertPaises(paises: List[Pais]): ConnectionIO[Int] = {
    val sql = "INSERT IGNORE INTO Pais (codigo_iso_3166, nombre) VALUES (?, ?)"
    Update[(String, String)](sql).updateMany(
      paises.map(p => (p.codigoIso3166, p.nombre))
    )
  }

  // Idiomas - Batch Insert
  def insertIdiomas(idiomas: List[Idioma]): ConnectionIO[Int] = {
    val sql = "INSERT IGNORE INTO Idioma (codigo_iso_639_1, nombre) VALUES (?, ?)"
    Update[(String, String)](sql).updateMany(
      idiomas.map(i => (i.codigoIso639, i.nombre))
    )
  }

  // Personas - Batch Insert
  def insertPersonas(personas: List[Persona]): ConnectionIO[Int] = {
    val sql = """
      INSERT IGNORE INTO Persona (persona_id, nombre, genero, ruta_perfil) 
      VALUES (?, ?, ?, ?)
    """
    Update[(Int, String, Option[String], Option[String])](sql).updateMany(
      personas.map(p => (p.personaId, p.nombre, p.genero, p.rutaPerfil))
    )
  }

  // Colecciones - Batch Insert
  def insertColecciones(colecciones: List[Coleccion]): ConnectionIO[Int] = {
    val sql = """
      INSERT IGNORE INTO Coleccion (coleccion_id, nombre, ruta_poster, ruta_fondo)
      VALUES (?, ?, ?, ?)
    """
    Update[(Int, String, Option[String], Option[String])](sql).updateMany(
      colecciones.map(c => (c.coleccionId, c.nombre, c.rutaPoster, c.rutaFondo))
    )
  }

  // Películas - Batch Insert (Optimizado)
  def insertPeliculas(peliculas: List[Pelicula]): ConnectionIO[Int] = {
    val sql = """
      INSERT IGNORE INTO Pelicula (
        pelicula_id, imdb_id, titulo, titulo_original, resumen, eslogan,
        duracion, fecha_estreno, presupuesto, ingresos, popularidad,
        promedio_votos, cantidad_votos, estado, pagina_web, ruta_poster,
        es_video, es_adulto, coleccion_id
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """

    type PeliculaTuple = (
      Int, Option[String], String, String, Option[String], Option[String],
        Option[Int], Option[LocalDate], Option[BigDecimal], Option[BigDecimal], Option[Double],
        Option[Double], Option[Int], Option[String], Option[String], Option[String],
        Boolean, Boolean, Option[Int]
      )

    Update[PeliculaTuple](sql).updateMany(
      peliculas.map(p => (
        p.peliculaId, p.imdbId, p.titulo, p.tituloOriginal, p.resumen, p.eslogan,
        p.duracion, p.fechaEstreno, p.presupuesto, p.ingresos, p.popularidad,
        p.promedioVotos, p.cantidadVotos, p.estado, p.paginaWeb, p.rutaPoster,
        p.esVideo, p.esAdulto, p.coleccionId
      ))
    )
  }

  // RELACIONES - BATCH INSERT

  def insertPeliculaGeneros(relaciones: List[PeliculaGenero]): ConnectionIO[Int] = {
    val sql = "INSERT IGNORE INTO PeliculaGenero (pelicula_id, genero_id) VALUES (?, ?)"
    Update[(Int, Int)](sql).updateMany(
      relaciones.map(r => (r.peliculaId, r.generoId))
    )
  }

  def insertPeliculaPalabrasClave(relaciones: List[PeliculaPalabraClave]): ConnectionIO[Int] = {
    val sql = "INSERT IGNORE INTO PeliculaPalabraClave (pelicula_id, palabra_clave_id) VALUES (?, ?)"
    Update[(Int, Int)](sql).updateMany(
      relaciones.map(r => (r.peliculaId, r.palabraClaveId))
    )
  }

  def insertPeliculaProductoras(relaciones: List[PeliculaProductora]): ConnectionIO[Int] = {
    val sql = "INSERT IGNORE INTO PeliculaProductora (pelicula_id, empresa_id) VALUES (?, ?)"
    Update[(Int, Int)](sql).updateMany(
      relaciones.map(r => (r.peliculaId, r.empresaId))
    )
  }

  def insertPeliculaPaises(relaciones: List[PeliculaPais]): ConnectionIO[Int] = {
    val sql = "INSERT IGNORE INTO PeliculaPais (pelicula_id, codigo_iso_3166) VALUES (?, ?)"
    Update[(Int, String)](sql).updateMany(
      relaciones.map(r => (r.peliculaId, r.codigoIso3166))
    )
  }

  def insertRepartos(repartos: List[Reparto]): ConnectionIO[Int] = {
    val sql = """
      INSERT IGNORE INTO Reparto (pelicula_id, persona_id, personaje, orden_aparicion, credito_id)
      VALUES (?, ?, ?, ?, ?)
    """
    Update[(Int, Int, String, Option[Int], Option[String])](sql).updateMany(
      repartos.map(r => (r.peliculaId, r.personaId, r.personaje, r.ordenAparicion, r.creditoId))
    )
  }

  def insertEquipoTecnico(equipos: List[EquipoTecnico]): ConnectionIO[Int] = {
    val sql = """
      INSERT IGNORE INTO EquipoTecnico (pelicula_id, persona_id, cargo, departamento, credito_id)
      VALUES (?, ?, ?, ?, ?)
    """
    Update[(Int, Int, String, Option[String], Option[String])](sql).updateMany(
      equipos.map(e => (e.peliculaId, e.personaId, e.cargo, e.departamento, e.creditoId))
    )
  }

  // Calificaciones - Batch Insert
  def insertCalificaciones(calificaciones: List[Calificacion]): ConnectionIO[Int] = {
    val sql = """
      INSERT IGNORE INTO Calificacion (calificacion_id, pelicula_id, usuario_id, puntuacion, marca_tiempo)
      VALUES (?, ?, ?, ?, ?)
    """
    Update[(Int, Int, Int, Double, java.time.LocalDateTime)](sql).updateMany(
      calificaciones.map(c => (c.calificacionId, c.peliculaId, c.usuarioId, c.puntuacion, c.marcaTiempo))
    )
  }

  // INSERCIÓN CON CHUNKING

  def insertBatch[A](
                      items: List[A],
                      batchSize: Int = 100
                    )(insertFn: List[A] => ConnectionIO[Int]): ConnectionIO[Int] = {
    items
      .grouped(batchSize)
      .toList
      .traverse(chunk => insertFn(chunk))
      .map(_.sum)
  }

  // QUERIES DE CONSULTA

  def getPeliculaById(id: Int): ConnectionIO[Option[Pelicula]] =
    sql"""
      SELECT pelicula_id, imdb_id, titulo, titulo_original, resumen, eslogan,
             duracion, fecha_estreno, presupuesto, ingresos, popularidad,
             promedio_votos, cantidad_votos, estado, pagina_web, ruta_poster,
             es_video, es_adulto, coleccion_id
      FROM Pelicula 
      WHERE pelicula_id = $id
    """.query[Pelicula].option

  def getAllPeliculas(limit: Int = 100): ConnectionIO[List[Pelicula]] =
    sql"""
      SELECT pelicula_id, imdb_id, titulo, titulo_original, resumen, eslogan,
             duracion, fecha_estreno, presupuesto, ingresos, popularidad,
             promedio_votos, cantidad_votos, estado, pagina_web, ruta_poster,
             es_video, es_adulto, coleccion_id
      FROM Pelicula 
      LIMIT $limit
    """.query[Pelicula].to[List]

  def getGenerosByPeliculaId(peliculaId: Int): ConnectionIO[List[Genero]] =
    sql"""
      SELECT g.genero_id, g.nombre
      FROM Genero g
      INNER JOIN PeliculaGenero pg ON g.genero_id = pg.genero_id
      WHERE pg.pelicula_id = $peliculaId
    """.query[Genero].to[List]

  def countPeliculas: ConnectionIO[Int] =
    sql"SELECT COUNT(*) FROM Pelicula".query[Int].unique
}