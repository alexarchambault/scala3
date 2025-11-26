package dotty.tools.scaladoc
package noLinkWarnings

import java.io.File
import dotty.Assertions.assertEquals

class LinkWarningsTest extends ScaladocTest("noLinkWarnings"):

  override def args(tempDir: File) = Scaladoc.Args(
    name = "test",
    tastyFiles = tastyFiles(name),
    output = tempDir,
    projectVersion = Some("1.0")
  )

  override def runTest(tempDir: File) = afterRendering(tempDir) {
    val diagnostics = summon[DocContext].compilerContext.reportedDiagnostics
    val filteredWarnings = diagnostics.warningMsgs.filter(_ != "1 warning found")
    assertEquals("There should be exactly one warning", 1, filteredWarnings.size)
    assertNoErrors(diagnostics)
  }
