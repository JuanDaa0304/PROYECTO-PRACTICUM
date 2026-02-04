package utilities

import java.time.LocalDate

object Parsers {

  def parseDate(s: String): Option[LocalDate] =
    Cleaners.cleanString(s).flatMap { v =>
      try Some(LocalDate.parse(v))
      catch {
        case _: Exception => None }
    }
}
