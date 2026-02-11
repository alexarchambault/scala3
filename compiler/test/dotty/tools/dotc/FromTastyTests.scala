package dotty
package tools
package dotc

import scala.language.unsafeNulls

import org.junit.jupiter.api.*
import reporting.TestReporter
import vulpix._

import java.io.{File => JFile}

import scala.concurrent.duration._

class FromTastyTests {
  import TestConfiguration._
  import FromTastyTests._

  @TestFactory def posTestFromTasty = {
    // Can be reproduced with
    // > sbt
    // > scalac -Ythrough-tasty -Ycheck:all <source>

    implicit val testGroup: TestGroup = TestGroup("posTestFromTasty")
    compileTastyInDir(s"tests${JFile.separator}pos", defaultOptions,
      fromTastyFilter = FileFilter.exclude(TestSources.posFromTastyExcludelisted)
    ).dynamicTests(_.checkCompile())
  }

  @TestFactory def runTestFromTasty = {
    // Can be reproduced with
    // > sbt
    // > scalac -Ythrough-tasty -Ycheck:all <source>
    // > scala Test

    implicit val testGroup: TestGroup = TestGroup("runTestFromTasty")
    compileTastyInDir(s"tests${JFile.separator}run", defaultOptions,
      fromTastyFilter = FileFilter.exclude(TestSources.runFromTastyExcludelisted)
    ).dynamicTests(_.checkRuns())
  }
}

object FromTastyTests extends ParallelTesting {
  // Test suite configuration --------------------------------------------------

  def maxDuration = 30.seconds
  def numberOfWorkers = Runtime.getRuntime().availableProcessors()
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
