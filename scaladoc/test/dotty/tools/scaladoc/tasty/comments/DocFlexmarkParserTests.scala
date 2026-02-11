package dotty.tools.scaladoc
package tasty.comments

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.{assertSame, assertTrue, assertEquals}
import dotty.tools.scaladoc.tasty.comments.markdown.DocFlexmarkParser

class DocFlexmarkParserTests {
  @Test def test(): Unit = {
    assertEquals(("a", "b c d"), DocFlexmarkParser.splitWikiLink("a b c d"))
    assertEquals(("a", "b\\ c d"), DocFlexmarkParser.splitWikiLink("a b\\ c d"))
    assertEquals(("a\\ b", "c d"), DocFlexmarkParser.splitWikiLink("a\\ b c d"))
    assertEquals(("a\\\\", "b c d"), DocFlexmarkParser.splitWikiLink("a\\\\ b c d"))
  }
}
