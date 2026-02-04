package services

import cats.effect.*
import cats.implicits.*
import doobie.*
import doobie.implicits.*
import models.*
import repositories.MovieRepository
import scala.concurrent.duration.*

class MovieService(using xa: Transactor[IO]) {

  // OPERACIONES DE CATÁLOGO 

  def insertCatalogoCompleto(
                              generos: List[Genero],
                              palabrasClave: List[PalabraClave],
                              productoras: List[Productora],
                              paises: List[Pais],
                              idiomas: List[Idioma],
                              personas: List[Persona],
                              colecciones: List[Coleccion]
                            ): IO[Unit] = {
    val program = for {
      _ <- IO.println("Insertando géneros...")
      g <- MovieRepository.insertBatch(generos, 50)(MovieRepository.insertGeneros).transact(xa)
      _ <- IO.sleep(50.millis)
      _ <- IO.println(s"✓ $g géneros insertados")

      _ <- IO.println("Insertando palabras clave...")
      p <- MovieRepository.insertBatch(palabrasClave, 100)(MovieRepository.insertPalabrasClave).transact(xa)
      _ <- IO.sleep(100.millis)
      _ <- IO.println(s"✓ $p palabras clave insertadas")

      _ <- IO.println("Insertando productoras...")
      pr <- MovieRepository.insertBatch(productoras, 100)(MovieRepository.insertProductoras).transact(xa)
      _ <- IO.sleep(100.millis)
      _ <- IO.println(s"✓ $pr productoras insertadas")

      _ <- IO.println("Insertando países...")
      pa <- MovieRepository.insertBatch(paises, 50)(MovieRepository.insertPaises).transact(xa)
      _ <- IO.sleep(50.millis)
      _ <- IO.println(s"✓ $pa países insertados")

      _ <- IO.println("Insertando idiomas...")
      i <- MovieRepository.insertBatch(idiomas, 50)(MovieRepository.insertIdiomas).transact(xa)
      _ <- IO.sleep(50.millis)
      _ <- IO.println(s"✓ $i idiomas insertados")

      _ <- IO.println("Insertando personas...")
      pe <- MovieRepository.insertBatch(personas, 100)(MovieRepository.insertPersonas).transact(xa)
      _ <- IO.sleep(100.millis)
      _ <- IO.println(s"✓ $pe personas insertadas")

      _ <- IO.println("Insertando colecciones...")
      c <- MovieRepository.insertBatch(colecciones, 50)(MovieRepository.insertColecciones).transact(xa)
      _ <- IO.sleep(50.millis)
      _ <- IO.println(s"✓ $c colecciones insertadas")
    } yield ()

    program
  }


  // OPERACIONES DE PELÍCULAS

  def insertPeliculas(peliculas: List[Pelicula], batchSize: Int = 100): IO[Int] = {
    IO.println(s"Insertando ${peliculas.size} películas en batches de $batchSize...") *>
      MovieRepository.insertBatch(peliculas, batchSize)(MovieRepository.insertPeliculas).transact(xa)
        .flatTap(count => IO.sleep(100.millis))
        .flatTap(count => IO.println(s"✓ $count películas insertadas"))
  }

  def insertRelaciones(
                        peliculaGeneros: List[PeliculaGenero],
                        peliculaPalabras: List[PeliculaPalabraClave],
                        peliculaProductoras: List[PeliculaProductora],
                        peliculaPaises: List[PeliculaPais]
                      ): IO[Unit] = {
    val program = for {
      _ <- IO.println("Insertando relaciones película-género...")
      g <- MovieRepository.insertBatch(peliculaGeneros, 100)(MovieRepository.insertPeliculaGeneros).transact(xa)
      _ <- IO.sleep(50.millis)
      _ <- IO.println(s"✓ $g relaciones género insertadas")

      _ <- IO.println("Insertando relaciones película-palabras clave...")
      p <- MovieRepository.insertBatch(peliculaPalabras, 100)(MovieRepository.insertPeliculaPalabrasClave).transact(xa)
      _ <- IO.sleep(100.millis)
      _ <- IO.println(s"✓ $p relaciones palabras clave insertadas")

      _ <- IO.println("Insertando relaciones película-productoras...")
      pr <- MovieRepository.insertBatch(peliculaProductoras, 100)(MovieRepository.insertPeliculaProductoras).transact(xa)
      _ <- IO.sleep(100.millis)
      _ <- IO.println(s"✓ $pr relaciones productoras insertadas")

      _ <- IO.println("Insertando relaciones película-países...")
      pa <- MovieRepository.insertBatch(peliculaPaises, 100)(MovieRepository.insertPeliculaPaises).transact(xa)
      _ <- IO.sleep(50.millis)
      _ <- IO.println(s"✓ $pa relaciones países insertadas")
    } yield ()

    program
  }

  def insertRepartosYEquipo(
                             repartos: List[Reparto],
                             equipos: List[EquipoTecnico]
                           ): IO[Unit] = {
    val program = for {
      _ <- IO.println("Insertando reparto...")
      r <- MovieRepository.insertBatch(repartos, 100)(MovieRepository.insertRepartos).transact(xa)
      _ <- IO.sleep(100.millis)
      _ <- IO.println(s"✓ $r miembros del reparto insertados")

      _ <- IO.println("Insertando equipo técnico...")
      e <- MovieRepository.insertBatch(equipos, 100)(MovieRepository.insertEquipoTecnico).transact(xa)
      _ <- IO.sleep(100.millis)
      _ <- IO.println(s"✓ $e miembros del equipo técnico insertados")
    } yield ()

    program
  }

  def insertCalificaciones(calificaciones: List[Calificacion], batchSize: Int = 100): IO[Unit] = {
    val program = for {
      _ <- IO.println(s"Insertando ${calificaciones.size} calificaciones en batches de $batchSize...")
      c <- MovieRepository.insertBatch(calificaciones, batchSize)(MovieRepository.insertCalificaciones).transact(xa)
      _ <- IO.sleep(100.millis)
      _ <- IO.println(s"✓ $c calificaciones insertadas")
    } yield ()

    program
  }


  // OPERACIONES DE CONSULTA

  def obtenerPeliculaPorId(id: Int): IO[Option[Pelicula]] =
    MovieRepository.getPeliculaById(id).transact(xa)

  def obtenerTodasLasPeliculas(limit: Int = 100): IO[List[Pelicula]] =
    MovieRepository.getAllPeliculas(limit).transact(xa)

  def obtenerGenerosDePelicula(peliculaId: Int): IO[List[Genero]] =
    MovieRepository.getGenerosByPeliculaId(peliculaId).transact(xa)

  def contarPeliculas: IO[Int] =
    MovieRepository.countPeliculas.transact(xa)


  // OPERACIÓN DE CARGA COMPLETA


  def cargaCompletaOptimizada(
                               generos: List[Genero],
                               palabrasClave: List[PalabraClave],
                               productoras: List[Productora],
                               paises: List[Pais],
                               idiomas: List[Idioma],
                               personas: List[Persona],
                               colecciones: List[Coleccion],
                               peliculas: List[Pelicula],
                               peliculaGeneros: List[PeliculaGenero],
                               peliculaPalabras: List[PeliculaPalabraClave],
                               peliculaProductoras: List[PeliculaProductora],
                               peliculaPaises: List[PeliculaPais],
                               repartos: List[Reparto],
                               equipos: List[EquipoTecnico],
                               calificaciones: List[Calificacion]
                             ): IO[Unit] = {
    for {
      _ <- IO.println("=" * 50)
      _ <- IO.println("INICIANDO CARGA COMPLETA DE DATOS")
      _ <- IO.println("=" * 50)
      inicio <- IO.realTimeInstant

      _ <- IO.println("\n[FASE 1] Cargando catálogos...")
      _ <- insertCatalogoCompleto(generos, palabrasClave, productoras, paises, idiomas, personas, colecciones)

      _ <- IO.println("\n[FASE 2] Cargando películas...")
      _ <- insertPeliculas(peliculas)

      _ <- IO.println("\n[FASE 3] Cargando relaciones...")
      _ <- insertRelaciones(peliculaGeneros, peliculaPalabras, peliculaProductoras, peliculaPaises)

      _ <- IO.println("\n[FASE 4] Cargando reparto y equipo técnico...")
      _ <- insertRepartosYEquipo(repartos, equipos)

      _ <- IO.println("\n[FASE 5] Cargando calificaciones...")
      _ <- insertCalificaciones(calificaciones)

      fin <- IO.realTimeInstant
      duracion = java.time.Duration.between(inicio, fin).toSeconds

      totalPeliculas <- contarPeliculas
      _ <- IO.println("\n" + "=" * 50)
      _ <- IO.println("CARGA COMPLETA FINALIZADA")
      _ <- IO.println("=" * 50)
      _ <- IO.println(s"Total de películas en BD: $totalPeliculas")
      _ <- IO.println(s"Tiempo total: $duracion segundos")
      _ <- IO.println("=" * 50)
    } yield ()
  }
}

// COMPANION OBJECT - CORREGIDO
object MovieService {
}