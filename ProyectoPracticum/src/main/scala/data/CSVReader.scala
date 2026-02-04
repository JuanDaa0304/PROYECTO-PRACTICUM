package data

import scala.io.Source
import java.io.PrintWriter
import rescala.default.*

object CSVReader {

  val rawLines: Var[List[String]] = Var(Nil)

  def load(path: String): Unit =
    rawLines.set(Source.fromFile(path, "UTF-8").getLines().toList)

  def write(
             path: String,
             headers: List[String],
             rows: List[List[String]]
           ): Unit = {
    val writer = new PrintWriter(path, "UTF-8")
    writer.println(headers.mkString(";"))
    rows.foreach(r => writer.println(r.mkString(";")))
    writer.close()
  }
}
