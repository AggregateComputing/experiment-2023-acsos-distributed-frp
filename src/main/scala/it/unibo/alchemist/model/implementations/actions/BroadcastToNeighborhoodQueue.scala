package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.actions.DistributedFrpIncarnation.*
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.interfaces.*
import it.unibo.distributed.frp.Molecules
import it.unibo.alchemist.model.implementations.PimpAlchemist.*
import _root_.scala.jdk.CollectionConverters.IteratorHasAsScala

case class BroadcastToNeighborhoodQueue[P <: Position[P]](
    from: Node[Any],
    environment: Environment[Any, P],
    data: Export[Any]
) extends AbstractAction[Any](from):
  override def cloneAction(node: Node[Any], reaction: Reaction[Any]): Action[Any] = ???

  override def execute(): Unit =
    val neighborhood = environment.getNeighborhood(from).getNeighbors.iterator().asScala.toList
    (from :: neighborhood).foreach { to =>
      to.updateConcentration[Map[Int, Export[Any]]](Molecules.ExportQueue, _ + (from.getId -> data))
    }
    from.updateConcentration[Double](Molecules.MessagesSent, _ + neighborhood.size)
    // Todo refactor
    val reactions = from.getReactions.iterator().asScala
    val findReaction = reactions.find(_.getActions.iterator().asScala.exists(_ == this))
    environment.getSimulation.reactionRemoved(findReaction.get)
    from.removeReaction(findReaction.get)

  override def getContext: Context = Context.LOCAL
