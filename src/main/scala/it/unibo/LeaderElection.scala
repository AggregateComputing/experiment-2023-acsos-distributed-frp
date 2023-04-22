package it.unibo

import it.unibo.alchemist.model.implementations.actions.DistributedFrpIncarnation
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.distributed.frp.lib.{GradientLib, IncarnationProvider, CollectLib, LeaderElectionLib}
import it.unibo.distributedfrp.utils.Liftable.*

class LeaderElection extends ProgramFactory:
  def create[P <: Position[P]](incarnation: DistributedFrpIncarnation[P]): incarnation.Flow[?] =
    val libs = new IncarnationProvider(incarnation) with LeaderElectionLib[P] with CollectLib with GradientLib
    import libs.*
    import libs.I.{given, *}

    leaderElection(constant(3))
      .map(data =>
        if (data) { 10 }
        else { 0 }
      )
      .adapt(incarnation)
