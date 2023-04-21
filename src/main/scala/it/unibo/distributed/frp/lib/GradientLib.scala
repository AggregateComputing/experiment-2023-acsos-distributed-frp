package it.unibo.distributed.frp.lib

import it.unibo.alchemist.model.implementations.actions.DistributedFrpIncarnation
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.distributed.frp.lang
import it.unibo.distributed.frp.lib.IncarnationProvider
import it.unibo.distributedfrp.core.Incarnation
import it.unibo.distributedfrp.simulation.TestNeighborSensors
import it.unibo.distributedfrp.utils.Liftable.liftTwice
import it.unibo.distributedfrp.utils.Liftable.*

trait GradientLib:
  self: IncarnationProvider[_ <: Incarnation with TestNeighborSensors] =>
  import I.{*, given}

  def gradient(source: Flow[Boolean]): Flow[Double] =
    loop(Double.PositiveInfinity) { distance =>
      mux(source) {
        constant(0.0)
      } {
        liftTwice(nbrRange, nbr(distance))(_ + _).withoutSelf.min
      }
    }

  def broadcast[T](source: Flow[Boolean], value: Flow[T]): Flow[T] =
    val broadcastResult = loop[(Double, Option[T])]((Double.PositiveInfinity, None)) { d =>
      mux(source) {
        value.map(0.0 -> Some(_))
      } {
        val n = nbr(d)
        val distances = n.mapTwice(_._1)
        val values = n.mapTwice(_._2)
        val field = liftTwice(distances, nbrRange, values)((ds, ra, va) => (ds + ra) -> va)
        field.withoutSelf.map(_.values.minByOption(_._1).getOrElse((Double.PositiveInfinity, None)))
      }
    }
    lift(broadcastResult, value)(_._2.getOrElse(_))

  def dilate(region: Flow[Boolean], width: Double): Flow[Boolean] =
    gradient(region).map(_ <= width)

  def distanceBetween(source: Flow[Boolean], destination: Flow[Boolean]): Flow[Double] =
    broadcast(destination, gradient(source))

