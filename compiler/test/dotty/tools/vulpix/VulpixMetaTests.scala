package dotty.tools
package vulpix

import org.junit.jupiter.api.*
import scala.concurrent.duration._
import TestConfiguration._

/** Meta tests for the Vulpix test suite. This test follows the structure of
 *  CompilationTests.scala. It is meant to be called from bash to diff with
 *  output against an expected result.
 */
@Tag("VulpixMeta")
class VulpixMetaTests {
  import VulpixMetaTests._

  implicit val summaryReport: SummaryReporting = new SummaryReport
  implicit def testGroup: TestGroup = TestGroup("VulpixMetaTests")

  @Disabled
  @TestFactory def compilePos =
    compileFilesInDir("tests/vulpix-tests/meta/pos", defaultOptions).dynamicTests(_.checkCompile())
  @Disabled
  @TestFactory def compileNeg =
    compileFilesInDir("tests/vulpix-tests/meta/neg", defaultOptions).dynamicTests(_.checkExpectedErrors())
  @Disabled
  @TestFactory def runAll =
    compileFilesInDir("tests/vulpix-tests/meta/run", defaultOptions).dynamicTests(_.checkRuns())
}

object VulpixMetaTests extends ParallelTesting {
  def maxDuration = 1.seconds
  // Ensure maximum reproducibility.
  def numberOfWorkers = 1
  def safeMode = false // Don't fork a new VM after each run test
  def isInteractive = false // Don't beautify output for interactive use.
  def testFilter = Nil // Run all the tests.
  def updateCheckFiles: Boolean = false
  def failedTests = None

  @AfterAll
  def tearDown() = this.cleanup()
}