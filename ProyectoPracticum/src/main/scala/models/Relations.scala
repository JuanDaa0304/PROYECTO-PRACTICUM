package models


// Relaciones Many-to-Many
case class PeliculaGenero(
                           peliculaId: Int,
                           generoId: Int
                         )

case class PeliculaPalabraClave(
                                 peliculaId: Int,
                                 palabraClaveId: Int
                               )

case class PeliculaProductora(
                               peliculaId: Int,
                               empresaId: Int
                             )

case class PeliculaPais(
                         peliculaId: Int,
                         codigoIso3166: String
                       )

// Reparto y Equipo Técnico
case class Reparto(
                    peliculaId: Int,
                    personaId: Int,
                    personaje: String,
                    ordenAparicion: Option[Int],
                    creditoId: Option[String]
                  )

case class EquipoTecnico(
                          peliculaId: Int,
                          personaId: Int,
                          cargo: String,
                          departamento: Option[String],
                          creditoId: Option[String]
                        )

case class Calificacion(
                         calificacionId: Int,
                         peliculaId: Int,
                         usuarioId: Int,
                         puntuacion: Double,
                         marcaTiempo: java.time.LocalDateTime
                       )