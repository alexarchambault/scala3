package hello

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions._

class HelloTest {
  @Test
  def simpleTest(): Unit = {
    assertEquals(1, 1)
  }
}
