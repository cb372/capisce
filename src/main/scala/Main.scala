import katz.effect.*
import http.*

@main def run =
  val resource = Client.get("google.com")

  val goodEffect = resource
    .use { response =>
      response.readBody.flatMap(body =>
        IO.delay(println(body))
      )
    }

  val badEffect = resource
    .use(IO.pure(_)) // whoops! the response has escaped the Resource#use block
    .flatMap { escapedResponse =>
      // we're now working with a response that's already been closed
      escapedResponse.readBody.flatMap(body =>
        IO.delay(println(body))
      )
    }

  println("Running the good effect")
  goodEffect.unsafeRunSync()

  println("Running the bad effect")
  badEffect.unsafeRunSync()

