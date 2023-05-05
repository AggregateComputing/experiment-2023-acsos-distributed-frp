package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.interfaces.{Action, Context, Environment, Node, Position, Reaction}
import DistributedFrpIncarnation.*
import it.unibo.distributed.frp.Molecules

import _root_.scala.jdk.CollectionConverters.IteratorHasAsScala

class ThrottleNeighborhoodMessages[P <: Position[P]](
    node: Node[Any],
    environment: Environment[Any, P]
) extends AbstractAction[Any](node):
  override def cloneAction(node: Node[Any], reaction: Reaction[Any]): Action[Any] = this
  override def execute(): Unit =
    val neighborhood = environment.getNeighborhood(node).getNeighbors.iterator().asScala.toList
    val received = (node :: neighborhood).map { node =>
      node.getId -> node.getConcentration(Molecules.Export).asInstanceOf[Export[Any]]
    }.toMap
    node.getConcentration(Molecules.Context).asInstanceOf[FrpContext].receiveWholeExports(received)

  override def getContext: Context = Context.NEIGHBORHOOD
