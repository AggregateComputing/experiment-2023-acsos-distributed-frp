package it.unibo

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.*
import it.unibo.distributedfrp.utils.Bounded
import it.unibo.scafi.lib.*

class ScafiCollect extends BaseScafiProgram with ScafiGradientLib with ScafiCollectLib:
  override def computation(): Any =
    val g = classicGradient(source = mid() == 0)
    C[Double](g, _ + _, 1.0, 0.0)
