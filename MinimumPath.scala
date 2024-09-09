case class MinimumPath(sum: Int, path: List[Int]) {
  override def toString(): String = s"""Minimal path is: ${path.mkString(" + ")} = $sum"""
}
