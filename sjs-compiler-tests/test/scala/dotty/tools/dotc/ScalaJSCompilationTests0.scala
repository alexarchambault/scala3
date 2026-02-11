package dotty.tools.dotc

import dotty.tools.dotc.util.MunitSuiteFromTestFactories

import scala.concurrent.duration.Duration

class ScalaJSCompilationTests0 extends MunitSuiteFromTestFactories {

  private lazy val underlying = new ScalaJSCompilationTests

  override def munitTimeout: Duration =
    ScalaJSCompilationTests.maxDuration

  lazy val dynTests = Seq(
    "neg" -> underlying.negScalaJS,
    "run" -> underlying.runScalaJS
  )
}
