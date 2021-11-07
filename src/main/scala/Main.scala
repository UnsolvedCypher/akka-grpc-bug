import akka.actor.ActorSystem
import akka.grpc.scaladsl.{ServerReflection, ServiceHandler, WebHandler}
import akka.http.scaladsl.Http
import com.example.protos.{Hello, HelloHandler}
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}

object Main {

  def main(args: Array[String]): Unit = {
    // Important: enable HTTP/2 in ActorSystem's config
    // We do it here programmatically, but you can also set it in the application.conf
    val conf = ConfigFactory
      .parseString("akka.http.server.preview.enable-http2 = on")
      .withFallback(ConfigFactory.defaultApplication())
    val system = ActorSystem("HelloWorld", conf)
    new Main(system).run()
    // ActorSystem threads will keep the app alive until `system.terminate()` is called
  }
}
class Main(system: ActorSystem) {
  def run(): (Future[Http.ServerBinding], Future[Http.ServerBinding]) = {
    // Akka boot up code
    implicit val sys: ActorSystem = system
    implicit val ec: ExecutionContext = sys.dispatcher

    val helloService = HelloHandler.partial(new HelloImpl())

    val reflectionService = ServerReflection.partial(List(
      Hello
    ))

    val serviceHandlers = ServiceHandler.concatOrNotFound(
        helloService,
        reflectionService
      )

    // Bind service handler servers to localhost:8080/8081
    val binding = Http().newServerAt("127.0.0.1", 8080).bind(serviceHandlers)

    // report successful binding
    binding.foreach { binding => println(s"regular gRPC server bound to: ${binding.localAddress}") }

    val grpcWebServiceHandlers = WebHandler.grpcWebHandler(
      reflectionService,
      helloService,
    )

    val grpcWebBinding = Http().newServerAt("127.0.0.1", 8081).bind(grpcWebServiceHandlers)

    grpcWebBinding.foreach { webBinding => println(s"gRPC-web server bound to: ${webBinding.localAddress}") }

    (binding, grpcWebBinding)
  }
}
