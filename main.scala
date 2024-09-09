import cats.implicits.*

import cats.effect.IO
import cats.effect.unsafe.implicits.global

@main def run(file: String): Unit =
  val service = MinimumPath.live[IO](file)

  (
    for {

      result <- service.calc()

      _ <- IO.println(s"Minimum path sum: $result")
    } yield ()
  ).unsafeRunSync()
