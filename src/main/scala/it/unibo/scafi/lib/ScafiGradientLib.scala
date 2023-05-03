package it.unibo.scafi.lib

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.*
trait ScafiGradientLib extends ScafiFieldUtils:
  self: AggregateProgram with StandardSensors =>

  def G[V](source: Boolean, field: V, acc: V => V, metric: () => Double): V =
    rep((Double.MaxValue, field)) { case (dist, value) =>
      mux(source) {
        (0.0, field)
      } {
        excludingSelf
          .minHoodSelector(nbr {
            dist
          } + metric())(
            (
              nbr {
                dist
              } + metric(),
              acc(nbr {
                value
              })
            )
          )
          .getOrElse((Double.PositiveInfinity, field))
      }
    }._2

  def broadcast[V](source: Boolean, field: V, metric: Metric = nbrRange): V =
    G(source, field, v => v, metric)

  def distanceBetween(source: Boolean, target: Boolean, metric: Metric = nbrRange): Double =
    broadcast(source, classicGradient(target, metric), metric)

  def channel(source: Boolean, target: Boolean, width: Double): Boolean = {
    val ds = classicGradient(source)
    val dt = classicGradient(target)
    val db = distanceBetween(source, target)
    !((ds + dt).isInfinite && db.isInfinite) && ds + dt <= db + width
  }
  def classicGradient(source: Boolean, metric: Metric = nbrRange): Double =
    rep(Double.PositiveInfinity) { d =>
      mux(source)(0.0)(minHoodPlus(nbr(d) + metric()))
    }
