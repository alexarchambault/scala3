package dotty
package tools
package dotc

import scala.language.unsafeNulls

import java.io.{File => JFile}
import java.nio.file.{Files, Path, Paths}

import org.junit.jupiter.api.{util => _, *}

import scala.concurrent.duration._
import scala.jdk.CollectionConverters.*
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
  @Disabled
  @TestFactory def idempotency = {
    implicit val testGroup: TestGroup = TestGroup("idempotency")
    val opt = defaultOptions

    val posIdempotency = compileFilesInDir("tests/pos", opt, filter)(using TestGroup("idempotency/posIdempotency1")).split()
      .zip(compileFilesInDir("tests/pos", opt, filter)(using TestGroup("idempotency/posIdempotency2")).split())

    val posTests = for ((pos1, pos2) <- posIdempotency) yield {
      DynamicTest.dynamicTest(
        pos1.targets.head.title,
        () =>
          try {
            pos1.keepOutput.checkCompile()
            pos2.keepOutput.checkCompile()

            IdempotencyCheck.checkIdempotency(pos1.targets.head.outDir.toPath, pos2.targets.head.outDir.toPath)
          }
          finally {
            pos1.delete()
            pos2.delete()
          }
      )
    }

    val orderIdempotency =
      new JFile("tests/order-idempotency").listFiles().toSeq.filter(_.isDirectory).flatMap { testDir =>
        val sources = TestSources.sources(testDir.toPath)
        compileList(testDir.getName, sources, opt)(using TestGroup("idempotency/orderIdempotency1")).split()
          .zip(compileList(testDir.getName, sources, opt)(using TestGroup("idempotency/orderIdempotency2")).split())
      }

    val orderTests = for ((ord1, ord2) <- orderIdempotency) yield {
      DynamicTest.dynamicTest(
        ord1.targets.head.title,
        () =>
          try {
            ord1.keepOutput.checkCompile()
            ord2.keepOutput.checkCompile()

            IdempotencyCheck.checkIdempotency(ord1.targets.head.outDir.toPath, ord2.targets.head.outDir.toPath)
          }
          finally {
            ord1.delete()
            ord2.delete()
          }
      )
    }

    // Disabled until strawman is fixed
    // IdempotencyCheck.checkIdempotency("out/idempotency/strawman0", "out/idempotency/strawman1")
    // FIXME: #2964 and maybe more
    /*
    IdempotencyCheck.checkIdempotency("out/idempotency/strawman1", "out/idempotency/strawman2")
    IdempotencyCheck.checkIdempotency("out/idempotency/strawman1", "out/idempotency/strawman3")
    */

    (posTests ++ orderTests).asJava
  }

}

object IdempotencyTests extends ParallelTesting {
  // Test suite configuration --------------------------------------------------

  // Not sure why we need to increase this one from Mill
  def maxDuration = 1.minute
  def numberOfWorkers = 5
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
