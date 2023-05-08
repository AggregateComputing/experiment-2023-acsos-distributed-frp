package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.{Action, Context, Molecule, Node, Reaction}
import it.unibo.alchemist.model.implementations.PimpAlchemist.*
class UpdateCellSink(node: Node[Any], molecule: Molecule, value: Any) extends AbstractAction[Any](node) {
  override def cloneAction(node: Node[Any], reaction: Reaction[Any]): Action[Any] = ???
  override def execute(): Unit = node.getSinkFromMolecule(molecule).update(_ => value)
  override def getContext: Context = Context.LOCAL
}
