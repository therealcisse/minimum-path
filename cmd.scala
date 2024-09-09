import cats.implicits.*

import cats.effect.IO
import cats.effect.unsafe.implicits.global

@main def cmd(): Unit =
  val service = MinimumPathService.live[IO]()

  (
    for {
      result <- service.stdin()

      _ <- IO.println(result.getOrElse("No minimal path found"))
    } yield ()
  ).unsafeRunSync()
