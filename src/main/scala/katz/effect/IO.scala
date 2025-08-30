package katz.effect

case class IO[A](thunk: () => A):
  def map[B](f: A => B): IO[B] =
    IO(() => {
      val a = thunk()
      f(a)
    })
  def flatMap[B](f: A => IO[B]): IO[B] =
    IO(() => {
      val a = thunk()
      val iob = f(a)
      iob.thunk()
    })

  def unsafeRunSync(): A = thunk()

object IO:
  def pure[A](value: A): IO[A] = IO(() => value)
  def delay[A](thunk: => A): IO[A] = IO(() => thunk)
  def raiseError[A](e: Throwable): IO[A] = IO(() => throw e)
