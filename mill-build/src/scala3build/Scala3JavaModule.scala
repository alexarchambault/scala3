package scala3build

import com.github.lolgab.mill.mima.Mima
import mill.*
import mill.scalalib.*
import mill.scalalib.publish.*

trait Scala3JavaModule extends JavaModule with PublishModule with Mima {
  trait Scala3JavaTests extends JavaTests with TestModule with TestModule.Junit5 {
    def testSources: T[Seq[PathRef]]
    def sources = testSources
    def testParallelism = false

    def useMunit: Boolean = false

    def testFramework =
      if (useMunit) "munit.Framework"
      else "com.github.sbt.junit.jupiter.api.JupiterFramework"
    def mvnDeps = super.mvnDeps() ++ Seq(
      mvn"com.github.sbt.junit:jupiter-interface:0.17.0",
      mvn"org.junit.platform:junit-platform-launcher:1.14.1",
      mvn"org.junit.jupiter:junit-jupiter-api:${Versions.junitJupiter}",
      mvn"org.scalameta::munit::1.2.1"
        .exclude(("org.scala-lang", "scala-library"))
        .exclude(("org.scala-lang", "scala3-library_3"))
    )

    def discoveredTestClasses =
      if (useMunit) super[TestModule].discoveredTestClasses
      else super[Junit5].discoveredTestClasses

    def enableBsp =
      // only exposing the base test module to BSP
      // both this one and the munit one have the same dependencies anyway
      super.enableBsp && !useMunit
  }

  def jvmId = "17" // force external zinc worker

  def pomSettings = PomSettings(
    description = artifactName(),
    organization = "org.scala-lang",
    url = "https://github.com/scala/scala3",
    licenses = Seq(License.`Apache-2.0`),
    versionControl = VersionControl.github("scala", "scala3"),
    developers = Seq(Developer("scala", "The Scala Team", "https://scala-lang.org", email = "security@scala-lang.org"))
  )
  def publishVersion = Versions.dottyVersion

  def javacOptions = super.javacOptions() ++ Seq(
    "--release", Versions.minimumJVMVersion
  )

  def manifest = super.manifest().add(
    "Automatic-Module-Name" ->
      s"${pomSettings().organization.replaceAll("-", ".")}.${artifactName().replaceAll("-", ".")}"
  )

  def runClasspathAsJars: T[Seq[PathRef]] = Task {
    resolvedRunMvnDeps().toSeq ++
      transitiveJars() ++
      Seq(jar())
  }

  def runClasspath = runClasspathAsJars
}
