package it.unibo

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.*
import it.unibo.scafi.lib.*

class ScafiChannel extends BaseScafiProgram with ScafiGradientLib with ScafiLeaderElectionLib:
  override def computation(): Any =
    branch(sense("obstacle"))(false)(channel(sense("source"), sense("destination"), 0.1))
