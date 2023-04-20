package it.unibo.alchemist.model.implementations.reactions

import it.unibo.ProgramFactory
import it.unibo.alchemist.model.implementations.actions.DistributedFrpIncarnation.FrpContext
import it.unibo.alchemist.model.implementations.actions.{DistributedFrpIncarnation, SendToNeighborhood}
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.implementations.timedistributions.{DiracComb, Trigger}
import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.model.interfaces.{Environment, Position, Time, TimeDistribution}
import it.unibo.distributed.frp.Molecules

import java.util.List as JList
import _root_.scala.jdk.CollectionConverters.IterableHasAsScala

class HeartBeat[P <: Position[P]](
    val environment: Environment[Any, P],
    val distribution: TimeDistribution[Any]
) extends AbstractGlobalReaction[P]:
  override def execute(): Unit =
    environment.getNodes.forEach { node =>
      val diff = environment.getSimulation.getTime.toDouble - node
        .getConcentration(Molecules.LastComputationTime)
        .asInstanceOf[Double]
      node.setConcentration(Molecules.TimeDifference, math.exp(diff / 2))
      val neighborhood = environment.getNeighborhood(node).asScala.toList
      node
        .getConcentration(Molecules.Context)
        .asInstanceOf[FrpContext]
        .heartbeat(neighborhood)
    }
    distribution.update(distribution.getNextOccurence, true, distribution.getRate, environment)
