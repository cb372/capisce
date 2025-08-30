package http

import katz.effect.*

object Client:
  def get(url: String): Resource[Response] =
    Resource.make(
      IO.pure(Response(body = "hello world!", closed = false))
    )(response =>
      IO.delay(response.closed = true)
    )
