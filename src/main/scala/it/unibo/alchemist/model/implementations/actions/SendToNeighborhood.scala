package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.interfaces.{Action, Context, Environment, Node, Position, Reaction}

import _root_.scala.jdk.CollectionConverters.IteratorHasAsScala
class SendToNeighborhood[P <: Position[P]](node: Node[Any], environment: Environment[Any, P], data: DistributedFrpIncarnation[P]#Export[Any]) extends AbstractAction[Any](node):
  override def cloneAction(node: Node[Any], reaction: Reaction[Any]): Action[Any] = this

  override def execute(): Unit = {
    val neighborhood = environment.getNeighborhood(node)
    neighborhood.getNeighbors.iterator().asScala.foreach {
      n =>
       n.getConcentration(new SimpleMolecule("context"))
          .asInstanceOf[DistributedFrpIncarnation[P]#Context].receiveExport(data, node)
    }
    node.getConcentration(new SimpleMolecule("context"))
      .asInstanceOf[DistributedFrpIncarnation[P]#Context].receiveExport(data, node)
  }

  override def getContext: Context = Context.NEIGHBORHOOD