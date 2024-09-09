class MinimumumPathTest extends munit.CatsEffectSuite {
  import cats.effect.*
  import fs2.io.file.Files
  import fs2.text
  import java.nio.file.*
  import cats.implicits.*

  val example = s"""
  |7
  |6 3
  |3 8 5
  |11 2 10 9
  """.stripMargin

  val tempFileFixture = ResourceSuiteLocalFixture(
    "temp-file",
    Files[IO].tempFile
  )

  override def munitFixtures = List(tempFileFixture)

  test("calc minimum path sum") {
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
        service.calc(file.toString),
        MinimumPath(18, List(7, 6, 3, 2).reverse).some
      )
    }
  }
}
