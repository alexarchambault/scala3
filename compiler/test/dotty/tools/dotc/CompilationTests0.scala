package dotty
package tools
package dotc

import dotty.tools.dotc.reporting.TestReporter
import dotty.tools.vulpix.*

import scala.concurrent.duration._

class CompilationTests0 extends munit.FunSuite {

  private val parallelTesting: ParallelTesting = new ParallelTesting {
    def maxDuration = CompilationTests.maxDuration
    def numberOfSlaves = CompilationTests.numberOfSlaves
    def safeMode = CompilationTests.safeMode
    def isInteractive = CompilationTests.isInteractive
    def testFilter = CompilationTests.testFilter
    def updateCheckFiles: Boolean = CompilationTests.updateCheckFiles
    def failedTests = CompilationTests.failedTests
  }

  import parallelTesting.CompilationTest.aggregateTests
  import parallelTesting.*
  import TestConfiguration.*

  private var testList = Set.empty[String]
  private val testListLock = new Object
  extension (compileTest: CompilationTest)
    private def declareMunitTests(prefix: String, run: CompilationTest => Unit)(implicit testGroup: TestGroup, loc: munit.Location): Unit =
      compileTest.splitTests(run) { (name, body) =>
        val name0 = s"$testGroup $name"
        testListLock.synchronized {
          if (testList.contains(name0))
            sys.error(s"Test $name0 already declared")
          testList += name0
        }
        test(name0) {
          body()
        }
      }

  private implicit val summaryReport: SummaryReporting = new SummaryReport

  // Positive tests ------------------------------------------------------------

  locally {
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
      // Run tests for legacy lazy vals
      compileFilesInDir("tests/pos", defaultOptions.and("-Wsafe-init", "-Ylegacy-lazy-vals", "-Ycheck-constraint-deps"), FileFilter.include(TestSources.posLazyValsAllowlist)),
      compileDir("tests/pos-special/java-param-names", defaultOptions.withJavacOnlyOptions("-parameters")),
    ) ::: (
      // TODO create a folder for capture checking tests with the stdlib, or use tests/pos-custom-args/captures under this mode?
      if Properties.usingScalaLibraryCCTasty then List(compileDir("tests/pos-special/stdlib", allowDeepSubtypes))
      else Nil
    )

    if scala.util.Properties.isJavaAtLeast("16") then
      tests ::= compileFilesInDir("tests/pos-java16+", defaultOptions.and("-Wsafe-init"))

    aggregateTests(tests*).declareMunitTests("pos", _.checkCompile())
  }


  locally {
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
      compileFile("tests/rewrites/refutable-pattern-bindings.scala", defaultOptions.and("-rewrite", "-source", "3.2-migration")),
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
    ).declareMunitTests("rewrite", _.checkRewrites())
  }


  locally {
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
    ).declareMunitTests("pos twice", _.times(2).checkCompile())
  }


  // Warning tests ------------------------------------------------------------

  locally {
    implicit val testGroup: TestGroup = TestGroup("compileWarn")
    aggregateTests(
      compileFilesInDir("tests/warn", defaultOptions),
    ).declareMunitTests("warn", _.checkWarnings())
  }


  // Negative tests ------------------------------------------------------------

  locally {
    implicit val testGroup: TestGroup = TestGroup("compileNeg")
    aggregateTests(
      compileFilesInDir("tests/neg", defaultOptions, FileFilter.exclude("i7575.scala" :: TestSources.negScala2LibraryTastyExcludelisted)),
      compileFilesInDir("tests/neg-deep-subtype", allowDeepSubtypes),
      compileFilesInDir("tests/neg-custom-args/captures", defaultOptions.and("-language:experimental.captureChecking", "-language:experimental.separationChecking", "-source", "3.8")),
      compileFile("tests/neg-custom-args/sourcepath/outer/nested/Test1.scala", defaultOptions.and("-sourcepath", "tests/neg-custom-args/sourcepath")),
      compileDir("tests/neg-custom-args/sourcepath2/hi", defaultOptions.and("-sourcepath", "tests/neg-custom-args/sourcepath2", "-Werror")),
      compileList("duplicate source", List(
        "tests/neg-custom-args/toplevel-samesource/S.scala",
        "tests/neg-custom-args/toplevel-samesource/nested/S.scala"),
        defaultOptions),
      compileFile("tests/neg/i7575.scala", defaultOptions.withoutLanguageFeatures),
    ).declareMunitTests("neg", _.checkExpectedErrors())
  }


  locally {
    implicit val testGroup: TestGroup = TestGroup("compileFuzzy")
    compileFilesInDir("tests/fuzzy", defaultOptions).declareMunitTests("fuzzy", _.checkNoCrash())
  }


  // Generic java signatures tests ---------------------------------------------

  locally {
    implicit val testGroup: TestGroup = TestGroup("genericJavaSignatures")
    compileFilesInDir("tests/generic-java-signatures", defaultOptions).declareMunitTests("java sig", _.checkRuns())
  }


  // Pickling Tests ------------------------------------------------------------

  locally {
    implicit val testGroup: TestGroup = TestGroup("testPickling")
    aggregateTests(
      compileFilesInDir("tests/pos", picklingOptions, FileFilter.exclude(TestSources.posTestPicklingExcludelisted)),
      compileFilesInDir("tests/run", picklingOptions, FileFilter.exclude(TestSources.runTestPicklingExcludelisted))
    ).declareMunitTests("picking", _.checkCompile())
  }


  //@Test disabled in favor of posWithCompilerCC to save time.
  locally {
    given TestGroup = TestGroup("recheck")
    aggregateTests(
      compileFilesInDir("tests/run", defaultOptions.and("-Yrecheck-test"), FileFilter.exclude(TestSources.runTestRecheckExcluded))
      //Disabled to save some time.
      //compileFilesInDir("tests/pos", recheckOptions, FileFilter.exclude(TestSources.posTestRecheckExcluded)),
    ).declareMunitTests("recheck", _.checkCompile())
  }

  // Explicit nulls tests
  locally {
    implicit val testGroup: TestGroup = TestGroup("explicitNullsNeg")
    aggregateTests(
      compileFilesInDir("tests/explicit-nulls/neg", explicitNullsOptions, FileFilter.exclude(TestSources.negExplicitNullsScala2LibraryTastyExcludelisted)),
      compileFilesInDir("tests/explicit-nulls/flexible-types-common", explicitNullsOptions `and` "-Yno-flexible-types"),
      compileFilesInDir("tests/explicit-nulls/unsafe-common", explicitNullsOptions `and` "-Yno-flexible-types", FileFilter.exclude(TestSources.negExplicitNullsScala2LibraryTastyExcludelisted)),
    ).declareMunitTests("explicitNullsNeg", _.checkExpectedErrors())

    // locally {
    //   val unsafeFile = compileFile("tests/explicit-nulls/flexible-unpickle/neg/Unsafe_1.scala", explicitNullsOptions without "-Yexplicit-nulls")
    //   val flexibleFile = compileFile("tests/explicit-nulls/flexible-unpickle/neg/Flexible_2.scala",
    //       explicitNullsOptions.and("-Yflexify-tasty").withClasspath(defaultOutputDir + testGroup + "/Unsafe_1/neg/Unsafe_1"))

    //   unsafeFile.keepOutput.checkCompile()
    //   flexibleFile.keepOutput.checkExpectedErrors()

    //   List(unsafeFile, flexibleFile).foreach(_.delete())
    // }
  }

  locally {
    implicit val testGroup: TestGroup = TestGroup("explicitNullsPos")
    aggregateTests(
      compileFilesInDir("tests/explicit-nulls/pos", explicitNullsOptions),
      compileFilesInDir("tests/explicit-nulls/flexible-types-common", explicitNullsOptions),
      compileFilesInDir("tests/explicit-nulls/unsafe-common", explicitNullsOptions `and` "-language:unsafeNulls" `and` "-Yno-flexible-types"),
    ).declareMunitTests("explicitNullsPos", _.checkCompile())

    // locally {
    //   val tests = List(
    //     compileFile("tests/explicit-nulls/flexible-unpickle/pos/Unsafe_1.scala", explicitNullsOptions without "-Yexplicit-nulls"),
    //     compileFile("tests/explicit-nulls/flexible-unpickle/pos/Flexible_2.scala",
    //     explicitNullsOptions.and("-Yflexify-tasty").withClasspath(defaultOutputDir + testGroup + "/Unsafe_1/pos/Unsafe_1")),
    //   ).map(_.keepOutput.checkCompile())

    //   tests.foreach(_.delete())
    // }
  }

  locally {
    implicit val testGroup: TestGroup = TestGroup("explicitNullsWarn")
    compileFilesInDir("tests/explicit-nulls/warn", explicitNullsOptions)
      .declareMunitTests("explicitNullsWarn", _.checkWarnings())
  }

  locally {
    implicit val testGroup: TestGroup = TestGroup("explicitNullsRun")
    compileFilesInDir("tests/explicit-nulls/run", explicitNullsOptions)
      .declareMunitTests("explicitNullsRun", _.checkRuns())
  }

  // initialization tests
  locally {
    implicit val testGroup: TestGroup = TestGroup("checkInitGlobal")
    compileFilesInDir("tests/init-global/warn", defaultOptions.and("-Ysafe-init-global"), FileFilter.exclude(TestSources.negInitGlobalScala2LibraryTastyExcludelisted))
      .declareMunitTests("checkInitGlobal warnings", _.checkWarnings())
    compileFilesInDir("tests/init-global/pos", defaultOptions.and("-Ysafe-init-global", "-Werror"), FileFilter.exclude(TestSources.posInitGlobalScala2LibraryTastyExcludelisted))
      .declareMunitTests("checkInitGlobal compile", _.checkCompile())

    if Properties.usingScalaLibraryTasty && !Properties.usingScalaLibraryCCTasty then
      compileFilesInDir("tests/init-global/warn-tasty", defaultOptions.and("-Ysafe-init-global"), FileFilter.exclude(TestSources.negInitGlobalScala2LibraryTastyExcludelisted))
        .declareMunitTests("checkInitGlobal tasty warnings", _.checkWarnings())
      compileFilesInDir("tests/init-global/pos-tasty", defaultOptions.and("-Ysafe-init-global", "-Werror"), FileFilter.exclude(TestSources.posInitGlobalScala2LibraryTastyExcludelisted))
        .declareMunitTests("checkInitGlobal tasty compile", _.checkCompile())
    end if
  }


  locally {
    implicit val testGroup: TestGroup = TestGroup("runAll")
    aggregateTests(
      compileFilesInDir("tests/run", defaultOptions.and("-Wsafe-init")),
      compileFilesInDir("tests/run-deep-subtype", allowDeepSubtypes),
      compileFilesInDir("tests/run-custom-args/captures", allowDeepSubtypes.and("-language:experimental.captureChecking", "-language:experimental.separationChecking", "-source", "3.8")),
    ).declareMunitTests("run", _.checkRuns())
  }

  locally {
    implicit val testGroup: TestGroup = TestGroup("runAllLegacy")
    aggregateTests(
      // Run tests for legacy lazy vals.
      compileFilesInDir("tests/run", defaultOptions.and("-Wsafe-init", "-Ylegacy-lazy-vals", "-Ycheck-constraint-deps"), FileFilter.include(TestSources.runLazyValsAllowlist)),
    ).declareMunitTests("run", _.checkRuns())
  }

  // parallel backend tests
  locally {
    given TestGroup = TestGroup("parallelBackend")
    val parallelism = Runtime.getRuntime().availableProcessors().min(16)
    if (parallelism <= 1)
      System.err.println("Not enough available processors to run parallel tests")
    else {
      val options = defaultOptions.and(s"-Ybackend-parallelism:${parallelism}")
      def parCompileDir(directory: String) = compileDir(directory, options)

      // Compilation units containing more than 1 source file
      aggregateTests(
        parCompileDir("tests/pos/i10477"),
        parCompileDir("tests/pos/i4758"),
        parCompileDir("tests/pos/scala2traits"),
        parCompileDir("tests/pos/class-gadt"),
        parCompileDir("tests/pos/tailcall"),
        parCompileDir("tests/pos/reference"),
        parCompileDir("tests/pos/pos_valueclasses")
      ).declareMunitTests("parallelBackend compile", _.checkCompile())

      aggregateTests(
        parCompileDir("tests/neg/package-implicit"),
        parCompileDir("tests/neg/package-export")
      ).declareMunitTests("parallelBackend expectError", _.checkExpectedErrors())

      aggregateTests(
        parCompileDir("tests/run/decorators"),
        parCompileDir("tests/run/generic")
      ).declareMunitTests("parallelBackend checkRun", _.checkRuns())
    }
  }
}
