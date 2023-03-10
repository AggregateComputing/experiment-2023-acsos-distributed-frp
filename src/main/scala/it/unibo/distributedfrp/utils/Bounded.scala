package it.unibo.distributedfrp.utils

trait LowerBounded[T] extends Ordering[T]:
  def lowerBound: T

trait UpperBounded[T] extends Ordering[T]:
  def upperBound: T

trait Bounded[T] extends LowerBounded[T], UpperBounded[T]
