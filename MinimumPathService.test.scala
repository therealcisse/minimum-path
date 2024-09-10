class MinimumumPathTest extends munit.CatsEffectSuite {
  import cats.effect.*
  import fs2.io.file.Files
  import fs2.text
  import java.nio.file.*
  import cats.implicits.*

  val example =
    s"""|7
        |6 3
        |3 8 5
        |11 2 10 9""".stripMargin

  val invalidTriangle =
    s"""|7
        |6 3
        |3 8 5
        |11""".stripMargin

  val invalidInput =
    s"""|7
        |6 3
        |3 xx 5
        |11""".stripMargin

  val empty = s"""
  """.stripMargin

  val tempFileFixture = ResourceSuiteLocalFixture(
    "temp-file",
    Files[IO].tempFile
  )

  override def munitFixtures = List(tempFileFixture)

  test("calc minimum path sum success") {
    val service = MinimumPathService.live[IO]()

    IO(tempFileFixture()).flatMap { file =>
      fs2
        .Stream(example)
        .covary[IO]
        .through(text.utf8.encode)
        .through(Files[IO].writeAll(file))
        .compile
        .drain
        .assert *> assertIO(
        service.fromPath(file.toString),
        MinimumPath(18, List(7, 6, 3, 2).reverse).some
      )
    }
  }

  test("calc minimum path sum error invalid triangle") {
    val service = MinimumPathService.live[IO]()

    IO(tempFileFixture()).flatMap { file =>
      fs2
        .Stream(invalidTriangle)
        .covary[IO]
        .through(text.utf8.encode)
        .through(Files[IO].writeAll(file))
        .compile
        .drain
        .assert *> interceptMessageIO[IllegalArgumentException]("Invalid triangle row: 4")(
        service.fromPath(file.toString)
      )
    }
  }

  test("calc minimum path sum error invalid input") {
    val service = MinimumPathService.live[IO]()

    IO(tempFileFixture()).flatMap { file =>
      fs2
        .Stream(invalidInput)
        .covary[IO]
        .through(text.utf8.encode)
        .through(Files[IO].writeAll(file))
        .compile
        .drain
        .assert *> interceptMessageIO[IllegalArgumentException]("Failed to process row: 3")(
        service.fromPath(file.toString)
      )
    }
  }

  test("calc minimum path sum error empty triangle") {
    val service = MinimumPathService.live[IO]()

    IO(tempFileFixture()).flatMap { file =>
      fs2
        .Stream(empty)
        .covary[IO]
        .through(text.utf8.encode)
        .through(Files[IO].writeAll(file))
        .compile
        .drain
        .assert *> interceptMessageIO[IllegalArgumentException]("Empty row : 1")(
        service.fromPath(file.toString)
      )
    }
  }
}
