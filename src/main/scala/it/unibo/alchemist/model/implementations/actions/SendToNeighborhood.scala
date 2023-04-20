package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.actions.SendToNeighborhood.ContextMolecule
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.interfaces.{Action, Context, Environment, Node, Position, Reaction}
import DistributedFrpIncarnation.*

import _root_.scala.jdk.CollectionConverters.IteratorHasAsScala

class SendToNeighborhood[P <: Position[P]](
    node: Node[Any],
    environment: Environment[Any, P],
    data: Export[Any]
) extends AbstractAction[Any](node):
  override def cloneAction(node: Node[Any], reaction: Reaction[Any]): Action[Any] = this

  override def execute(): Unit = {
    val neighborhood = environment.getNeighborhood(node).getNeighbors.iterator().asScala.toList

    (node :: neighborhood).foreach { n =>
      n.getConcentration(ContextMolecule)
        .asInstanceOf[FrpContext]
        .receiveExport(data, node)
    }
  }

  override def getContext: Context = Context.NEIGHBORHOOD

object SendToNeighborhood:
  val ContextMolecule: SimpleMolecule = SimpleMolecule("context")
