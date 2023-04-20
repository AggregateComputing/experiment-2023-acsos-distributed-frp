package it.unibo.alchemist.model.implementations.reactions

import it.unibo.ProgramFactory
import it.unibo.alchemist.model.implementations.actions.{DistributedFrpIncarnation, SendToNeighborhood}
import it.unibo.alchemist.model.implementations.timedistributions.{DiracComb, Trigger}
import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.model.interfaces.{Environment, Position, Time, TimeDistribution}

import _root_.scala.jdk.CollectionConverters.IterableHasAsScala
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.distributed.frp.Molecules

import java.util.List as JList
class InitFrpGlobalReaction[P <: Position[P]](val environment: Environment[Any, P], val programFactory: String)
    extends AbstractGlobalReaction[P]:
  private val sendingTime: Time = DoubleTime(1)
  private val factory =
    Class.forName(programFactory).getDeclaredConstructor().newInstance().asInstanceOf[ProgramFactory]
  lazy val globalIncarnation = new DistributedFrpIncarnation[P](environment)
  override val distribution: TimeDistribution[Any] = DiracComb(Time.ZERO, 1.0)

  override def execute(): Unit = {
    val program = factory.create(globalIncarnation)
    val contexts = environment.getNodes.asScala.toList.map(node => globalIncarnation.context(node.getId))
    for context <- contexts do
      program
        .run(Seq.empty)(using context)
        .listen { v =>
          context.node.setConcentration(Molecules.LastComputationTime, environment.getSimulation.getTime.toDouble)
          environment.getSimulation.schedule(() => context.node.setConcentration(Molecules.Root, v.root))
          // TODO: perhaps we should add more "randomness" to the sending time
          val copied =
            getTimeDistribution.cloneOnNewNode(context.node, environment.getSimulation.getTime.plus(sendingTime))
          val event = new Event[Any](context.node, copied)
          context.node.getReactions.asScala.toList.foreach { reaction =>
            context.node.removeReaction(reaction)
            environment.getSimulation.reactionRemoved(reaction)
          }
          event.setActions(JList.of(SendToNeighborhood[P](context.node, environment, v)))
          context.node.addReaction(event)
          environment.getSimulation.reactionAdded(event)
        }
    distribution.update(Time.INFINITY, true, getRate, environment) // as it removes the current reaction
  }
