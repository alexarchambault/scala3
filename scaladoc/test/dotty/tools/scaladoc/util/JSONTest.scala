package dotty.tools.scaladoc
package util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions._

class JSONTest:
  @Test
  def testStrings =
    assertEquals(quoteStr("""ala"""), jsonString("""ala"""))
    assertEquals(quoteStr("""\""""), jsonString("""""""))