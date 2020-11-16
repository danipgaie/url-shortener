package repository

case class UrlResource(destinationUrl: String, shortUrl: String, creationDate: String)

trait UrlRepository {
  def setURL(id: Long, data: UrlResource): Boolean
  def getURL(id: Long): Option[UrlResource]
  def getNextId: Option[Long]
}
