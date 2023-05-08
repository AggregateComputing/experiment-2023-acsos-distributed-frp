package it.unibo.alchemist.model.implementations

import it.unibo.alchemist.boundary.interfaces.OutputMonitor
import it.unibo.alchemist.model.interfaces.{Environment, Position, Time}
import nz.sodium.Listener
import _root_.scala.jdk.CollectionConverters.{IteratorHasAsScala, MapHasAsScala}
class CleanListeners[P <: Position[P]](programListeners: List[Listener]) extends OutputMonitor[Any, P]:
  override def finished(environment: Environment[Any, P], time: Time, step: Long): Unit =
    programListeners.foreach(_.unlisten())
    val elements = environment.getNodes
      .iterator()
      .asScala
      .toList
      .flatMap(_.getContents.asScala.values)
      .collect { case elem if elem.isInstanceOf[Listener] => elem.asInstanceOf[Listener] }
    elements.foreach(_.unlisten())
