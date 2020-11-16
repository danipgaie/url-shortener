package controllers

import javax.inject.Inject
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._
import resources._

class ShortenerController @Inject()(cc: ControllerComponents, resourceHandler: UrlResourceHandler)
  extends AbstractController(cc) {
  private val logger = Logger(getClass)

  def createShortURL: Action[AnyContent] = Action { implicit request =>
    logger.debug(s"$request ${request.body}")
    case class CreateShortURLInputData(longUrl: String)
    request.body.asJson match {
      case None => BadRequest(Json.obj("error" -> "Expecting application/json request body"))
      case Some(jsonBody) => decode[CreateShortURLInputData](jsonBody.toString()) match {
        case Left(error) => {
          logger.trace(s"Invalid application/json request body: ${error.toString}")
          BadRequest(Json.obj("error" -> "Invalid application/json request body"))
        }
        case Right(data) =>
          resourceHandler.createShortURL(data.longUrl) match {
            case None => {
              logger.error(s"There was a problem encoding the URL: ${data.longUrl}")
              Conflict(Json.obj("error" -> "There was a problem encoding the URL"))
            }
            case Some(shortUrl) => Created(shortUrl.asJson.noSpaces).as("application/json")
          }
      }
    }
  }

  def redirect(slug: String) = Action { implicit request =>
    logger.debug(s"$request")
    resourceHandler.lookup(slug) match {
      case None => NotFound
      case Some(destinationUrl) => MovedPermanently(destinationUrl)
        .withHeaders(CACHE_CONTROL -> "no-cache, no-store, max-age=0, must-revalidate")
    }
  }

}
