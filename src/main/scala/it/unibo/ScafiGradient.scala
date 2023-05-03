package it.unibo

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.*
import it.unibo.scafi.lib.ScafiGradientLib

class ScafiGradient extends BaseScafiProgram with ScafiGradientLib:
  override def computation(): Any =
    branch(sense("obstacle"))(-1)(classicGradient(sense("source")))
