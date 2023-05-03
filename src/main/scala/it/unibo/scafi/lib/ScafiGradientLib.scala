package it.unibo.scafi.lib

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.*
trait ScafiGradientLib:
  self: AggregateProgram with StandardSensors =>

  def classicGradient(source: Boolean, metric: Metric = nbrRange): Double =
    rep(Double.PositiveInfinity) { d =>
      mux(source)(0.0)(minHoodPlus(nbr(d) + metric()))
    }
