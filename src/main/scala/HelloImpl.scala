import com.example.protos.{Hello, HelloReply, HelloRequest}

import scala.concurrent.Future

class HelloImpl() extends Hello {
  override def sayHello(in: HelloRequest): Future[HelloReply] = {
    println("Hello world, " + in.name)
    Future.successful(HelloReply("Hello, world, " + in.name))
  }
}
