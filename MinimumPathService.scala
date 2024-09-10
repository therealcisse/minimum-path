import cats.data.NonEmptyList
import scala.util.control.NonFatal

trait MinimumPathService[F[_]] {
  def fromPath(path: String): F[Option[MinimumPath]]
  def stdin(): F[Option[MinimumPath]]

}

object MinimumPathService {
  import cats.implicits.*
  import cats.effect.kernel.*
  import fs2.io.file.{Files, Path}
  import fs2.text

  def live[F[_]: Async: Files](): MinimumPathService[F] =

    def parseLine(s: String, index: Int): F[(Array[Int], Int)] =
      val tr = s.trim

      if tr.isEmpty then Sync[F].raiseError(IllegalArgumentException(s"Empty row : $index"))
      else
        Sync[F]
          .delay {
            tr.split(" ").filter(_.nonEmpty).map(_.toInt) -> index
          }
          .onError { case NonFatal(e) =>
            Sync[F].raiseError(IllegalArgumentException(s"Failed to process row: $index", e))
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

      private def go(s: fs2.Stream[F, String]): F[Option[MinimumPath]] =
        s.through(text.lines)
          .zip(fs2.Stream.fromIterator(Iterator.from(1), chunkSize = 4096))
          .parEvalMap(maxConcurrent = sys.runtime.availableProcessors)(parseLine)
          .fold(None: Option[Array[MinimumPath]]) {
            case (None, (row, _)) => row.map(r => MinimumPath(r, List(r))).some
            case (Some(p), (row, index)) =>
              if p.length + 1 != row.length then
                throw IllegalArgumentException(s"Invalid triangle row: $index")
              else process(p, row)
          }
          .head
          .compile
          .lastOrError
          .map(_.flatMap(_.minByOption(_.sum)))

      def stdin(): F[Option[MinimumPath]] =
        go {

          fs2.io
            .readInputStream(Sync[F].delay(System.in), chunkSize = 4096, closeAfterUse = false)
            .through(text.utf8.decode)
        }

      def fromPath(path: String): F[Option[MinimumPath]] =
        go {
          Files[F].readUtf8(Path(path))
        }

    }

}
