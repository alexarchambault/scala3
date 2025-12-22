package dotty
package tools
package dotc

import scala.language.unsafeNulls

import java.io.{File => JFile}
import java.nio.file.{Files, Path, Paths}

import org.junit.jupiter.api.{util => _, *}

import scala.concurrent.duration._
import reporting.TestReporter
import util.IdempotencyCheck
import vulpix._


class IdempotencyTests {
  import TestConfiguration._
  import IdempotencyTests._
  import CompilationTest.aggregateTests

  // ignore flaky tests
  val filter = FileFilter.NoFilter

  @Tag("slow")
  @Test def idempotency: Unit = {
    implicit val testGroup: TestGroup = TestGroup("idempotency")
    val opt = defaultOptions

    val posIdempotency = aggregateTests(
      compileFilesInDir("tests/pos", opt, filter)(using TestGroup("idempotency/posIdempotency1")),
      compileFilesInDir("tests/pos", opt, filter)(using TestGroup("idempotency/posIdempotency2")),
    )

    val orderIdempotency = {
      val tests =
        for {
          testDir <- new JFile("tests/order-idempotency").listFiles() if testDir.isDirectory
        } yield {
          val sources = TestSources.sources(testDir.toPath)
          aggregateTests(
            compileList(testDir.getName, sources, opt)(using TestGroup("idempotency/orderIdempotency1")),
            compileList(testDir.getName, sources.reverse, opt)(using TestGroup("idempotency/orderIdempotency2"))
          )
        }
      aggregateTests(tests*)
    }

    val allTests = aggregateTests(orderIdempotency, posIdempotency)

    val tests = allTests.keepOutput.checkCompile()

    IdempotencyCheck.checkIdempotency("out/idempotency/orderIdempotency1", "out/idempotency/orderIdempotency2")

    // Disabled until strawman is fixed
    // IdempotencyCheck.checkIdempotency("out/idempotency/strawman0", "out/idempotency/strawman1")
    // FIXME: #2964 and maybe more
    /*
    IdempotencyCheck.checkIdempotency("out/idempotency/strawman1", "out/idempotency/strawman2")
    IdempotencyCheck.checkIdempotency("out/idempotency/strawman1", "out/idempotency/strawman3")
    */

    IdempotencyCheck.checkIdempotency("out/idempotency/posIdempotency1", "out/idempotency/posIdempotency2")

    tests.delete()
  }

}

object IdempotencyTests extends ParallelTesting {
  // Test suite configuration --------------------------------------------------

  def maxDuration = 30.seconds
  def numberOfWorkers = 5
  def safeMode = Properties.testsSafeMode
  def isInteractive = SummaryReport.isInteractive
  def testFilter = Properties.testsFilter
  def updateCheckFiles: Boolean = Properties.testsUpdateCheckfile
  def failedTests = TestReporter.lastRunFailedTests

  implicit val summaryReport: SummaryReporting = new SummaryReport
  @AfterAll def tearDown(): Unit = {
    super.cleanup()
    summaryReport.echoSummary()
  }
}
