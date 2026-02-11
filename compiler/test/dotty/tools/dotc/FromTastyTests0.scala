package dotty.tools.dotc

import dotty.tools.dotc.util.MunitSuiteFromTestFactories

import scala.concurrent.duration.Duration

class FromTastyTests0 extends MunitSuiteFromTestFactories {

  private lazy val fromTastyTests = new FromTastyTests

  override def munitTimeout: Duration =
    FromTastyTests.maxDuration

  lazy val dynTests = Seq(
    "posTestFromTasty" -> fromTastyTests.posTestFromTasty,
    "runTestFromTasty" -> fromTastyTests.runTestFromTasty
  )

}
