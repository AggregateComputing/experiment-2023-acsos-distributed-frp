package it.unibo

import it.unibo.alchemist.model.implementations.actions.DistributedFrpIncarnation
import it.unibo.alchemist.model.interfaces.Position


trait ProgramFactory:
  def create[P <: Position[P]](incarnation: DistributedFrpIncarnation[P]): incarnation.Flow[?]
