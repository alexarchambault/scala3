package dotty.tools.scaladoc

import scala.jdk.CollectionConverters.{ListHasAsScala, SeqHasAsJava}
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import scala.collection.mutable.ListBuffer

abstract class ScaladocTest(val name: String):

  def afterRendering(tempDir: File)(op: DocContext ?=> Unit) =
    val ctx = Scaladoc.run(args(tempDir))(using testContext)
    op(using ctx)

  def moduleDocContext = testDocContext(tastyFiles(name))

  def withModule(tempDir: File)(op: DocContext ?=> Module => Unit) =
    given DocContext = moduleDocContext
    op(ScalaModuleProvider.mkModule())

  def args(tempDir: File) = Scaladoc.Args(
      name = "test",
      tastyFiles = tastyFiles(name),
      output = tempDir,
      projectVersion = Some("1.0"),
      sourceLinks = List("github://scala/scala3/master")
    )

  @Test
  final def runTest0(@TempDir tempDir: Path): Unit = {
    try runTest(tempDir.toFile)
    finally
      if (errors.nonEmpty)
        for (err <- errors)
          System.err.println(s"Error: $err")
    if (errors.nonEmpty)
      sys.error("Some errors were found")
  }

  def runTest(tempDir: File): Unit

  private var errors = new ListBuffer[String]
  def reportError(msg: String) =
    errors += msg


