package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.interfaces.{Action, Context, Environment, Node, Position, Reaction}
import DistributedFrpIncarnation.*
import it.unibo.distributed.frp.Molecules

import _root_.scala.jdk.CollectionConverters.IteratorHasAsScala
import it.unibo.alchemist.model.implementations.PimpAlchemist.updateConcentration
class ThrottleNeighborhoodMessages[P <: Position[P]](
    node: Node[Any],
    environment: Environment[Any, P]
) extends AbstractAction[Any](node):
  override def cloneAction(node: Node[Any], reaction: Reaction[Any]): Action[Any] = this
  override def execute(): Unit =
    val received = node.getConcentration(Molecules.ExportQueue).asInstanceOf[Map[Int, Export[Any]]]
    val neighborhood =
      node.getId :: environment.getNeighborhood(node).getNeighbors.iterator().asScala.toList.map(_.getId)
    val newExported = received.filter { case (id, _) => neighborhood.contains(id) }
    node.updateConcentration[Map[Int, Export[Any]]](Molecules.ExportQueue, _ => newExported)
    node.getConcentration(Molecules.Context).asInstanceOf[FrpContext].receiveWholeExports(received)

  override def getContext: Context = Context.NEIGHBORHOOD
