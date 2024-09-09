import cats.data.NonEmptyList

trait MinimumPath[F[_]] {
  def calc(): F[Option[Int]]

}

object MinimumPath {
  import cats.implicits.*
  import cats.effect.kernel.*
  import fs2.io.file.{Files, Path}

  def live[F[_]: Async: Files](file: String): MinimumPath[F] =

    def parseLine(s: String): F[Array[Int]] = Sync[F].delay {
      s.split(" ").filter(_.nonEmpty).map(_.toInt)
    }

    new MinimumPath[F] {
      def calc(): F[Option[Int]] = Files[F]
        .readUtf8Lines(Path(file))
        .filterNot(_.isEmpty)
        .parEvalMap(maxConcurrent = sys.runtime.availableProcessors)(parseLine)
        .fold(None: Option[Array[Int]]) {
          case (None, rows) => rows.some
          case (Some(p), rows) =>
            val newP = new Array[Int](rows.length)

            // Process the current row based on the previous row
            for (j <- rows.indices) {
              val left  = if j > 0 then p(j - 1) else Int.MaxValue
              val right = if j < p.length then p(j) else Int.MaxValue
              newP(j) = rows(j) + math.min(left, right)
            }

            newP.some
        }
        .head
        .compile
        .lastOrError
        .map(_.flatMap(_.minOption))
    }

}
