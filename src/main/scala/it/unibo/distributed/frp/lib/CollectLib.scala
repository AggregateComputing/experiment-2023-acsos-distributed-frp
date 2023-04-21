package it.unibo.distributed.frp.lib

import it.unibo.distributedfrp.core.Incarnation
import it.unibo.distributedfrp.simulation.TestNeighborSensors
import it.unibo.distributedfrp.utils.Liftable.*

trait CollectLib:
  self: IncarnationProvider[_ <: Incarnation { type DeviceId = Int }] =>
  import I.{*, given}
  def collect[V](
      potential: Flow[Double],
      accumulator: (V, V) => V,
      local: V,
      Null: Flow[V]
  ): Flow[V] =
    loop(local) { value =>
      val neighborhoodId: Flow[NeighborField[DeviceId]] = nbr(findParent(potential))
      val valueField = nbr(value)
      val realRightPath =
        lift(neighborhoodId, mid)((mids, parent) => mids.map { case (id, data) => (id, (parent == data)) })
      liftTwice(realRightPath, valueField, nbr(Null))((cond, V, Null) => if cond then V else Null)
        .map(field => field.values.foldLeft(local)(accumulator))
    }

  def findParent(potential: Flow[Double]): Flow[DeviceId] = {
    val potentialId = liftTwice(nbr(potential), nbr(mid))((p, id) => (p, id)).map(field => field.minBy(_._2._1))
    val potentialMin = potentialId.map(_._2._1)
    potentialId.map(_._2._1)
    mux(lift(potentialMin, potential)(_ < _)) {
      potentialId.map(_._2._2)
    } {
      constant(Int.MaxValue)
    }
  }
