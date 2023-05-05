package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.actions.DistributedFrpIncarnation.*
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.interfaces.*
import it.unibo.distributed.frp.Molecules

import _root_.scala.jdk.CollectionConverters.IteratorHasAsScala

case class BroadcastToNeighborhood[P <: Position[P]](
    from: Node[Any],
    environment: Environment[Any, P],
    data: Export[Any]
) extends AbstractAction[Any](from):
  override def cloneAction(node: Node[Any], reaction: Reaction[Any]): Action[Any] = ???

  override def execute(): Unit =
    val neighborhood = environment.getNeighborhood(from).getNeighbors.iterator().asScala.toList
    (from :: neighborhood).foreach { to =>
      to.getConcentration(Molecules.Context)
        .asInstanceOf[FrpContext]
        .receiveExport(data, from)
    }

  override def getContext: Context = Context.NEIGHBORHOOD
