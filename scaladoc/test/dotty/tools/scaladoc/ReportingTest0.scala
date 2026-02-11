package dotty.tools.scaladoc

class ReportingTest0 extends munit.FunSuite {
  private lazy val reportingTest = new ReportingTest

  test("testErrorInCaseOfDocsShadowing") {
    reportingTest.testErrorInCaseOfDocsShadowing
  }
}
