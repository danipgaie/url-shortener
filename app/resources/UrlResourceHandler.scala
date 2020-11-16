package resources

import javax.inject.Inject
import repository._
import utils._

class UrlResourceHandler @Inject()(repository: UrlRepository) {
  def createShortURL(longUrl: String): Option[UrlResource] = {
    repository.getNextId match {
      case None => None
      case Some(id) => {
        val encodedId = Utils.idEnconder.encode(id)
        val urlResource = UrlResource(longUrl,
                                      Config.SHORT_URL_DOMAIN + encodedId,
                                      Utils.getCurrentUTCTimeString)
        if (repository.setURL(id, urlResource))
          Option(urlResource)
        else
          None
      }
    }
  }

  def lookup(slug: String): Option[String] = {
    Utils.idEnconder.decode(slug) match {
      case Nil => None
      case id :: _ => repository.getURL(id) match {
        case None => None
        case Some(data) => Option(data.destinationUrl)
      }
    }
  }
}
