package scala.quoted.staging.repl

import dotty.BootstrappedOnlyTests
import dotty.tools.scripts
import dotty.tools.repl.ReplTest
import dotty.tools.vulpix.TestConfiguration
import org.junit.jupiter.api.*

import scala.jdk.CollectionConverters.*

/** Runs all tests contained in `staging/test-resources/repl-staging` */
class StagingScriptedReplTests extends ReplTest(ReplTest.withStagingOptions) {

  @Tag("BootstrappedOnly")
  @TestFactory def replStagingTests = {
    scripts("/repl-staging")
      .toSeq
      .map { f =>
        DynamicTest.dynamicTest(
          f.toString,
          () => {
            testFile(f)
          }
        )
      }
      .asJava
  }

}
