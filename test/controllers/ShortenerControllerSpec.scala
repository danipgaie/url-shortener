package controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.mvc.{Result, Results}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, _}
import repository.{UrlRepository, UrlResource}
import resources.UrlResourceHandler
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.concurrent.Future

case class JsonResponse(destinationUrl: String, shortUrl: String, creationDate: String)

class ShortenerControllerSpec extends PlaySpec with Results with MockitoSugar with GuiceOneAppPerSuite {
  val repository = mock[UrlRepository]
  val controller = new ShortenerController(Helpers.stubControllerComponents(), new UrlResourceHandler(repository))
  "ShortenerController#createShortURL" should {
    "should return error if no JSON is received" in {
      val result: Future[Result] = controller.createShortURL().apply(FakeRequest(POST, ""))
      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustBe "{\"error\":\"Expecting application/json request body\"}"
    }
    "should return error if invalid JSON is received" in {
      val result = controller.createShortURL().apply(
        FakeRequest(POST, "/").withJsonBody(Json.parse("""{ "field": "value" }"""))
      )
      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual "{\"error\":\"Invalid application/json request body\"}"
    }
    "should return shortened URL" in {
      val longUrl = "www.google.com"
      when(repository.getNextId) thenReturn Option(1)
      when(repository.setURL(any[Long], any[UrlResource])) thenReturn true
      val result = controller.createShortURL().apply(
        FakeRequest(POST, "/").withJsonBody(Json.parse(s"""{ "longUrl": "$longUrl" }"""))
      )
      status(result) mustEqual CREATED
      decode[JsonResponse](contentAsString(result)) match {
        case Left(error) => withClue(s"unexpected result format:${contentAsString(result)}, error=$error") {}
        case Right(response) => {
          response.destinationUrl mustEqual longUrl
          response.creationDate must not be null
          response.shortUrl must not be null
        }
      }
    }
  }
  "ShortenerController#redirect" should {
    "should return error if empty parameter" in {
      val result: Future[Result] = controller.redirect("").apply(FakeRequest())
      status(result) mustEqual NOT_FOUND
    }
    "should redirect" in {
      val slug = "w03LAa"
      when(repository.getURL(any[Long])) thenReturn Option(UrlResource("www.google.es", "QWERTY", "2020..."))
      val result: Future[Result] = controller.redirect(slug).apply(FakeRequest(GET, s"/g/$slug"))
      status(result) mustEqual MOVED_PERMANENTLY
    }
  }
}
