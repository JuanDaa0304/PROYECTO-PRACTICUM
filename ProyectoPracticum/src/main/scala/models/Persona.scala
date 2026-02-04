package models

case class Persona(
                    personaId: Int,
                    nombre: String,
                    genero: Option[String],
                    rutaPerfil: Option[String]
                  )
