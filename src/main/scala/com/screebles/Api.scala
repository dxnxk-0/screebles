package com.screebles

import akka.actor.Actor
import net.liftweb.json._
import redis.clients.jedis.{GeoUnit, Jedis}
import spray.http.MediaTypes._
import spray.http.StatusCodes
import spray.httpx.unmarshalling.Unmarshaller
import spray.routing._
import scala.collection.JavaConverters._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class ApiActor extends Actor with Api {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(apiRoute)
}


// this trait defines our service behavior independently from the service actor
trait Api extends HttpService {

  val redis = new Jedis("localhost")

  implicit val jsonReader = Unmarshaller.delegate[String, JValue](`application/json`) { json =>
    parseOpt(json).getOrElse(throw new IllegalArgumentException("The json document is not valid"))
  }

  implicit val formats = DefaultFormats

  val apiRoute =
    pathPrefix("api") {
      respondWithMediaType(`application/json`) {
        pathPrefix("walls") {
          post {
            pathEndOrSingleSlash {
              entity(as[JValue]) { json =>
                redis.geoadd((json \ "name").extract[String], 0.0, 0.0, "_")
                complete(StatusCodes.NoContent)
              }
            }
          }
        } ~
        pathPrefix("screebles") {
          post {
            pathEndOrSingleSlash {
              entity(as[JValue]) { json =>
                redis.geoadd((json \ "wall").extract[String], (json \ "lon").extract[Double], (json \ "lat").extract[Double], (json \ "screeble").extract[String])
                complete(StatusCodes.NoContent)
              }
            }
          } ~
          get {
            parameters('wall.as[String], 'lat.as[Double], 'lon.as[Double], 'radius.as[Double]) { (wall, lat, lon, radius) =>
              complete {
                prettyRender {
                  JArray(redis.georadius(wall, lon, lat, radius, GeoUnit.KM).asScala.map(s => JString(s.getMemberByString)).toList)
                }
              }
            }
          }
        }
      }
    }
}