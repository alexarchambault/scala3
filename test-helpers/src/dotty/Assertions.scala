package dotty

object Assertions:

  def assertTrue(message: String, condition: Boolean) =
    org.junit.jupiter.api.Assertions.assertTrue(condition, message)

  def assertTrue(condition: Boolean) =
    org.junit.jupiter.api.Assertions.assertTrue(condition)

  def assertFalse(message: String, condition: Boolean) =
    org.junit.jupiter.api.Assertions.assertFalse(condition, message)

  def assertFalse(condition: Boolean) =
    org.junit.jupiter.api.Assertions.assertFalse(condition)

  def assertEquals[T](message: String, a: T, b: T) =
    org.junit.jupiter.api.Assertions.assertEquals(a, b, message)

  def assertEquals[T](a: T, b: T) =
    org.junit.jupiter.api.Assertions.assertEquals(a, b)

  def assertSame[T](message: String, a: T, b: T) =
    org.junit.jupiter.api.Assertions.assertSame(a, b, message)

  def assertSame[T](a: T, b: T) =
    org.junit.jupiter.api.Assertions.assertSame(a, b)

  def assertNotNull(message: String, value: Object) =
    org.junit.jupiter.api.Assertions.assertNotNull(value, message)

  def assertNotNull(value: Object) =
    org.junit.jupiter.api.Assertions.assertNotNull(value)

  def assertNotEquals[T](message: String, a: T, b: T) =
    org.junit.jupiter.api.Assertions.assertNotEquals(a, b, message)

  def assertNotEquals[T](a: T, b: T) =
    org.junit.jupiter.api.Assertions.assertNotEquals(a, b)

  def fail(message: String) =
    org.junit.jupiter.api.Assertions.fail(message)
