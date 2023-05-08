package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.interfaces.{Action, Context, Environment, Node, Position, Reaction}
import DistributedFrpIncarnation.*
import it.unibo.distributed.frp.Molecules
import it.unibo.alchemist.model.implementations.PimpAlchemist.*
import _root_.scala.jdk.CollectionConverters.IteratorHasAsScala
/** Unstable, to many event created at runtime */
class SendToNeighbor[P <: Position[P]](
    to: Node[Any],
    environment: Environment[Any, P],
    data: (Node[Any], Export[Any])
) extends AbstractAction[Any](to):
  override def cloneAction(node: Node[Any], reaction: Reaction[Any]): Action[Any] = this

  override def execute(): Unit =
    to.updateConcentration[Map[Int, Export[Any]]](Molecules.ExportQueue, _ + (data._1.getId -> data._2))
    //to.getConcentration(Molecules.Context).asInstanceOf[FrpContext].receiveExport(data._2, data._1)
    data._1.updateConcentration[Double](Molecules.MessagesSent, _ + 1)
    val reactions = to.getReactions.iterator().asScala
    val findReaction = reactions.find(_.getActions.iterator().asScala.exists(_ == this))
    environment.getSimulation.reactionRemoved(findReaction.get)
    to.removeReaction(findReaction.get)

  override def getContext: Context = Context.NEIGHBORHOOD

  override def toString: String = s"SendToNeighbor(${to.getId}, ${data._1.getId})"
