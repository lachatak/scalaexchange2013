package core

import spray.httpx.unmarshalling.{MalformedContent, Unmarshaller, Deserialized}
import spray.http._
import spray.json._
import spray.client.pipelining._
import java.text.SimpleDateFormat
import akka.actor.{ActorRef, Actor}
import spray.http.HttpRequest
import scala.Some
import domain.Tweet

trait TweetMarshaller {

  implicit object TweetUnmarshaller extends Unmarshaller[Tweet] {

    val dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy")

    def apply(entity: HttpEntity): Deserialized[Tweet] = {
      val json = JsonParser(entity.asString).asJsObject
      (json.fields.get("id_str"), json.fields.get("text"), json.fields.get("created_at"), json.fields.get("user")) match {
        case (Some(JsString(id)), Some(JsString(text)), Some(JsString(createdAt)), Some(JsObject(user))) =>
          user.get("id_str") match {
            case Some(JsString(userId)) => Right(Tweet(id, userId, text, dateFormat.parse(createdAt)))
            case _                      => Left(MalformedContent("Bad tweet JSON"))
          }
        case _                          => Left(MalformedContent("Bad status JSON"))
      }
    }
  }

}

object TweetStreamerActor {
  val twitterUri = Uri("https://stream.twitter.com/1.1/statuses/filter.json")
}

class TweetStreamerActor(io: ActorRef, uri: Uri, processor: ActorRef) extends Actor with TweetMarshaller {
  import OAuth._

  val oAuthAuthorize = oAuthAuthorizer(Consumer("hVdsWmYtTW4TnaUPVzqkA", "63Ir9LQVFV7CA0Oj69hs0Xvew6o88WWvXMN1qK6C0"),
    Token("29976216-JP2lKH1CKzQ7G467dGh0TJ9WeGUjmnWQvZ5GwnBR2", "3ILPoYz0hjrFpMo33JxwqbJhfm52fF0qUBTybgUNqrZwD"))
  val tweetUnmarshaller = unmarshal[Tweet]

  def receive: Receive = {
    case query: String =>
      val post = HttpEntity(ContentType(MediaTypes.`application/x-www-form-urlencoded`), s"track=$query")
      val rq = HttpRequest(HttpMethods.POST, uri = uri, entity = post) ~> oAuthAuthorize
      sendTo(io).withResponsesReceivedBy(self)(rq)
    case ChunkedResponseStart(_) =>
    case MessageChunk(entity, _) =>
      val tweet = tweetUnmarshaller(HttpResponse(entity = entity))
      processor ! tweet
    case _ =>
  }

}