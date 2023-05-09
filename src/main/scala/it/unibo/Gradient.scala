package it.unibo

import it.unibo.alchemist.model.implementations.actions.DistributedFrpIncarnation
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.distributedfrp.utils.Liftable.liftTwice
import it.unibo.distributed.frp.lang.*
import it.unibo.distributedfrp.core.Incarnation
import it.unibo.distributedfrp.utils.Liftable.*
import it.unibo.distributed.frp.lib.{GradientLib, IncarnationProvider}
class Gradient extends ProgramFactory:
  def create[P <: Position[P]](incarnation: DistributedFrpIncarnation[P]): incarnation.Flow[?] =
    val libs = new IncarnationProvider(incarnation) with GradientLib
    import libs.*
    import libs.I.{given, *}
    branch(sensor[Boolean]("obstacle")) {
      constant(-1.0)
    } {
      gradient(sensor[Boolean]("source"))
    }.adapt(incarnation)
