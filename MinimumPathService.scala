import cats.data.NonEmptyList

trait MinimumPathService[F[_]] {
  def calc(path: String): F[Option[MinimumPath]]
  def stdin(): F[Option[MinimumPath]]

}

object MinimumPathService {
  import cats.implicits.*
  import cats.effect.kernel.*
  import fs2.io.file.{Files, Path}
  import fs2.text

  def live[F[_]: Async: Files](): MinimumPathService[F] =

    def parseLine(s: String): F[Array[Int]] = Sync[F].delay {
      s.split(" ").filter(_.nonEmpty).map(_.toInt)
    }

    new MinimumPathService[F] {

      private def process(
        previousRows: Array[MinimumPath],
        current: Array[Int]
      ): Option[Array[MinimumPath]] =
        val newP = new Array[MinimumPath](current.length)

        // Process the current row based on the previous row
        for (j <- current.indices) {
          val left = if j > 0 then previousRows(j - 1) else MinimumPath(Int.MaxValue, Nil)
          val right =
            if j < previousRows.length then previousRows(j) else MinimumPath(Int.MaxValue, Nil)

          val chosen = if left.sum < right.sum then left else right

          newP(j) = MinimumPath(current(j) + chosen.sum, current(j) :: chosen.path)
        }

        newP.some

      def stdin(): F[Option[MinimumPath]] =
        fs2.io
          .readInputStream(Sync[F].delay(System.in), chunkSize = 4096, closeAfterUse = false)
          .through(text.utf8.decode)
          .through(text.lines)
          .filterNot(_.trim.isEmpty)
          .parEvalMap(maxConcurrent = sys.runtime.availableProcessors)(parseLine)
          .fold(None: Option[Array[MinimumPath]]) {
            case (None, rows)    => rows.map(r => MinimumPath(r, List(r))).some
            case (Some(p), rows) => process(p, rows)
          }
          .head
          .compile
          .lastOrError
          .map(_.flatMap(_.minByOption(_.sum)))

      def calc(path: String): F[Option[MinimumPath]] = Files[F]
        .readUtf8(Path(path))
        .through(text.lines)
        .filterNot(_.trim.isEmpty)
        .parEvalMap(maxConcurrent = sys.runtime.availableProcessors)(parseLine)
        .fold(None: Option[Array[MinimumPath]]) {
          case (None, rows)    => rows.map(r => MinimumPath(r, List(r))).some
          case (Some(p), rows) => process(p, rows)
        }
        .head
        .compile
        .lastOrError
        .map(_.flatMap(_.minByOption(_.sum)))

    }

}
