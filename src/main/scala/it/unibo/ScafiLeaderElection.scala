package it.unibo
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.*
import it.unibo.scafi.lib.*
class ScafiLeaderElection
    extends AggregateProgram
    with StandardSensors
    with ScafiGradientLib
    with ScafiLeaderElectionLib
    with ScafiAlchemistSupport:
  override def main(): Any =
    val leader = S(3)
    node.put(
      "root",
      if (leader) { 10 }
      else { 0 }
    )
