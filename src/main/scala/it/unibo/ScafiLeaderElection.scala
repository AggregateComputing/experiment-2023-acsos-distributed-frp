package it.unibo
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.*
import it.unibo.scafi.lib.*
class ScafiLeaderElection extends BaseScafiProgram with ScafiGradientLib with ScafiLeaderElectionLib:
  override def computation(): Any =
    val leader = S(3)
    if (leader) { 1 }
    else { 0 }
