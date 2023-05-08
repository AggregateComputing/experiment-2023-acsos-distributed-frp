package it.unibo.distributed.frp.lib

import it.unibo.alchemist.model.implementations.actions.DistributedFrpIncarnation
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.distributedfrp.core.Incarnation
import it.unibo.distributedfrp.simulation.TestNeighborSensors
import it.unibo.distributedfrp.utils.Liftable.*

trait LeaderElectionLib[P <: Position[P]]:
  self: IncarnationProvider[DistributedFrpIncarnation[P]] with GradientLib with CollectLib =>

  import I.{*, given}
  def leaderElection(grain: Flow[Double]): Flow[Boolean] = breakingUsingUids(randomUid, grain)

  def randomUid: Flow[(Double, DeviceId)] = lift(
    loop(random.nextDouble()) { v =>
      v
    },
    mid
  )((a, b) => (a, b))

  def breakingUsingUids(uid: Flow[(Double, DeviceId)], grain: Flow[Double]): Flow[Boolean] =
    val currentLead = loop((Double.PositiveInfinity, -1)) { lead =>
      val realLead = lift(uid, lead) { case (me, other) => if (other._2 == -1) me else other }
      val distance = gradient(lift(uid, realLead)(_ == _))
      distanceCompetition(distance, realLead, uid, grain)
    }
    lift(uid, currentLead)(_ == _)

  def distanceCompetition(
      distance: Flow[Double],
      lead: Flow[(Double, DeviceId)],
      uid: Flow[(Double, DeviceId)],
      grain: Flow[Double]
  ): Flow[(Double, DeviceId)] =
    val inf = uid.map(data => (Double.PositiveInfinity, data._2))
    mux(lift(distance, grain)(_ > _)) {
      uid
    } {
      mux(lift(distance, grain.map(_ * 0.5))(_ >= _)) {
        inf
      } {
        val otherDistances = liftTwice(nbr(distance), nbrRange)(_ + _)
        val selection = lift(otherDistances, grain.map(_ * 0.5))((data, thr) =>
          data.map { case (id, distance) => (id, distance >= thr) }
        )
        liftTwice(selection, nbr(inf), nbr(lead)) { (selection, inf, lead) =>
          if (selection) inf else lead
        }.map(data => data.values.minOption).map(_.getOrElse((Double.PositiveInfinity, -1)))
      }
    }
