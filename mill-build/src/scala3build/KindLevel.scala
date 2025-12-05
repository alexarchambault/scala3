package scala3build

import mill.api.Cross

case class KindLevel(kind: Kind, level: Level) {
  def isHybrid: Boolean =
    kind == Kind.Hybrid
  def isBootstrapping: Boolean =
    level == Level.Bootstrapping
}

object KindLevel {
  implicit lazy val toSegments: Cross.ToSegments[KindLevel] = new Cross.ToSegments[KindLevel]({
    case KindLevel(kind, level) =>
      List(kind.asString, level.asString)
  })

  lazy val allValues = Seq[KindLevel](
    KindLevel(Kind.Hybrid, Level.Bootstrapping),
    KindLevel(Kind.Hybrid, Level.Final),
    KindLevel(Kind.Pure, Level.Bootstrapping),
    KindLevel(Kind.Pure, Level.Final)
  )

  def apply(isHybrid: Boolean, isBootstrapping: Boolean): KindLevel =
    KindLevel(
      if (isHybrid) Kind.Hybrid else Kind.Pure,
      if (isBootstrapping) Level.Bootstrapping else Level.Final
    )
}
