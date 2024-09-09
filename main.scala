import cats.implicits.*

import cats.effect.IO
import cats.effect.unsafe.implicits.global

@main def run(file: String): Unit =
  val service = MinimumPathService.live[IO]()

  (
    for {

      result <- service.calc(file)

      _ <- IO.println(result.getOrElse("No minimal path found"))
    } yield ()
  ).unsafeRunSync()
