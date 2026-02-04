package models

import java.time.LocalDate

case class Pelicula(
                     peliculaId: Int,
                     imdbId: Option[String],
                     titulo: String,
                     tituloOriginal: String,
                     resumen: Option[String],
                     eslogan: Option[String],
                     duracion: Option[Int],
                     fechaEstreno: Option[LocalDate],
                     presupuesto: Option[BigDecimal],
                     ingresos: Option[BigDecimal],
                     popularidad: Option[Double],
                     promedioVotos: Option[Double],
                     cantidadVotos: Option[Int],
                     estado: Option[String],
                     paginaWeb: Option[String],
                     rutaPoster: Option[String],
                     esVideo: Boolean,
                     esAdulto: Boolean,
                     coleccionId: Option[Int]
                   )
