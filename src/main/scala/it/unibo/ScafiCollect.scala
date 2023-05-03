package it.unibo

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.*
import it.unibo.distributedfrp.utils.Bounded
import it.unibo.scafi.lib.*

class ScafiCollect
    extends AggregateProgram
    with StandardSensors
    with ScafiAlchemistSupport
    with ScafiGradientLib
    with ScafiCollectLib:
  override def main(): Any =
    val g = classicGradient(source = mid() == 0)
    val count = C[Double](g, _ + _, 1.0, 0.0)
    node.put("root", count)
