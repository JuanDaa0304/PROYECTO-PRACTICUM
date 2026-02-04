package models

case class Coleccion(
                      coleccionId: Int,
                      nombre: String,
                      rutaPoster: Option[String],
                      rutaFondo: Option[String]
                    )