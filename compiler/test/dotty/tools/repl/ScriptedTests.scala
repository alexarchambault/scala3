package dotty
package tools
package repl

import org.junit.jupiter.api.*

/** Runs all tests contained in `compiler/test-resources/repl/` */
class ScriptedTests extends ReplTest {

  @Test def replTests = scripts("/repl").foreach(testFile)

  @Tag("BootstrappedOnly")
  @Test def replMacrosTests = scripts("/repl-macros").foreach(testFile)

  @Test def typePrinterTests = scripts("/type-printer").foreach(testFile)
}

object ScriptedTests {
}
