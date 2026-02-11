package dotty.tools.scaladoc
package site

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions._

class BlogParserTest:

  private val blogConfig =
    """input: blog
      |output: blog
      |hidden: false
      |""".stripMargin

  @Test
  def loadBlog(): Unit = assertEquals(
    BlogConfig("blog", "blog", false),
    BlogParser.readYml(blogConfig)
  )