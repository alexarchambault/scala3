package dotty
package tools
package dotc

import scala.language.unsafeNulls

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions._
import dotty.Assumptions._

import java.io.File
import java.nio.file._
import java.util.stream.{ Stream => JStream }
import scala.jdk.CollectionConverters._
import scala.util.matching.Regex
import scala.concurrent.duration._
import TestSources.sources
import reporting.TestReporter
import vulpix._
import dotty.tools.dotc.config.ScalaSettings

class CompilationTests {
  import ParallelTesting._
  import TestConfiguration._
  import CompilationTests._
  import CompilationTest.aggregateTests

  // Positive tests ------------------------------------------------------------

  @TestFactory def pos = {
    implicit val testGroup: TestGroup = TestGroup("compilePos")
    var tests = List(
      compileFilesInDir("tests/pos", defaultOptions.and("-Wsafe-init", "-Wunused:all", "-Wshadow:private-shadow", "-Wshadow:type-parameter-shadow"), FileFilter.include(TestSources.posLintingAllowlist)),
      compileFilesInDir("tests/pos", defaultOptions.and("-Wsafe-init"), FileFilter.exclude(TestSources.posLintingAllowlist)),
      compileFilesInDir("tests/pos-deep-subtype", allowDeepSubtypes),
      compileFilesInDir("tests/pos-special/sourcepath/outer", defaultOptions.and("-sourcepath", "tests/pos-special/sourcepath")),
      compileFile("tests/pos-special/sourcepath/outer/nested/Test4.scala", defaultOptions.and("-sourcepath", "tests/pos-special/sourcepath")),
      compileFilesInDir("tests/pos-scala2", defaultOptions.and("-source", "3.0-migration")),
      compileFilesInDir("tests/pos-custom-args/captures", defaultOptions.and("-language:experimental.captureChecking", "-language:experimental.separationChecking", "-source", "3.8")),
      compileFile("tests/pos-special/utf8encoded.scala", defaultOptions.and("-encoding", "UTF8")),
      compileFile("tests/pos-special/utf16encoded.scala", defaultOptions.and("-encoding", "UTF16")),
      compileDir("tests/pos-special/i18589", defaultOptions.and("-Wsafe-init").without("-Ycheck:all")),
      compileDir("tests/pos-special/i24547", defaultOptions.without("-Ycheck:all")),
      // Run tests for legacy lazy vals
      compileFilesInDir("tests/pos", defaultOptions.and("-Wsafe-init", "-Ylegacy-lazy-vals", "-Ycheck-constraint-deps"), FileFilter.include(TestSources.posLazyValsAllowlist))(using TestGroup("compilePosLegacyLazyVals")),
      compileDir("tests/pos-special/java-param-names", defaultOptions.withJavacOnlyOptions("-parameters")),
    ) ::: (
      // TODO create a folder for capture checking tests with the stdlib, or use tests/pos-custom-args/captures under this mode?
      if Properties.usingScalaLibraryCCTasty then List(compileDir("tests/pos-special/stdlib", allowDeepSubtypes))
      else Nil
    )

    if scala.util.Properties.isJavaAtLeast("16") then
      tests ::= compileFilesInDir("tests/pos-java16+", defaultOptions.and("-Wsafe-init"))

    aggregateTests(tests*).dynamicTests(_.checkCompile())
  }

  @TestFactory def rewrites = {
    implicit val testGroup: TestGroup = TestGroup("rewrites")

    aggregateTests(
      compileFile("tests/rewrites/rewrites.scala", defaultOptions.and("-source", "3.0-migration").and("-rewrite", "-indent")),
      compileFile("tests/rewrites/rewrites3x.scala", defaultOptions.and("-rewrite", "-source", "future-migration")),
      compileFile("tests/rewrites/rewrites3x-fatal-warnings.scala", defaultOptions.and("-rewrite", "-source", "future-migration", "-Werror")),
      compileFile("tests/rewrites/i21394.scala", defaultOptions.and("-rewrite", "-source", "future-migration")),
      compileFile("tests/rewrites/uninitialized-var.scala", defaultOptions.and("-rewrite", "-source", "future-migration")),
      compileFile("tests/rewrites/with-type-operator.scala", defaultOptions.and("-rewrite", "-source", "future-migration")),
      compileFile("tests/rewrites/private-this.scala", defaultOptions.and("-rewrite", "-source", "future-migration")),
      compileFile("tests/rewrites/alphanumeric-infix-operator.scala", defaultOptions.and("-rewrite", "-source", "future-migration")),
      compileFile("tests/rewrites/filtering-fors.scala", defaultOptions.and("-rewrite", "-source", "3.2-migration")),
      compileFile("tests/rewrites/refutable-pattern-bindings-old.scala", defaultOptions.and("-rewrite", "-source", "3.2-migration")),
      compileFile("tests/rewrites/refutable-pattern-bindings.scala", defaultOptions.and("-rewrite", "-source", "3.8-migration")),
      compileFile("tests/rewrites/i8982.scala", defaultOptions.and("-indent", "-rewrite")),
      compileFile("tests/rewrites/i9632.scala", defaultOptions.and("-indent", "-rewrite")),
      compileFile("tests/rewrites/i11895.scala", defaultOptions.and("-indent", "-rewrite")),
      compileFile("tests/rewrites/i12340.scala", unindentOptions.and("-rewrite")),
      compileFile("tests/rewrites/i17187.scala", unindentOptions.and("-rewrite")),
      compileFile("tests/rewrites/i17399.scala", unindentOptions.and("-rewrite")),
      compileFile("tests/rewrites/i20002.scala", defaultOptions.and("-indent", "-rewrite")),
      compileDir("tests/rewrites/annotation-named-pararamters", defaultOptions.and("-rewrite", "-source:3.6-migration")),
      compileFile("tests/rewrites/i21418.scala", unindentOptions.and("-rewrite", "-source:3.5-migration")),
      compileFile("tests/rewrites/infix-named-args.scala", defaultOptions.and("-rewrite", "-source:3.7-migration")),
      compileFile("tests/rewrites/ambiguous-named-tuple-assignment.scala", defaultOptions.and("-rewrite", "-source:3.6-migration")),
      compileFile("tests/rewrites/i21382.scala", defaultOptions.and("-indent", "-rewrite")),
      compileFile("tests/rewrites/unused.scala", defaultOptions.and("-rewrite", "-Wunused:all")),
      compileFile("tests/rewrites/i22440.scala", defaultOptions.and("-rewrite")),
      compileFile("tests/rewrites/i22731.scala", defaultOptions.and("-rewrite", "-source:3.7-migration")),
      compileFile("tests/rewrites/i22731b.scala", defaultOptions.and("-rewrite", "-source:3.7-migration")),
      compileFile("tests/rewrites/implicit-to-given.scala", defaultOptions.and("-rewrite", "-Yimplicit-to-given")),
      compileFile("tests/rewrites/i22792.scala", defaultOptions.and("-rewrite")),
      compileFile("tests/rewrites/i23449.scala", defaultOptions.and("-rewrite", "-source:3.4-migration")),
      compileFile("tests/rewrites/i24213.scala", defaultOptions.and("-rewrite", "-source:3.4-migration")),
    ).dynamicTests(_.checkRewrites())
  }

  @TestFactory def posTwice = {
    implicit val testGroup: TestGroup = TestGroup("posTwice")
    aggregateTests(
      compileFilesInDir("tests/pos-java-interop", defaultOptions),
      compileFilesInDir("tests/pos-java-interop-separate", defaultOptions),
      compileFile("tests/pos/t2168.scala", defaultOptions),
      compileFile("tests/pos/test-erasure.scala", defaultOptions),
      compileFile("tests/pos/Coder.scala", defaultOptions),
      compileFile("tests/pos/blockescapes.scala", defaultOptions),
      compileFile("tests/pos/functions1.scala", defaultOptions),
      compileFile("tests/pos/test-implicits1.scala", defaultOptions),
      compileFile("tests/pos/inferred.scala", defaultOptions),
      compileFile("tests/pos/selftypes.scala", defaultOptions),
      compileFile("tests/pos/varargs.scala", defaultOptions),
      compileFile("tests/pos/vararg-pattern.scala", defaultOptions),
      compileFile("tests/pos/opassign.scala", defaultOptions),
      compileFile("tests/pos/typedapply.scala", defaultOptions),
      compileFile("tests/pos/nameddefaults.scala", defaultOptions),
      compileFile("tests/pos/test-desugar.scala", defaultOptions),
      compileFile("tests/pos/sigs.scala", defaultOptions),
      compileFile("tests/pos/test-typers.scala", defaultOptions),
      compileDir("tests/pos/typedIdents", defaultOptions),
      compileFile("tests/pos/assignments.scala", defaultOptions),
      compileFile("tests/pos/packageobject.scala", defaultOptions),
      compileFile("tests/pos/overloaded.scala", defaultOptions),
      compileFile("tests/pos/overrides.scala", defaultOptions),
      compileDir("tests/pos/java-override", defaultOptions),
      compileFile("tests/pos/templateParents.scala", defaultOptions),
      compileFile("tests/pos/overloadedAccess.scala", defaultOptions),
      compileFile("tests/pos/approximateUnion.scala", defaultOptions),
      compileFilesInDir("tests/pos/tailcall", defaultOptions),
      compileShallowFilesInDir("tests/pos/pos_valueclasses", defaultOptions),
      compileFile("tests/pos/subtyping.scala", defaultOptions),
      compileFile("tests/pos/i0239.scala", defaultOptions),
      compileFile("tests/pos/anonClassSubtyping.scala", defaultOptions),
      compileFile("tests/pos/extmethods.scala", defaultOptions),
      compileFile("tests/pos/companions.scala", defaultOptions),
      compileFile("tests/pos/main.scala", defaultOptions)
    ).dynamicTests(_.times(2).checkCompile())
  }

  // Warning tests ------------------------------------------------------------

  @TestFactory def warn = {
    implicit val testGroup: TestGroup = TestGroup("compileWarn")
    aggregateTests(
      compileFilesInDir("tests/warn", defaultOptions),
    ).dynamicTests(_.checkWarnings())
  }

  // Negative tests ------------------------------------------------------------

  @TestFactory def negAll = {
    implicit val testGroup: TestGroup = TestGroup("compileNeg")
    aggregateTests(
      compileFilesInDir("tests/neg", defaultOptions, FileFilter.exclude(TestSources.negScala2LibraryTastyExcludelisted)),
      compileFilesInDir("tests/neg-deep-subtype", allowDeepSubtypes),
      compileFilesInDir("tests/neg-custom-args/captures", defaultOptions.and("-language:experimental.captureChecking", "-language:experimental.separationChecking", "-source", "3.8")),
      compileFile("tests/neg-custom-args/sourcepath/outer/nested/Test1.scala", defaultOptions.and("-sourcepath", "tests/neg-custom-args/sourcepath")),
      compileDir("tests/neg-custom-args/sourcepath2/hi", defaultOptions.and("-sourcepath", "tests/neg-custom-args/sourcepath2", "-Werror")),
      compileList("duplicate source", List(
        "tests/neg-custom-args/toplevel-samesource/S.scala",
        "tests/neg-custom-args/toplevel-samesource/nested/S.scala"),
        defaultOptions),
      compileFile("tests/neg/i7575.scala", defaultOptions.withoutLanguageFeatures),
    ).dynamicTests(_.checkExpectedErrors())
  }

  @TestFactory def fuzzyAll = {
    implicit val testGroup: TestGroup = TestGroup("compileFuzzy")
    compileFilesInDir("tests/fuzzy", defaultOptions).dynamicTests(_.checkNoCrash())
  }

  // Run tests -----------------------------------------------------------------

  @TestFactory def runAll = {
    implicit val testGroup: TestGroup = TestGroup("runAll")
    aggregateTests(
      compileFilesInDir("tests/run", defaultOptions.and("-Wsafe-init")),
      compileFilesInDir("tests/run-deep-subtype", allowDeepSubtypes),
      compileFilesInDir("tests/run-custom-args/captures", allowDeepSubtypes.and("-language:experimental.captureChecking", "-language:experimental.separationChecking", "-source", "3.8")),
      // Run tests for legacy lazy vals.
      compileFilesInDir("tests/run", defaultOptions.and("-Wsafe-init", "-Ylegacy-lazy-vals", "-Ycheck-constraint-deps"), FileFilter.include(TestSources.runLazyValsAllowlist))(using TestGroup("runAllLegacyLazyVals")),
    ).dynamicTests(_.checkRuns())
  }

  // Generic java signatures tests ---------------------------------------------

  @TestFactory def genericJavaSignatures = {
    implicit val testGroup: TestGroup = TestGroup("genericJavaSignatures")
    compileFilesInDir("tests/generic-java-signatures", defaultOptions).dynamicTests(_.checkRuns())
  }

  // Pickling Tests ------------------------------------------------------------

  @TestFactory def pickling = {
    implicit val testGroup: TestGroup = TestGroup("testPickling")
    aggregateTests(
      compileFilesInDir("tests/pos", picklingOptions, FileFilter.exclude(TestSources.posTestPicklingExcludelisted)),
      compileFilesInDir("tests/run", picklingOptions, FileFilter.exclude(TestSources.runTestPicklingExcludelisted))
    ).dynamicTests(_.checkCompile())
  }

  //@Test disabled in favor of posWithCompilerCC to save time.
  @TestFactory def recheck =
    given TestGroup = TestGroup("recheck")
    aggregateTests(
      compileFilesInDir("tests/run", defaultOptions.and("-Yrecheck-test"), FileFilter.exclude(TestSources.runTestRecheckExcluded))
      //Disabled to save some time.
      //compileFilesInDir("tests/pos", recheckOptions, FileFilter.exclude(TestSources.posTestRecheckExcluded)),
    ).dynamicTests(_.checkCompile())

  // Explicit nulls tests
  @TestFactory def explicitNullsNeg = {
    implicit val testGroup: TestGroup = TestGroup("explicitNullsNeg")
    aggregateTests(
      compileFilesInDir("tests/explicit-nulls/neg", explicitNullsOptions, FileFilter.exclude(TestSources.negExplicitNullsScala2LibraryTastyExcludelisted)),
      compileFilesInDir("tests/explicit-nulls/flexible-types-common", explicitNullsOptions `and` "-Yno-flexible-types"),
      compileFilesInDir("tests/explicit-nulls/unsafe-common", explicitNullsOptions `and` "-Yno-flexible-types", FileFilter.exclude(TestSources.negExplicitNullsScala2LibraryTastyExcludelisted)),
    ).dynamicTests(_.checkExpectedErrors())

    // locally {
    //   val unsafeFile = compileFile("tests/explicit-nulls/flexible-unpickle/neg/Unsafe_1.scala", explicitNullsOptions without "-Yexplicit-nulls")
    //   val flexibleFile = compileFile("tests/explicit-nulls/flexible-unpickle/neg/Flexible_2.scala",
    //       explicitNullsOptions.and("-Yflexify-tasty").withClasspath(defaultOutputDir + testGroup + "/Unsafe_1/neg/Unsafe_1"))

    //   unsafeFile.keepOutput.checkCompile()
    //   flexibleFile.keepOutput.checkExpectedErrors()

    //   List(unsafeFile, flexibleFile).foreach(_.delete())
    // }
  }

  @TestFactory def explicitNullsPos = {
    implicit val testGroup: TestGroup = TestGroup("explicitNullsPos")
    aggregateTests(
      compileFilesInDir("tests/explicit-nulls/pos", explicitNullsOptions),
      compileFilesInDir("tests/explicit-nulls/flexible-types-common", explicitNullsOptions),
      compileFilesInDir("tests/explicit-nulls/unsafe-common", explicitNullsOptions `and` "-language:unsafeNulls" `and` "-Yno-flexible-types"),
    ).dynamicTests(_.checkCompile())

    // locally {
    //   val tests = List(
    //     compileFile("tests/explicit-nulls/flexible-unpickle/pos/Unsafe_1.scala", explicitNullsOptions without "-Yexplicit-nulls"),
    //     compileFile("tests/explicit-nulls/flexible-unpickle/pos/Flexible_2.scala",
    //     explicitNullsOptions.and("-Yflexify-tasty").withClasspath(defaultOutputDir + testGroup + "/Unsafe_1/pos/Unsafe_1")),
    //   ).map(_.keepOutput.checkCompile())

    //   tests.foreach(_.delete())
    // }
  }

  @TestFactory def explicitNullsWarn = {
    implicit val testGroup: TestGroup = TestGroup("explicitNullsWarn")
    compileFilesInDir("tests/explicit-nulls/warn", explicitNullsOptions)
  }.dynamicTests(_.checkWarnings())

  @TestFactory def explicitNullsRun = {
    implicit val testGroup: TestGroup = TestGroup("explicitNullsRun")
    compileFilesInDir("tests/explicit-nulls/run", explicitNullsOptions)
  }.dynamicTests(_.checkRuns())

  // initialization tests for global objects
  @TestFactory def checkInitGlobal = {
    implicit val testGroup: TestGroup = TestGroup("checkInitGlobal")
    val warnTests = compileFilesInDir("tests/init-global/warn", defaultOptions.and("-Ysafe-init-global"), FileFilter.exclude(TestSources.negInitGlobalScala2LibraryTastyExcludelisted))
      .namedDynamicTests("warnings")(_.checkWarnings())
      .asScala
    val posTests = compileFilesInDir("tests/init-global/pos", defaultOptions.and("-Ysafe-init-global", "-Werror"), FileFilter.exclude(TestSources.posInitGlobalScala2LibraryTastyExcludelisted))
      .namedDynamicTests("compile")(_.checkCompile())
      .asScala
    val tastyTests =
      if Properties.usingScalaLibraryTasty && !Properties.usingScalaLibraryCCTasty then
        val tastyWarnTests = compileFilesInDir("tests/init-global/warn-tasty", defaultOptions.and("-Ysafe-init-global"), FileFilter.exclude(TestSources.negInitGlobalScala2LibraryTastyExcludelisted))
          .namedDynamicTests("warnings")(_.checkWarnings())
          .asScala
        val tastyPosTests = compileFilesInDir("tests/init-global/pos-tasty", defaultOptions.and("-Ysafe-init-global", "-Werror"), FileFilter.exclude(TestSources.posInitGlobalScala2LibraryTastyExcludelisted))
          .namedDynamicTests("compile")(_.checkCompile())
          .asScala
        tastyWarnTests ++ tastyPosTests
      else
        Nil
      end if
    (warnTests ++ posTests ++ tastyTests).asJava
  }

  @Test def checkInitGlobalTastySource: Unit = {
    val group = TestGroup("checkInitGlobal/tastySource")
    val tastSourceOptions = defaultOptions.and("-Ysafe-init-global")
    val outDirLib = defaultOutputDir + group + "/A/tastySource/A-scala"

    // Set -sourceroot such that the source code cannot be found by the compiler
    val libOptions = tastSourceOptions.and("-sourceroot", "tests/init-global/special")
    val lib = compileFile("tests/init-global/special/tastySource/A.scala", libOptions)(using group).keepOutput.checkCompile()

    compileFile("tests/init-global/special/tastySource/B.scala", tastSourceOptions.withClasspath(outDirLib))(using group).checkWarnings()

    lib.delete()
  }

  // initialization tests
  @TestFactory def safeInit = {
    given TestGroup = TestGroup("safeInit")
    val options = defaultOptions.and("-Wsafe-init", "-Werror")
    val neg = compileFilesInDir("tests/init/neg", options).dynamicTests(_.checkExpectedErrors()).asScala
    val warn = compileFilesInDir("tests/init/warn", defaultOptions.and("-Wsafe-init")).dynamicTests(_.checkWarnings()).asScala
    val pos = compileFilesInDir("tests/init/pos", options).dynamicTests(_.checkCompile()).asScala
    val crash = compileFilesInDir("tests/init/crash", options.without("-Werror")).dynamicTests(_.checkCompile()).asScala
    // The regression test for i12128 has some atypical classpath requirements.
    // The test consists of three files: (a) Reflect_1  (b) Macro_2  (c) Test_3
    // which must be compiled separately. In addition:
    //   - the output from (a) must be on the classpath while compiling (b)
    //   - the output from (b) must be on the classpath while compiling (c)
    //   - the output from (a) _must not_ be on the classpath while compiling (c)
    val i12128 = DynamicTest.dynamicTest("i12128", () => {
      val i12128Group = TestGroup("checkInit/i12128")
      val i12128Options = options.without("-Werror")

      val testReflect1 = compileFile("tests/init/special/i12128/Reflect_1.scala", i12128Options)(using i12128Group)
      val outDir1 = testReflect1.targets.head.outDir.toString
      val testMacro2 = compileFile("tests/init/special/i12128/Macro_2.scala", i12128Options.withClasspath(outDir1))(using i12128Group)
      val outDir2 = testMacro2.targets.head.outDir.toString
      val testTest3 = compileFile("tests/init/special/i12128/Test_3.scala", options.withClasspath(outDir2))(using i12128Group)

      val tests = List(
        testReflect1,
        testMacro2,
        testTest3
      ).map(_.keepOutput.checkCompile())

      tests.foreach(_.delete())
    })

    /* This tests for errors in the program's TASTy trees.
     * The test consists of three files: (a) v1/A, (b) v1/B, and (c) v0/A. (a) and (b) are
     * compatible, but (b) and (c) are not. If (b) and (c) are compiled together, there should be
     * an error when reading the files' TASTy trees. */
    val tastyErrorValOrDefdef = DynamicTest.dynamicTest("tasty-error val-or-defdef", () => {
      val tastyErrorGroup = TestGroup("checkInit/tasty-error/val-or-defdef")
      val tastyErrorOptions = options.without("-Werror")

      val testA1 = compileFile("tests/init/tasty-error/val-or-defdef/v1/A.scala", tastyErrorOptions)(using tastyErrorGroup)
      val classA1 = testA1.targets.head.outDir.toString
      val testB1 = compileFile("tests/init/tasty-error/val-or-defdef/v1/B.scala", tastyErrorOptions.withClasspath(classA1))(using tastyErrorGroup)
      val testA0 = compileFile("tests/init/tasty-error/val-or-defdef/v0/A.scala", tastyErrorOptions)(using tastyErrorGroup)

      val classA0 = testA0.targets.head.outDir.toString
      val classB1 = testB1.targets.head.outDir.toString

      val tests = List(
        testA1,
        testB1,
        testA0,
      ).map(_.keepOutput.checkCompile())

      compileFile("tests/init/tasty-error/val-or-defdef/Main.scala", tastyErrorOptions.withClasspath(classA0).withClasspath(classB1))(using tastyErrorGroup).checkExpectedErrors()

      tests.foreach(_.delete())
    })

    /* This tests for errors in the program's TASTy trees.
     * The test consists of five files: Main, C, v1/A, v1/B, and v0/A. The files v1/A, v1/B, and v0/A all depend on C. v1/A and v1/B are
     * compatible, but v1/B and v0/A are not. If v1/B and v0/A are compiled together, there should be
     * an error when reading the files' TASTy trees. This fact is demonstrated by the compilation of Main. */
    val tastyErrorTypedef = DynamicTest.dynamicTest("tasty-error typedef", () => {
      val tastyErrorGroup = TestGroup("checkInit/tasty-error/typedef")
      val tastyErrorOptions = options.without("-Werror").without("-Ycheck:all")

      val testC = compileFile("tests/init/tasty-error/typedef/C.scala", tastyErrorOptions)(using tastyErrorGroup)
      val classC = testC.targets.head.outDir.toString
      val testA0 = compileFile("tests/init/tasty-error/typedef/v0/A.scala", tastyErrorOptions.withClasspath(classC))(using tastyErrorGroup)
      val testA1 = compileFile("tests/init/tasty-error/typedef/v1/A.scala", tastyErrorOptions.withClasspath(classC))(using tastyErrorGroup)
      val classA1 = testA1.targets.head.outDir.toString
      val testB1 = compileFile("tests/init/tasty-error/typedef/v1/B.scala", tastyErrorOptions.withClasspath(classC).withClasspath(classA1))(using tastyErrorGroup)

      val classA0 = testA0.targets.head.outDir.toString
      val classB1 = testB1.targets.head.outDir.toString

      val tests = List(
        testC,
        testA1,
        testB1,
        testA0,
      ).map(_.keepOutput.checkCompile())

      compileFile("tests/init/tasty-error/typedef/Main.scala", tastyErrorOptions.withClasspath(classC).withClasspath(classA0).withClasspath(classB1))(using tastyErrorGroup).checkExpectedErrors()

      tests.foreach(_.delete())
    })

    (neg ++ warn ++ pos ++ crash ++ Seq(i12128, tastyErrorValOrDefdef, tastyErrorTypedef)).asJava
  }

  // parallel backend tests
  @TestFactory def parallelBackend = {
    given TestGroup = TestGroup("parallelBackend")
    val parallelism = Runtime.getRuntime().availableProcessors().min(16)
    assumeTrue("Not enough available processors to run parallel tests", parallelism > 1)

    val options = defaultOptions.and(s"-Ybackend-parallelism:${parallelism}")
    def parCompileDir(directory: String) = compileDir(directory, options)

    // Compilation units containing more than 1 source file
    val compileTests = aggregateTests(
      parCompileDir("tests/pos/i10477"),
      parCompileDir("tests/pos/i4758"),
      parCompileDir("tests/pos/scala2traits"),
      parCompileDir("tests/pos/class-gadt"),
      parCompileDir("tests/pos/tailcall"),
      parCompileDir("tests/pos/reference"),
      parCompileDir("tests/pos/pos_valueclasses")
    ).dynamicTests(_.checkCompile()).asScala

    val expectErrorsTests = aggregateTests(
      parCompileDir("tests/neg/package-implicit"),
      parCompileDir("tests/neg/package-export")
    ).dynamicTests(_.checkExpectedErrors()).asScala

    val checkRunTests = aggregateTests(
      parCompileDir("tests/run/decorators"),
      parCompileDir("tests/run/generic")
    ).dynamicTests(_.checkRuns()).asScala

    (compileTests ++ expectErrorsTests ++ checkRunTests).asJava
  }
}

object CompilationTests extends ParallelTesting {
  // Test suite configuration --------------------------------------------------

  def maxDuration = 45.seconds
  def numberOfWorkers = Runtime.getRuntime().availableProcessors()
  def safeMode = Properties.testsSafeMode
  def isInteractive = false
  def testFilter = Properties.testsFilter
  def updateCheckFiles: Boolean = Properties.testsUpdateCheckfile
  def failedTests = TestReporter.lastRunFailedTests

  implicit val summaryReport: SummaryReporting = new SummaryReport
  @AfterAll def tearDown(): Unit = {
    super.cleanup()
    summaryReport.echoSummary()
  }
}
