package repository

import javax.inject.Singleton
import com.redis._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser._
import play.api.Logger
import utils.Config

@Singleton
class RedisUrlRepository extends UrlRepository {
  private val logger = Logger(getClass)
  val SLUG_ID_KEY = "slug-id:"
  val UNIQUE_ID_KEY = "long-url-id:id"
  val clients = new RedisClientPool(Config.REDIS_HOST, Config.REDIS_PORT)

  def setURL(id: Long, data: UrlResource): Boolean = {
    val key = SLUG_ID_KEY + id
    val value = data.asJson.noSpaces
    logger.debug(s"Redis setnx slug.key=$key value=$value")
    clients.withClient(_.setnx(key, value))
  }

  def getURL(id: Long): Option[UrlResource] = {
    val key = SLUG_ID_KEY + id
    logger.debug(s"Redis get slug.key=$key")
    clients.withClient {
      _.get[String](key) match {
          case None => None
          case Some(value) => decode[UrlResource](value) match {
            case Left(error) => None
            case Right(resource) => Option(resource)
          }
        }
    }
  }

  def getNextId: Option[Long] = {
    val key = UNIQUE_ID_KEY
    logger.debug(s"Redis incr key=$key")
    clients.withClient(_.incr(key))
  }
}
