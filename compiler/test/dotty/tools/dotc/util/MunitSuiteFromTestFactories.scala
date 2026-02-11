package dotty.tools.dotc.util

import dotty.Properties
import org.junit.jupiter.api.DynamicTest

import java.lang.{Iterable => JIterable}

import scala.jdk.CollectionConverters.*

abstract class MunitSuiteFromTestFactories extends munit.FunSuite {

  private var testList = Set.empty[String]
  private val testListLock = new Object

  def dynTests: Seq[(String, JIterable[DynamicTest])]

  for {
    (testGroup, dynTests) <- dynTests
    dynTest <- dynTests.asScala.toVector
  } {
    val name = s"$testGroup ${dynTest.getDisplayName}"
    val finalName = testListLock.synchronized {
      var name0 = name
      var idx = 0
      while (testList.contains(name0)) {
        idx += 1
        name0 = s"$name $idx"
      }
      testList += name0
      name0
    }
    test(finalName) {
      if (!Properties.isRunByCI)
        System.err.println(s"Running ${Console.BLUE}$finalName${Console.RESET}")
      try dynTest.getExecutable.execute()
      catch {
        case t: Throwable =>
          System.err.println(s"Failed: ${Console.RED}$finalName${Console.RESET}")
          throw t
      }
    }
  }

}
