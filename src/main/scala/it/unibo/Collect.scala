package it.unibo

import it.unibo.alchemist.model.implementations.actions.DistributedFrpIncarnation
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.distributed.frp.lib.{GradientLib, IncarnationProvider, CollectLib}
import it.unibo.distributedfrp.utils.Liftable.*

class Collect extends ProgramFactory:
  def create[P <: Position[P]](incarnation: DistributedFrpIncarnation[P]): incarnation.Flow[?] =
    val libs = new IncarnationProvider(incarnation) with CollectLib with GradientLib
    import libs.*
    import libs.I.{given, *}

    val potential = gradient(mid.map(_ == 1))
    collect[Double](potential, _ + _, 1, constant(0)).adapt(incarnation)

//.map(x => (x * 100).round / 100.0)
