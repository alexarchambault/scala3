package scala.quoted.staging.repl

import dotty.BootstrappedOnlyTests
import dotty.tools.scripts
import dotty.tools.repl.ReplTest
import dotty.tools.vulpix.TestConfiguration
import org.junit.jupiter.api.*

/** Runs all tests contained in `staging/test-resources/repl-staging` */
class StagingScriptedReplTests extends ReplTest(ReplTest.withStagingOptions) {

  @Tag("BootstrappedOnly")
  @Test def replStagingTests = scripts("/repl-staging").foreach(testFile)

}
