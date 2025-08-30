package katz.effect

import language.experimental.captureChecking

trait Resource[A]:
  def use[B](f: A^ => IO[B]): IO[B]

object Resource:
  def make[A](acquire: IO[A])(release: A^ => IO[Unit]): Resource[A]^{release} =
    new Resource[A]:
      def use[B](f: A^ => IO[B]): IO[B] =
        for
          resource <- acquire
          result   <- f(resource)
          _        <- release(resource)
        yield result
