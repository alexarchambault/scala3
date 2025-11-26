package dotty.tools.scaladoc
package noLinkWarnings

import java.io.File

class NoLinkWarningsTest extends ScaladocTest("noLinkWarnings"):

  override def args(tempDir: File) = Scaladoc.Args(
    name = "test",
    tastyFiles = tastyFiles(name),
    output = tempDir,
    projectVersion = Some("1.0"),
    noLinkWarnings = true
  )

  override def runTest(tempDir: File) = afterRendering(tempDir) {
    val diagnostics = summon[DocContext].compilerContext.reportedDiagnostics
    assertNoWarning(diagnostics)
    assertNoErrors(diagnostics)
  }
