package it.unibo.scafi.lib
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.*
trait ScafiLeaderElectionLib:
  self: AggregateProgram with StandardSensors with ScafiGradientLib =>
  def S(grain: Double, metric: Metric = nbrRange): Boolean =
    breakUsingUids(randomUid, grain, metric)

  def randomUid: (Double, ID) = rep((nextRandom(), mid())) { v =>
    (v._1, mid())
  }

  def breakUsingUids(uid: (Double, ID), grain: Double, metric: Metric): Boolean =
    uid == rep(uid) { lead =>
      // Distance from current device (uid) to the current leader (lead).
      val dist = classicGradient(uid == lead, metric)
      distanceCompetition(dist, lead, uid, grain, metric)
    }
  def distanceCompetition(d: Double, lead: (Double, ID), uid: (Double, ID), grain: Double, metric: Metric): (
      Double,
      ID
  ) = {
    val inf: (Double, ID) = (Double.PositiveInfinity, uid._2)
    mux(d > grain) {
      uid
    } {
      mux(d >= (0.5 * grain))(inf) {
        minHood {
          mux(nbr(d) + metric() >= 0.5 * grain) {
            nbr(inf)
          } {
            nbr(lead)
          }
        }(using Builtins.Bounded.tupleBounded)
      }
    }
  }
