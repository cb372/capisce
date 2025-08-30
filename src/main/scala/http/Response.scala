package http

import katz.effect.IO

import java.io.IOException

case class Response(
    body: String,
    var closed: Boolean
):
  def readBody: IO[String] =
    if (closed)
      IO.raiseError(new IOException("this response is already closed!"))
    else
      IO.pure(body)

