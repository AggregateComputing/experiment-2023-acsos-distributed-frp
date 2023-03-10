package it.unibo.alchemist.model.implementations.actions


import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.interfaces.{Environment, Node, Position}
import it.unibo.distributedfrp.core.Incarnation
import it.unibo.distributedfrp.frp.IncrementalCellSink
import it.unibo.distributedfrp.simulation.{IncarnationWithEnvironment, TestLocalSensors, TestNeighborSensors}
import nz.sodium.Cell

import _root_.scala.jdk.CollectionConverters.MapHasAsScala
import scala.math.hypot
class DistributedFrpIncarnation[P <: Position[P]](environment: Environment[Any, P]) extends Incarnation
  with TestNeighborSensors:

  override type Context = SimulationContext
  override type NeighborState = SimulationNeighborState
  override type LocalSensorId = String
  override type DeviceId = Int

  override def context(selfId: Int): Context = new SimulationContext(environment.getNodeByID(selfId))

  class SimulationContext(val node: Node[Any]) extends BasicContext:
    override def selfId: DeviceId = node.getId

    val cells = node.getContents.asScala.map {
      case (molecule, value) => molecule.getName -> new IncrementalCellSink(value, calm = true)
    }
    // Todo fix the alignment with molecules
    node.setConcentration(new SimpleMolecule("context"), this)


    cells.foreach((value, cell) => node.setConcentration(new SimpleMolecule(value+"-sink"), cell))
    // Todo think how to call it using reactions
    private val neighborsSink = new IncrementalCellSink[Map[DeviceId, NeighborState]](Map.empty, calm = true)

    def receiveExport(`export`: Export[Any], neighbor: Node[Any]): Unit =
      val neighborState = SimulationNeighborState(node, neighbor, `export`)
      neighborsSink.update(_ + (neighbor.getId -> neighborState))

    def heartbeat(neighbourhood: List[Node[Any]]): Unit =
      neighborsSink.update(old => {
        val newNeighbourhood = neighbourhood.map(_.getId).toSet
        val keys = old.keySet
        val toRemove = (keys -- (newNeighbourhood + node.getId))

        old.view.filterKeys(!toRemove.contains(_)).toMap
      })
    override def neighbors: Cell[Map[DeviceId, NeighborState]] = neighborsSink.cell

    override def sensor[A](id: LocalSensorId): Cell[A] = cells(id).cell.map(_.asInstanceOf[A])

  case class SimulationNeighborState(node: Node[Any],
                                     neighbor: Node[Any],
                                     exported: Export[Any])
    extends BasicNeighborState:
    import SimulationNeighborSensor._
    override def sensor[A](id: NeighborSensorId): A = id match
      case NbrRange =>
        environment.getDistanceBetweenNodes(node, neighbor).asInstanceOf[A]