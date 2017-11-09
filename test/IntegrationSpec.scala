package test

import play.api.test.Helpers._

class IntegrationSpec extends TestUtils {

  "Get by company number" should {

    "return correct company" in {
      val companyNumber = "04988527"
      val id = "123412341234"
      val res = fakeRequest(s"/v1/period/${companyNumber}/id/${id}")
      status(res) mustBe UNPROCESSABLE_ENTITY
      contentType(res) mustBe Some("application/json")
    }

    "return 404 if company is not found" in {
      val companyNumber = "12345678"
      val id = "123412341234"
      val res = fakeRequest(s"/v1/period/${companyNumber}/id/${id}")
      status(res) mustBe UNPROCESSABLE_ENTITY
      contentType(res) mustBe Some("application/json")
    }
  }
}
