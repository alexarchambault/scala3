package scala3build

import build_.package_ as build
import mill.*
import mill.api.BuildCtx
import mill.scalalib.*

import scala.annotation.tailrec

/**
 * Tasks for Scala modules in the Scala 3 build.
 *
 * Beyond what [[Scala3JavaModule]] adds, this deals with setting up
 * the library, compiler, compiler bridge, and scaladoc, when bootstrapping and
 * for final modules. This also sets some shared scalac options.
 */
trait Scala3Module extends Scala3JavaModule with ScalaModule { outer =>
  def mode: Mode = Mode.Final

  def sourcesFolders = super.sourcesFolders ++ Seq(
    if (mode == Mode.Bootstrapping) "src-non-bootstrapped" else "src-bootstrapped"
  )

  def resolutionParams = Task.Anon {
    val baseParams = super.resolutionParams()
    baseParams.withForceVersion0(
      baseParams.forceVersion0.filter(_._1.organization.value != "org.scala-lang")
    )
  }

  def scalaVersion =
    if (mode == Mode.Bootstrapping) Versions.referenceVersion
    else Versions.dottyVersion

  def scalaLibraryMvnDeps = Nil
  def scalaCompilerClasspath =
    if (mode == Mode.Bootstrapping)
      Task {
        defaultResolver().classpath(
          Seq(mvn"org.scala-lang:scala3-compiler_3:${Versions.referenceVersion}")
        )
      }
    else
      build.compiler(Mode.Bootstrapping).runClasspathAsJars

  def scalaCompilerBridge =
    if (mode == Mode.Bootstrapping)
      Task(None)
    else
      Task {
        Some(build.`sbt-bridge`(Mode.Bootstrapping).jar())
      }

  def scalaDocClasspath =
    if (mode == Mode.Bootstrapping)
      super.scalaDocClasspath
    else
      // Use the *bootstrapped* scaladoc to build all bootstrapped (that is, published) modules' scaladoc,
      // even the one of the bootstrapped scaladoc itself (it builds its own scaladoc)
      build.scaladoc(Mode.Final).runClasspathAsJars

  def scalacOptions = super.scalacOptions() ++ Seq(
    "-feature",
    "-deprecation",
    "-unchecked",
    //"-Wconf:cat=deprecation&msg=Unsafe:s",    // example usage
    //"-Wunused:all",
    //"-rewrite", // requires -Werror:false since no rewrites are applied with errors
    "-encoding", "UTF8",
    "-language:implicitConversions",
    "--java-output-version", Versions.minimumJVMVersion
  )

  def scalaDocOptions = Task {
    val baseOptions = super.scalaDocOptions()
    val filteredBaseOptions = {
      @tailrec
      def helper(opts: Seq[String]): Seq[String] = {
        val sourcepathIdx = opts.indexOf("-sourcepath")
        if (sourcepathIdx >= 0)
          helper(opts.take(sourcepathIdx) ++ opts.drop(sourcepathIdx + 2))
        else
          opts
      }
      helper(baseOptions)
    }
    val extraOptions =
      if (mode == Mode.Final) {
        val rawExtraOptions = ScaladocOptions.scalacOptionsDocSettings()
        // Make -project-logo arg an absolute path, as scaladoc isn't going to run
        // from the root of the project with Mill
        val projLogoIdx = rawExtraOptions.indexOf("-project-logo")
        if (projLogoIdx >= 0)
          rawExtraOptions.take(projLogoIdx + 1) ++
            Seq(os.Path(rawExtraOptions(projLogoIdx + 1), BuildCtx.workspaceRoot).toString) ++
            rawExtraOptions.drop(projLogoIdx + 2)
        else
           rawExtraOptions
      }
      else
        Nil
    filteredBaseOptions ++ extraOptions
  }

  trait Scala3Tests extends ScalaTests with Scala3JavaTests {
    // Future versions of Mill should handle these two
    def scalaLibraryMvnDeps = outer.scalaLibraryMvnDeps
    def scalaCompilerClasspath = outer.scalaCompilerClasspath
  }

  def enableBsp = mode == Mode.Bootstrapping || Scala3Module.enableBspForFinalModules
}

object Scala3Module {
  def enableBspForFinalModules = false
}
