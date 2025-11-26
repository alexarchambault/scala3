package dotty.tools.dotc

import dotty.tools.vulpix._
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled

@Disabled class Playground:
  import TestConfiguration._
  import CompilationTests._

  @Test def example: Unit =
    implicit val testGroup: TestGroup = TestGroup("playground")
    compileFile("tests/playground/example.scala", defaultOptions).checkCompile()
