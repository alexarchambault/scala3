package dotty.tools.dotc

import dotty.tools.dotc.util.MunitSuiteFromTestFactories

import scala.concurrent.duration.Duration

class IdempotencyTests0 extends MunitSuiteFromTestFactories {

  private lazy val idempotencyTests = new IdempotencyTests

  override def munitTimeout: Duration =
    IdempotencyTests.maxDuration

  lazy val dynTests = Seq(
    "idempotency" -> idempotencyTests.idempotency
  )

}
