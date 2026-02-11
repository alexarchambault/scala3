package dotty

object Assumptions:

  def assumeFalse(message: String, condition: Boolean) =
    org.junit.jupiter.api.Assumptions.assumeFalse(condition, message)

  def assumeTrue(message: String, condition: Boolean) =
    org.junit.jupiter.api.Assumptions.assumeTrue(condition, message)
