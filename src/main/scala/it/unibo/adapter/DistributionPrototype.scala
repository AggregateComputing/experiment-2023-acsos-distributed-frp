package it.unibo.adapter

import it.unibo.alchemist.model.implementations.timedistributions.{ExponentialTime, DiracComb as AlchemistDiracComb}
import it.unibo.alchemist.model.interfaces.{Time, TimeDistribution}
import it.unibo.alchemist.model.implementations.times.DoubleTime
import org.apache.commons.math3.random.RandomGenerator

trait DistributionPrototype:
  def adapt[T](initialTime: Time, random: RandomGenerator): TimeDistribution[T]

  def randomInit(randomGenerator: RandomGenerator): Time

class DiracComb(val rate: Double, sameTime: Boolean) extends DistributionPrototype:
  def this(rate: Double) = this(rate, false)
  override def adapt[T](initialTime: Time, random: RandomGenerator): TimeDistribution[T] =
    AlchemistDiracComb[T](initialTime, rate)

  override def randomInit(randomGenerator: RandomGenerator): Time = if (sameTime) {
    Time.ZERO
  } else {
    DoubleTime(
      randomGenerator.nextDouble() * (1 / rate)
    )
  }
class Exponential(val rate: Double) extends DistributionPrototype:
  override def adapt[T](initialTime: Time, random: RandomGenerator): TimeDistribution[T] =
    ExponentialTime[T](rate, initialTime, random)

  override def randomInit(randomGenerator: RandomGenerator): Time = DoubleTime(
    randomGenerator.nextDouble() * (1 / rate)
  )
