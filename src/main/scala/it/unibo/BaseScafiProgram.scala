package it.unibo

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.*
abstract class BaseScafiProgram extends AggregateProgram with StandardSensors with ScafiAlchemistSupport:
  override final def main(): Any = {
    node.put("ticks", node.get[Double]("ticks") + 1)
    val result = computation()
    node.put("root", result)
  }

  def computation(): Any
