package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.interfaces.*
import org.danilopianini.util.{ListSet, ListSets}

import java.util
trait AbstractGlobalReaction[P <: Position[P]] extends GlobalReaction[Any]:
  def environment: Environment[Any, P]
  def distribution: TimeDistribution[Any]

  private val actions: util.List[Action[Any]] = util.List.of()
  private val conditions: util.List[Condition[Any]] = util.List.of()

  override def getActions: util.List[Action[Any]] = actions

  override def setActions(list: util.List[_ <: Action[Any]]): Unit =
    actions.clear()
    actions.addAll(list)

  override def setConditions(list: util.List[_ <: Condition[Any]]): Unit =
    conditions.clear()
    conditions.addAll(list)

  override def getConditions: util.List[Condition[Any]] = conditions

  override def getInboundDependencies: ListSet[_ <: Dependency] = ListSets.emptyListSet()

  override def getOutboundDependencies: ListSet[_ <: Dependency] = ListSets.emptyListSet()

  override def getTimeDistribution: TimeDistribution[Any] = distribution

  override def canExecute: Boolean =
    !getTimeDistribution.getNextOccurence.isInfinite // todo

  override def initializationComplete(time: Time, environment: Environment[Any, _]): Unit = {}

  override def update(time: Time, b: Boolean, environment: Environment[Any, _]): Unit = {}

  override def compareTo(other: Actionable[Any]): Int = getTau.compareTo(other.getTau)

  override def getRate: Double = distribution.getRate

  override def getTau: Time = distribution.getNextOccurence
