package it.unibo.alchemist.model.implementations.reactions

import it.unibo.ProgramFactory
import it.unibo.alchemist.model.implementations.actions.{DistributedFrpIncarnation, SendToNeighborhood}
import it.unibo.alchemist.model.implementations.timedistributions.{DiracComb, Trigger}
import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.model.interfaces.{Environment, Position, Time, TimeDistribution}

import _root_.scala.jdk.CollectionConverters.IterableHasAsScala
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.distributed.frp.Molecules
import org.apache.commons.math3.random.RandomGenerator

import java.util.List as JList
class InitFrpGlobalReaction[P <: Position[P]](
    val environment: Environment[Any, P],
    val randomGenerator: RandomGenerator,
    val distribution: TimeDistribution[Any],
    programFactory: String
) extends AbstractGlobalReaction[P]:
  private val factory =
    Class.forName(programFactory).getDeclaredConstructor().newInstance().asInstanceOf[ProgramFactory]
  private lazy val globalIncarnation = new DistributedFrpIncarnation[P](environment, randomGenerator)

  override def execute(): Unit =
    val program = factory.create(globalIncarnation)
    val contexts = environment.getNodes.asScala.toList.map(node => globalIncarnation.context(node.getId))
    for context <- contexts do
      program
        .run(Seq.empty)(using context)
        .listen { v =>
          context.node.setConcentration(Molecules.LastComputationTime, environment.getSimulation.getTime.toDouble)
          context.node.setConcentration(Molecules.Root, v.root)
          context.node.setConcentration(Molecules.Export, v)
          // TODO: perhaps we should add more "randomness" to the sending time
          val nextTime =
            if (environment.getSimulation.getTime.toDouble == 0.0)
              DoubleTime(randomGenerator.nextDouble() * 1 / getTimeDistribution.getRate)
            else
              environment.getSimulation.getTime
          val copied =
            getTimeDistribution.cloneOnNewNode(context.node, nextTime)
          val event = new Event(context.node, copied)
          context.node.getReactions.asScala.toList.foreach { reaction =>
            context.node.removeReaction(reaction)
            environment.getSimulation.reactionRemoved(reaction)
          }
          context.storeTicks()
          event.setActions(JList.of(SendToNeighborhood(context.node, environment, v)))
          context.node.addReaction(event)
          environment.getSimulation.reactionAdded(event)
        }
    distribution.update(Time.INFINITY, true, getRate, environment) // as it removes the current reaction
