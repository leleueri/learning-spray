package spray.examples

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import com.sun.org.apache.xalan.internal.xsltc.compiler.Param
import akka.actor.Props
import spray.util.SprayActorLogging
import scala.concurrent.duration._
import spray.can.Http
import spray.util._
import spray.http._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {

  val myRoute =
    path("") {
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Say hello to <i>spray-routing</i> on <i>spray-can</i>!</h1>
              </body>
            </html>
          }
        }
      }
    } ~ path("headers") {
      get {
        extract(_.request.headers) {
        	h =>
        	// serialize headers in the response body
        	complete (h.mkString)
        }
      }
    } ~ path("params") {
      get {
        parameter('p1, 'pOpt ?) {  	(param1, paramOptional) =>
        // use Symbol to name query parameters
       	complete ( "Mandatory param p1[" + param1 + "] / Optional param pOpt[" +  paramOptional.getOrElse("MissingParam") + "]")
        }
      }
    } ~
      path("stream1") {
       get {
       respondWithMediaType(`text/html`) {
          // we detach in order to move the blocking code inside the simpleStringStream off the service actor
          detachTo(singleRequestServiceActor) {
            complete(simpleStringStream)
          }
        }
       }
      } ~
      path("stream2") {
        get {
            sendStreamingResponse
        }
      }
    
    // TODO try to return an Object serialized in JSON and XML
      
      
      
      
      
  /////////////////////////////////
  // Take from the spray example //  
  /////////////////////////////////
    
    
  def simpleStringStream: Stream[String] = {
    val secondStream = Stream.continually {
      // CAUTION: we block here to delay the stream generation for you to be able to follow it in your browser,
      // this is only done for the purpose of this demo, blocking in actor code should otherwise be avoided
      Thread.sleep(500)
      "<li>" + DateTime.now.toIsoDateTimeString + "</li>"
    }
    streamStart #:: secondStream.take(15) #::: streamEnd #:: Stream.empty
  }
    
  // we prepend 2048 "empty" bytes to push the browser to immediately start displaying the incoming chunks
  lazy val streamStart = " " * 2048 + "<html><body><h2>A streaming response</h2><p>(for 15 seconds)<ul>"
  lazy val streamEnd = "</ul><p>Finished.</p></body></html>"
    
  // simple case class whose instances we use as send confirmation message for streaming chunks
  case class Ok(remaining: Int)

  def sendStreamingResponse(ctx: RequestContext): Unit =
    actorRefFactory.actorOf {
      Props {
        new Actor with SprayActorLogging {
          // we use the successful sending of a chunk as trigger for scheduling the next chunk
          val responseStart = HttpResponse(entity = HttpEntity(`text/html`, streamStart))
          // Start the chuncked response and notify that 16 chunk will be sent
          ctx.responder ! ChunkedResponseStart(responseStart).withAck(Ok(16))

          // the Stream responder is an actor
          def receive = {
            // on OK message with 0, the stream is finished, send the last chunck and stop the actor
            case Ok(0) =>
              ctx.responder ! MessageChunk(streamEnd)
              ctx.responder ! ChunkedMessageEnd
              context.stop(self)

            // on OK message with non 0, the stream isn't finished, send a chunck
            case Ok(remaining) =>
              {      Thread.sleep(500)
                val nextChunk = MessageChunk("<li>" + DateTime.now.toIsoDateTimeString + "</li>")
                ctx.responder ! nextChunk.withAck(Ok(remaining - 1))
              }

            case ev: Http.ConnectionClosed =>
              log.warning("Stopping response streaming due to {}", ev)
          }
        }
      }
    }


}

