package it.unibo

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.*
import it.unibo.scafi.lib.ScafiGradientLib

class ScafiGradient extends AggregateProgram with StandardSensors with ScafiAlchemistSupport with ScafiGradientLib:
  override def main(): Any =
    val g = branch(sense("obstacle"))(-1)(classicGradient(sense("source")))
    node.put("root", g)
