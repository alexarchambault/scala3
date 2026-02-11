package dotty.tools.dotc

import dotty.Properties
import dotty.tools.dotc.util.MunitSuiteFromTestFactories

import scala.concurrent.duration.Duration

class BootstrappedOnlyCompilationTests0 extends MunitSuiteFromTestFactories {

  private lazy val compilationTests = new BootstrappedOnlyCompilationTests

  override def munitTimeout: Duration =
    BootstrappedOnlyCompilationTests.maxDuration

  lazy val actualDynTests = Seq(
    "compilePosMacros" -> compilationTests.posMacros,
    "compilePosWithCompiler" -> compilationTests.posWithCompiler,
    "posTwiceWithCompiler" -> compilationTests.posTwiceWithCompiler,
    "compileNegWithCompiler" -> compilationTests.negWithCompiler,
    "runWithCompiler" -> compilationTests.runWithCompiler,
    // "runScala2LibraryFromTasty" -> compilationTests.runScala2LibraryFromTasty, // disabled?
    "runBootstrappedOnly" -> compilationTests.runBootstrappedOnly,
    "testPicklingWithCompiler" -> compilationTests.picklingWithCompiler,
    "testPlugins" -> compilationTests.testPlugins
  )

  def dynTests =
    if (Properties.testsIsBootstrapped) actualDynTests
    else Nil

}
