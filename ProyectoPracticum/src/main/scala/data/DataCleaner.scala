package data

import utilities.Cleaners.*

object DataCleaner {

  def cleanRows(raw: List[String]): List[List[String]] = {
    val header :: rows = raw
    val colCount = header.split(",").length

    rows.flatMap { line =>
      val cols = line.split(",", -1).toList

      if (cols.length != colCount) None
      else {

        val budget = toDouble(cols(2))
        val popularity = toDouble(cols(10))
        val revenue = toDouble(cols(15))
        val releaseDate = cleanDate(cols(14))
        val genresSize = jsonArraySize(cols(3))
        val castSize = jsonArraySize(cols(19))
        val crewSize = jsonArraySize(cols(20))

        if (
          budget.isDefined &&
            popularity.isDefined &&
            releaseDate.isDefined
        ) {
          Some(
            cols.updated(14, releaseDate.get.toString)
          )
        } else None
      }
    }
  }
}
