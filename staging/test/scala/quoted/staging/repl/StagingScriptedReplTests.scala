package scala.quoted.staging.repl

import dotty.BootstrappedOnlyTests
import dotty.tools.scripts
import dotty.tools.repl.ReplTest
import dotty.tools.vulpix.TestConfiguration
import org.junit.jupiter.api.*

import scala.jdk.CollectionConverters.*

/** Runs all tests contained in `staging/test-resources/repl-staging` */
class StagingScriptedReplTests extends ReplTest(ReplTest.withStagingOptions) {

  @Disabled
  @Tag("BootstrappedOnly")
  @TestFactory def replStagingTests = {
    scripts("/repl-staging")
      .toSeq
      .map { f =>
        val name = {
          val p = f.toPath
          val elems = (0 until p.getNameCount).map(p.getName(_).toString)
          val idx = elems.lastIndexOf("repl-staging")
          assert(idx >= 0)
          elems.drop(idx + 1).mkString("/")
        }
        DynamicTest.dynamicTest(
          name,
          () => testFile(f)
        )
      }
      .take(0)
      .asJava
  }

}
