package dotty.tools
package repl

import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

class JavaDefinedTests extends ReplTest {
  @Test def typeOfJavaDefinedString = initially {
    run("String")
    assertTrue(storedOutput().contains("Java defined class String is not a value"))
  }

  @Test def typeOfJavaDefinedClass = initially {
    run("Class")
    assertTrue(storedOutput().contains("Java defined class Class is not a value"))
  }
}
