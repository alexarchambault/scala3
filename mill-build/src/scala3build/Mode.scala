package scala3build

import mill.api.Cross

/**
 * Enum for the two stages to build compiler-related JARs: bootstrapping and final
 */
sealed abstract class Mode(val name: String)

object Mode:
  /**
   * First build of a module, using an already published Scala 3 version
   *
   * Aka "non-bootstrapped" in the sbt build
   */
  case object Bootstrapping extends Mode("bootstrapping")

  /**
   * Second (and last) build of a module, using newly built compiler and standard library
   *
   * Aka "bootstrapped" in the sbt build
   *
   * This uses the standard library and compiler built in the Bootstrapping stage, and
   * the scaladoc built in the Final stage (in this stage, we build scaladoc and use it
   * straightaway in the same stage).
   */
  case object Final extends Mode("final")

  lazy val allValues = Seq[Mode](
    Bootstrapping,
    Final
  )

  given Cross.ToSegments[Mode] =
    new Cross.ToSegments(mode => List(mode.name))
