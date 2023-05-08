package it.unibo

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.*
import it.unibo.scafi.lib.*

class ScafiChannel extends BaseScafiProgram with ScafiGradientLib with ScafiLeaderElectionLib:
  override def computation(): Any =
    val result = branch(sense("obstacle"))(false)(channel(sense("source"), sense("destination"), 0.1))
    if result then 10 else 0.0
