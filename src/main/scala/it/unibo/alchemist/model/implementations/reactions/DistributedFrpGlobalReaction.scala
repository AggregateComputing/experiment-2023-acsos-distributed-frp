package it.unibo.alchemist.model.implementations.reactions

import it.unibo.{ProgramFactory, Loop}
import it.unibo.alchemist.model.implementations.actions.{AbstractAction, DistributedFrpIncarnation, SendToNeighborhood}
import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.model.interfaces.*
import org.danilopianini.util.{ListSet, ListSets}

import _root_.scala.jdk.CollectionConverters.IterableHasAsScala
import java.util

class DistributedFrpGlobalReaction[P <: Position[P]](
    val environment: Environment[Any, P],
    distribution: TimeDistribution[Any],
    programFactory: String
) extends GlobalReaction[Any]:

  private var executed = false
  private val factory =
    Class.forName(programFactory).getDeclaredConstructor().newInstance().asInstanceOf[ProgramFactory]
  lazy val globalIncarnation = new DistributedFrpIncarnation[P](environment)
  private val actions: util.List[Action[Any]] = util.List.of()
  private val conditions: util.List[Condition[Any]] = util.List.of()
  override def getActions: util.List[Action[Any]] = actions
  override def setActions(list: util.List[_ <: Action[Any]]): Unit = {
    actions.clear()
    actions.addAll(list)
  }

  override def setConditions(list: util.List[_ <: Condition[Any]]): Unit = {
    conditions.clear()
    conditions.addAll(list)
  }

  override def execute(): Unit = {
    // todo pass the program as argument
    if !executed then
      val program = factory.create(globalIncarnation)
      val contexts = environment.getNodes.asScala.toList.map(node => globalIncarnation.context(node.getId))
      for context <- contexts do
        program
          .run(Seq.empty)(using context)
          .listen { v =>
            context.node.setConcentration(SimpleMolecule("lastTime"), environment.getSimulation.getTime.toDouble)
            environment.getSimulation.schedule(() => context.node.setConcentration(SimpleMolecule("root"), v.root))
            val copied = getTimeDistribution.cloneOnNewNode(
              context.node,
              environment.getSimulation.getTime.plus(new DoubleTime(1))
            )
            val event = new Event[Any](context.node, copied)
            context.node.getReactions.asScala.toList.foreach { reaction =>
              context.node.removeReaction(reaction)
              environment.getSimulation.reactionRemoved(reaction)
            }
            event.setActions(util.List.of(SendToNeighborhood[P](context.node, environment, v)))
            context.node.addReaction(event)
            environment.getSimulation.reactionAdded(event)
          }
    else
      environment.getNodes.forEach { node =>
        val diff = environment.getSimulation.getTime.toDouble - node
          .getConcentration(SimpleMolecule("lastTime"))
          .asInstanceOf[Double]
        node.setConcentration(SimpleMolecule("timeDiff"), math.exp(diff / 2))
        val neighborhood = environment.getNeighborhood(node).asScala.toList
        node
          .getConcentration(SimpleMolecule("context"))
          .asInstanceOf[DistributedFrpIncarnation[P]#Context]
          .heartbeat(neighborhood)
      }
    executed = true
    distribution.update(getTimeDistribution.getNextOccurence, true, getRate, environment)
  }

  protected def executeBeforeUpdateDistribution(): Unit = {}

  override def getConditions: util.List[Condition[Any]] = conditions

  override def getInboundDependencies: ListSet[_ <: Dependency] = ListSets.emptyListSet()

  override def getOutboundDependencies: ListSet[_ <: Dependency] = ListSets.emptyListSet()

  override def getTimeDistribution: TimeDistribution[Any] = distribution

  override def canExecute: Boolean = true //!executed //!executed // todo

  override def initializationComplete(time: Time, environment: Environment[Any, _]): Unit = {}

  override def update(time: Time, b: Boolean, environment: Environment[Any, _]): Unit = {}

  override def compareTo(other: Actionable[Any]): Int = getTau.compareTo(other.getTau)

  override def getRate: Double = distribution.getRate

  override def getTau: Time = distribution.getNextOccurence
