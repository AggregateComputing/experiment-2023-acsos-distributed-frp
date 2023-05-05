package it.unibo

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.*
import it.unibo.distributed.frp.Molecules
abstract class BaseScafiProgram extends AggregateProgram with StandardSensors with ScafiAlchemistSupport:
  final override def main(): Any = {
    node.put(Molecules.Rounds.getName, node.get[Double](Molecules.Rounds.getName) + 1)
    node.put(Molecules.MessagesSent.getName, node.get[Double](Molecules.MessagesSent.getName) + foldhood(0)(_ + _)(1))

    val result = computation()
    node.put("root", result)
  }

  def computation(): Any
