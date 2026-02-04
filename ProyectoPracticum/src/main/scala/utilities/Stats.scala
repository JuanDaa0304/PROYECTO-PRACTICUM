package utilities

object Stats {

  def min(values: List[Double]): Double = values.min
  def max(values: List[Double]): Double = values.max
  def avg(values: List[Double]): Double = values.sum / values.size

}
