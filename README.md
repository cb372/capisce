Experimenting with capture checking to make Cats Effect's Resource safer.

## Problem statement

When people are new to CE `Resource`, they often accidentally "escape" the value
from the `use` block and then interact with it after it's been released.

A common example is trying to read the body of an http4s `Response` after it's
been released.

In this repo I've made minimal implementations to represent CE's `IO` and
`Resource`, and http4s's `Client` and `Response`, to demonstrate the problem.
Everything is hardcoded to `IO` for brevity.

```scala
import katz.effect.*
import http.*

val resource: Resource[Response] = Client.get("google.com")

val badEffect = resource
.use(IO.pure(_)) // whoops! the response has escaped the Resource#use block
.flatMap { escapedResponse =>
  // we're now working with a response that's already been closed
  escapedResponse.readBody.flatMap(body =>
    IO.delay(println(body))
  )
}
```
