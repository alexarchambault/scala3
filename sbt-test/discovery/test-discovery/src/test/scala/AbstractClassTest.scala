import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

// Test discovery should not pick this up because it's abstract
abstract class AbstractClassTest {
  @Test def foo = {
    ???
  }
}
