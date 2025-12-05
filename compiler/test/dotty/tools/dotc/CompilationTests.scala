package dotty
package tools
package dotc

import scala.language.unsafeNulls

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions._
import dotty.Assumptions._

import java.io.File
import java.nio.file._
import java.util.stream.{ Stream => JStream }
import scala.jdk.CollectionConverters._
import scala.util.matching.Regex
import scala.concurrent.duration._
import TestSources.sources
import reporting.TestReporter
import vulpix._
import dotty.tools.dotc.config.ScalaSettings

class CompilationTests {
  import ParallelTesting._
  import TestConfiguration._
  import CompilationTests._
  import CompilationTest.aggregateTests

  // initialization tests
  @Test def safeInit: Unit = {
    given TestGroup = TestGroup("safeInit")
    val options = defaultOptions.and("-Wsafe-init", "-Werror")
    compileFilesInDir("tests/init/neg", options).checkExpectedErrors()
    compileFilesInDir("tests/init/warn", defaultOptions.and("-Wsafe-init")).checkWarnings()
    compileFilesInDir("tests/init/pos", options).checkCompile()
    compileFilesInDir("tests/init/crash", options.without("-Werror")).checkCompile()
    // The regression test for i12128 has some atypical classpath requirements.
    // The test consists of three files: (a) Reflect_1  (b) Macro_2  (c) Test_3
    // which must be compiled separately. In addition:
    //   - the output from (a) must be on the classpath while compiling (b)
    //   - the output from (b) must be on the classpath while compiling (c)
    //   - the output from (a) _must not_ be on the classpath while compiling (c)
    locally {
      val i12128Group = TestGroup("checkInit/i12128")
      val i12128Options = options.without("-Werror")
      val outDir1 = defaultOutputDir + i12128Group + "/Reflect_1/i12128/Reflect_1"
      val outDir2 = defaultOutputDir + i12128Group + "/Macro_2/i12128/Macro_2"

      val tests = List(
        compileFile("tests/init/special/i12128/Reflect_1.scala", i12128Options)(using i12128Group),
        compileFile("tests/init/special/i12128/Macro_2.scala", i12128Options.withClasspath(outDir1))(using i12128Group),
        compileFile("tests/init/special/i12128/Test_3.scala", options.withClasspath(outDir2))(using i12128Group)
      ).map(_.keepOutput.checkCompile())

      tests.foreach(_.delete())
    }

    /* This tests for errors in the program's TASTy trees.
     * The test consists of three files: (a) v1/A, (b) v1/B, and (c) v0/A. (a) and (b) are
     * compatible, but (b) and (c) are not. If (b) and (c) are compiled together, there should be
     * an error when reading the files' TASTy trees. */
    locally {
      val tastyErrorGroup = TestGroup("checkInit/tasty-error/val-or-defdef")
      val tastyErrorOptions = options.without("-Werror")

      val classA0 = defaultOutputDir + tastyErrorGroup + "/A/v0/A"
      val classA1 = defaultOutputDir + tastyErrorGroup + "/A/v1/A"
      val classB1 = defaultOutputDir + tastyErrorGroup + "/B/v1/B"

      val tests = List(
        compileFile("tests/init/tasty-error/val-or-defdef/v1/A.scala", tastyErrorOptions)(using tastyErrorGroup),
        compileFile("tests/init/tasty-error/val-or-defdef/v1/B.scala", tastyErrorOptions.withClasspath(classA1))(using tastyErrorGroup),
        compileFile("tests/init/tasty-error/val-or-defdef/v0/A.scala", tastyErrorOptions)(using tastyErrorGroup),
      ).map(_.keepOutput.checkCompile())

      compileFile("tests/init/tasty-error/val-or-defdef/Main.scala", tastyErrorOptions.withClasspath(classA0).withClasspath(classB1))(using tastyErrorGroup).checkExpectedErrors()

      tests.foreach(_.delete())
    }

    /* This tests for errors in the program's TASTy trees.
     * The test consists of five files: Main, C, v1/A, v1/B, and v0/A. The files v1/A, v1/B, and v0/A all depend on C. v1/A and v1/B are
     * compatible, but v1/B and v0/A are not. If v1/B and v0/A are compiled together, there should be
     * an error when reading the files' TASTy trees. This fact is demonstrated by the compilation of Main. */
    locally {
      val tastyErrorGroup = TestGroup("checkInit/tasty-error/typedef")
      val tastyErrorOptions = options.without("-Werror").without("-Ycheck:all")

      val classC = defaultOutputDir + tastyErrorGroup + "/C/typedef/C"
      val classA0 = defaultOutputDir + tastyErrorGroup + "/A/v0/A"
      val classA1 = defaultOutputDir + tastyErrorGroup + "/A/v1/A"
      val classB1 = defaultOutputDir + tastyErrorGroup + "/B/v1/B"

      val tests = List(
        compileFile("tests/init/tasty-error/typedef/C.scala", tastyErrorOptions)(using tastyErrorGroup),
        compileFile("tests/init/tasty-error/typedef/v1/A.scala", tastyErrorOptions.withClasspath(classC))(using tastyErrorGroup),
        compileFile("tests/init/tasty-error/typedef/v1/B.scala", tastyErrorOptions.withClasspath(classC).withClasspath(classA1))(using tastyErrorGroup),
        compileFile("tests/init/tasty-error/typedef/v0/A.scala", tastyErrorOptions.withClasspath(classC))(using tastyErrorGroup),
      ).map(_.keepOutput.checkCompile())

      compileFile("tests/init/tasty-error/typedef/Main.scala", tastyErrorOptions.withClasspath(classC).withClasspath(classA0).withClasspath(classB1))(using tastyErrorGroup).checkExpectedErrors()

      tests.foreach(_.delete())
    }
  }
}

object CompilationTests extends ParallelTesting {
  // Test suite configuration --------------------------------------------------

  def maxDuration = 45.seconds
  def numberOfSlaves = Runtime.getRuntime().availableProcessors()
  def safeMode = Properties.testsSafeMode
  def isInteractive = false
  def testFilter = Properties.testsFilter
  def updateCheckFiles: Boolean = Properties.testsUpdateCheckfile
  def failedTests = TestReporter.lastRunFailedTests

  implicit val summaryReport: SummaryReporting = new SummaryReport
  @AfterAll def tearDown(): Unit = {
    super.cleanup()
    summaryReport.echoSummary()
  }
}
