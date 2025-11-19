package dotty.tools.dotc.core.tasty

import scala.language.unsafeNulls

import java.io.{File => JFile, ByteArrayOutputStream, IOException}
import java.nio.file.{Files, NoSuchFileException, Paths}

import scala.sys.process._

import org.junit.Test
import org.junit.Assert.{assertEquals, assertTrue, assertFalse, fail}

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.ast.tpd.TreeOps
import dotty.tools.dotc.{Driver, Main}
import dotty.tools.dotc.decompiler
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Decorators.{toTermName, toTypeName}
import dotty.tools.dotc.core.Mode
import dotty.tools.dotc.core.Names.Name
import dotty.tools.dotc.interfaces.Diagnostic.ERROR
import dotty.tools.dotc.reporting.TestReporter
import dotty.tools.io.{Directory, File, Path, JarArchive}

import dotty.tools.vulpix.TestConfiguration
import dotty.Properties

class PathPicklingTest {

  @Test def test(): Unit = {
    val out = Properties.repoRoot.resolve("out/testPathPickling").toFile
    val cwd = Properties.repoRoot.toString
    delete(out)
    out.mkdirs()

    locally {
      val ignorantProcessLogger = ProcessLogger(_ => ())
      val options = TestConfiguration.defaultOptions
        .and("-d", new JFile(out, "out.jar").toString)
        .and("-sourceroot", Properties.repoRoot.resolve("tests/pos").toString)
        .and(
          Properties.repoRoot.resolve("tests/pos/i10430/lib.scala").toString,
          Properties.repoRoot.resolve("tests/pos/i10430/app.scala").toString
        )
      val reporter = TestReporter.reporter(System.out, logLevel = ERROR)
      val rep = Main.process(options.all, reporter)
      assertFalse("Compilation failed.", rep.hasErrors)
    }

    val printedTasty =
      val sb = new StringBuffer
      val jar = JarArchive.open(Path(new JFile(out, "out.jar").toString), create = false)
      try
        for file <- jar.iterator if file.name.endsWith(".tasty") do
          sb.append(TastyPrinter.showContents(file.toByteArray, noColor = true, isBestEffortTasty = false))
      finally jar.close()
      sb.toString()

    assertTrue(printedTasty.contains(": i10430/lib.scala"))
    assertTrue(printedTasty.contains("[i10430/lib.scala]"))
    assertFalse(printedTasty.contains(": i10430\\lib.scala"))
    assertFalse(printedTasty.contains("[i10430\\lib.scala]"))

    assertTrue(printedTasty.contains(": i10430/app.scala"))
    assertTrue(printedTasty.contains("[i10430/app.scala]"))
    assertFalse(printedTasty.contains(": i10430\\app.scala"))
    assertFalse(printedTasty.contains("[i10430\\app.scala]"))
  }

  private def delete(file: JFile): Unit = {
    if (file.isDirectory) file.listFiles.foreach(delete)
    try Files.delete(file.toPath)
    catch {
      case _: NoSuchFileException => // already deleted, everything's fine
    }
  }
}
