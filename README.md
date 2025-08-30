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

## Solving it with capture checking

We can use capture checking to solve this by telling `Resource#use` to track the
capture of the value passed to `f`.

Before:

```scala
trait Resource[A]:
  def use[B](f: A => IO[B]): IO[B]
```

After:

```scala
import language.experimental.captureChecking

trait Resource[A]:
  def use[B](f: A^ => IO[B]): IO[B]
```

If we also enable capture checking at the call site:

```scala
import language.experimental.captureChecking

val resource: Resource[Response] = Client.get("google.com")

// ... exactly the same code as before
```

then the incorrect code no longer compiles. We get a beautifully cryptic error
message:

```
[error] -- [E007] Type Mismatch Error: /Users/chris.birchall/code/capisce/src/main/scala/Main.scala:17:9 
[error] 17 |    .use(IO.pure(_)) // whoops! the response has escaped the Resource#use block
[error]    |         ^^^^^^^^^^
[error]    |Found:    (_$1: http.Response^'s1) ->'s2 katz.effect.IO[http.Response^'s3]^'s4
[error]    |Required: (http.Response^{})^ => katz.effect.IO[http.Response^'s5]
[error]    |
[error]    |Note that capability cap cannot be included in outer capture set 's5.
[error]    |
[error]    |where:    =>  refers to a fresh root capability created in value badEffect when checking argument to parameter f of method use
[error]    |          ^   refers to the universal root capability
[error]    |          cap is a root capability associated with the result type of (_$1: (http.Response^{})^): katz.effect.IO[http.Response^'s5]
[error]    |
[error]    | longer explanation available when compiling with `-explain`
```

If you want any chance of deciphering this error, I recommend reading [Nicolas
Rinaudo's excellent 
post](https://nrinaudo.github.io/articles/capture_checking.html).
