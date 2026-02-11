package scala.quoted.staging.repl

import dotty.tools.dotc.util.MunitSuiteFromTestFactories

class StagingScriptedReplTests0 extends MunitSuiteFromTestFactories {

  private lazy val underlying = new StagingScriptedReplTests

  lazy val dynTests = Seq(
    "replStagingTests" -> underlying.replStagingTests
  )

  // seems running the test suite doesn't stop if no tests are defined
  test("dummy") {}
}
