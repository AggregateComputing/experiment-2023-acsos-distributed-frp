package it.unibo.alchemist.model.implementations.actions
import it.unibo.alchemist.model.interfaces.{Action, Environment, Node, Position, Reaction}
class MoveFor[T, P <: Position[P]](node: Node[T], environment: Environment[T, P], deltaX: Double, distance: Double)
    extends AbstractMoveNode[T, P](environment, node):
  private var totalDistance = 0.0
  override def cloneAction(node: Node[T], reaction: Reaction[T]): Action[T] = ???
  override def getNextPosition: P =
    if (totalDistance > distance) environment.makePosition(0, 0)
    else
      totalDistance += deltaX
      environment.makePosition(deltaX, 0)
