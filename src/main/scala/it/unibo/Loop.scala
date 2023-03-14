package it.unibo

import it.unibo.alchemist.model.implementations.actions.DistributedFrpIncarnation
import it.unibo.alchemist.model.interfaces.Position

class Loop extends ProgramFactory:
  def create[P <: Position[P]](incarnation: DistributedFrpIncarnation[P]): incarnation.Flow[?] =
    import incarnation.{*, given}
    import it.unibo.distributedfrp.utils.Liftable.*
    loop(0)(_.map(_ + 1))