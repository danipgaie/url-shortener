# Interview Project: URL "shortener" service

## Objective
Design a URL "shortener" service. We don't need a GUI or user authentication.
The service should just expose two endpoints:
* a POST to create the minified url, and
* a GET to resolve it
Use whatever technologies and architecture you think are best suited for the task.

## Solution
The solutions was implemented using Scala and Play framework for the REST API
and redis as a database.

__Creating a short URL__

Each time a short URL is generated, a new number is generated (increased atomically by one, and stored in redis).
Using that number, the `slug` (short "code" for the URL), is created using _hashids_ (https://hashids.org/javascript/).
_Hashids_ provides short, unique, non-sequential ids based on number.

That way, even if the same URL is "shortened" more than once, each time it will generate a different short URL.

The database is then storing: the long URL, the short URL and the data it was created (for possibly adding an expiry date 
and then removing old entries from database).
Using redis as a database would make sharding easier than a SQL database and improve scaling.

__Retrieve long URL__

When the _GET_ endpoint it's called, the `slug` (received as a parameter in the URL) is then decoded (using _hashids_) so data containing the long URL 
can be retrieved from the database.

With the full URL a 
[HTTP 301 Moved Permanently](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/301) is sent as a response 
to the user.
  

### Requirements
* [Scala interactive build tool](https://www.scala-sbt.org/) `sbt`.
* Redis server: needs to be running in the host and port configured in `application.conf`.
You can use [docker](https://www.docker.com/) (with dockerhub https://hub.docker.com/_/redis) and
run a redis server locally on port `6379` by running the following command
```
docker run --name redis-server -d redis -p6379:6379
```

### Scala dependencies
- [Play framework](https://www.playframework.com/)
- [pico-hashids](https://github.com/pico-works/pico-hashids)
- [Circe](https://github.com/circe/circe)
- [Redis](https://github.com/debasishg/scala-redis)
- [Mockito](https://github.com/mockito/mockito)

### Running
You can use `sbt` for running the application (after running redis server) with the following command:
```
sbt run
```

### Examples of usage
 * _POST /v1/shorten_: creates a new short URL for a long url.
   * Request:
     ```
     curl -H "Content-Type: application/json" \
         -d '{"longUrl": "https://www.google.com"}' \
         http://localhost:9000/v1/shorten
     ```
   * Response:
     ```
     {
       "destinationUrl":"https://www.google.com",
       "shortUrl":"http://localhost:9000/g/7dzX0b",
       "creationDate":"2020-11-15T11:29:29.647+0000"
     }
     ``` 
 * _GET https://www.google.com/g/<short-slug>_
   * Request:
     ```
     curl http://localhost:9000/g/7dzX0b
     ```
   * Response:
     ```
     HTTP 301 Moved Permanently: https://www.google.com
     ```

## TODOs

* Consider an expiration for the stored short URLs, based on last usage or date of creation
* Add metrics of usage for _GET_ and _POST_ endpoints
* Improve handling of errors 
* Consider the possibility of reducing the amount of data stored in the database by storing only the slug 
  in the database (not the full short URL), if the URL domain will always be the same.

## References
* https://github.com/marcogalluzzi/scala-url-shortener