package dotty.tools.dotc

import dotty.tools.dotc.util.MunitSuiteFromTestFactories

import scala.concurrent.duration.Duration

class CompilationTests0 extends MunitSuiteFromTestFactories {

  private lazy val compilationTests = new CompilationTests

  override def munitTimeout: Duration =
    CompilationTests.maxDuration

  lazy val dynTests = Seq(
    "compilePos" -> compilationTests.pos,
    "rewrites" -> compilationTests.rewrites,
    "posTwice" -> compilationTests.posTwice,
    "compileWarn" -> compilationTests.warn,
    "compileNeg" -> compilationTests.negAll,
    "compileFuzzy" -> compilationTests.fuzzyAll,
    "runAll" -> compilationTests.runAll,
    "genericJavaSignatures" -> compilationTests.genericJavaSignatures,
    "pickling" -> compilationTests.pickling,
    "recheck" -> compilationTests.recheck,
    "explicitNullsNeg" -> compilationTests.explicitNullsNeg,
    "explicitNullsPos" -> compilationTests.explicitNullsPos,
    "explicitNullsWarn" -> compilationTests.explicitNullsWarn,
    "explicitNullsRun" -> compilationTests.explicitNullsRun,
    "checkInitGlobal" -> compilationTests.checkInitGlobal,
    "parallelBackend" -> compilationTests.parallelBackend,
    "safeInit" -> compilationTests.safeInit
  )
}
