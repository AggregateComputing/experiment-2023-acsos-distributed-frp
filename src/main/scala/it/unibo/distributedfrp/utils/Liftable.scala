package it.unibo.distributedfrp.utils

trait Liftable[F[_]]:
  extension[A] (a: F[A])
    def map[B](f: A => B): F[B]
    
  def lift[A, B, C](a: F[A], b: F[B])(f: (A, B) => C): F[C]

  def lift[A, B, C, D](a: F[A], b: F[B], c: F[C])(f: (A, B, C) => D): F[D]

object Liftable:
  def lift[A, B, C, F[_] : Liftable](a: F[A], b: F[B])(f: (A, B) => C): F[C] =
    summon[Liftable[F]].lift(a, b)(f)

  def liftTwice[A, B, C, F1[_] : Liftable, F2[_] : Liftable](a: F1[F2[A]], b: F1[F2[B]])(f: (A, B) => C): F1[F2[C]] =
    lift(a, b)((aa, bb) => lift(aa, bb)(f))

  def lift[A, B, C, D, F[_] : Liftable](a: F[A], b: F[B], c: F[C])(f: (A, B, C) => D): F[D] =
    summon[Liftable[F]].lift(a, b, c)(f)

  def liftTwice[A, B, C, D, F1[_] : Liftable, F2[_] : Liftable](a: F1[F2[A]], b: F1[F2[B]], c: F1[F2[C]])(f: (A, B, C) => D): F1[F2[D]] =
    lift(a, b, c)((aa, bb, cc) => lift(aa, bb, cc)(f))