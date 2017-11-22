package test

import play.api.test.Helpers._

class IntegrationSpec extends TestUtils {

  "Get by company number" should {

    "return correct company" in {
      val period = "04988527"
      val id = "123412341234"
      val res = fakeRequest(s"/v1/period/${period}/id/${id}")
      status(res) mustBe UNPROCESSABLE_ENTITY
      contentType(res) mustBe Some("application/json")
    }

    "return 404 if business is not found" in {
      val period = "12345678"
      val id = "123412341234"
      val res = fakeRequest(s"/v1/period/${period}/id/${id}")
      status(res) mustBe UNPROCESSABLE_ENTITY
      contentType(res) mustBe Some("application/json")
    }

    "return 422 if ubrn is not the correct length" in {
      val period = "201706"
      val id = "123"
      val res = fakeRequest(s"/v1/period/${period}/id/${id}")
      status(res) mustBe UNPROCESSABLE_ENTITY
      contentType(res) mustBe Some("application/json")
    }

    "return 422 if period is not the correct format" in {
      val period = "20176"
      val id = "123412341234"
      val res = fakeRequest(s"/v1/period/${period}/id/${id}")
      status(res) mustBe UNPROCESSABLE_ENTITY
      contentType(res) mustBe Some("application/json")
    }

  }
}
