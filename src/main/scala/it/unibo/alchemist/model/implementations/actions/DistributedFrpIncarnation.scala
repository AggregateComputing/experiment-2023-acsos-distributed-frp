package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.interfaces.{Environment, Node, Position}
import it.unibo.distributed.frp.Molecules
import it.unibo.distributedfrp.core.{ExportTree, Incarnation}
import it.unibo.distributedfrp.frp.IncrementalCellSink
import it.unibo.distributedfrp.simulation.{IncarnationWithEnvironment, TestLocalSensors, TestNeighborSensors}
import nz.sodium.{Cell, CellSink}
import it.unibo.alchemist.model.implementations.PimpAlchemist.*
import it.unibo.alchemist.model.implementations.actions.DistributedFrpIncarnation.FrpContext
import org.apache.commons.math3.random.RandomGenerator
import it.unibo.distributedfrp.utils.Liftable.{lift, map}

import _root_.scala.jdk.CollectionConverters.MapHasAsScala
import scala.math.hypot
class DistributedFrpIncarnation[P <: Position[P]](environment: Environment[Any, P], val random: RandomGenerator)
    extends Incarnation
    with TestNeighborSensors:

  override type Context = SimulationContext
  override type NeighborState = SimulationNeighborState
  override type LocalSensorId = String
  override type DeviceId = Int
  override def context(selfId: Int): Context = new SimulationContext(environment.getNodeByID(selfId))

  class SimulationContext(val node: Node[Any]) extends BasicContext:
    override def selfId: DeviceId = node.getId
    node.setConcentration(Molecules.Context, this)
    val molecules = node.getContents.asScala.toList.map(_._1)
    // SIDE EFFECTS!
    molecules
      .tapEach(molecule => node.createSinkFromMolecule(molecule)) // create sinks for each molecule
      .foreach { molecule => // align molecule with sinks updates
        val listen = node.getSinkFromMolecule(molecule).cell.listenWeak(data => node.setConcentration(molecule, data))
        node.setConcentration(SimpleMolecule(s"listen-$molecule"), listen)
      }
    private val neighborsSink = new IncrementalCellSink[Map[DeviceId, NeighborState]](Map.empty, calm = true)

    def receiveExport(`export`: Export[Any], neighbor: Node[Any]): Unit =
      val neighborState = SimulationNeighborState(node, neighbor, `export`)
      neighborsSink.update(_ + (neighbor.getId -> neighborState))

    def receiveWholeExports(exports: Map[Int, Export[Any]]): Unit =
      val neighborStates =
        exports.map((id, `export`) => id -> SimulationNeighborState(node, environment.getNodeByID(id), `export`))
      neighborsSink.update(_ => neighborStates)

    def heartbeat(neighbourhood: List[Node[Any]]): Unit =
      neighborsSink.update { old =>
        val newNeighbourhood = neighbourhood.map(_.getId).toSet
        val keys = old.keySet
        val toRemove = keys -- (newNeighbourhood + node.getId)

        old.view.filterKeys(!toRemove.contains(_)).toMap
      }
    override def neighbors: Cell[Map[DeviceId, NeighborState]] = neighborsSink.cell

    override def sensor[A](id: LocalSensorId): Cell[A] =
      node.getSinkFromMolecule(new SimpleMolecule(id)).cell.map(_.asInstanceOf[A])

  case class SimulationNeighborState(node: Node[Any], neighbor: Node[Any], exported: Export[Any])
      extends BasicNeighborState:
    import SimulationNeighborSensor.*
    override def sensor[A](id: NeighborSensorId): A = id match
      case NbrRange =>
        environment.getDistanceBetweenNodes(node, neighbor).asInstanceOf[A]
  extension [A](flow: Flow[A])
    def log(key: String): Flow[A] =
      val id = mid.map(environment.getNodeByID)
      lift(id, flow) { (node, data) =>
        node.setConcentration(SimpleMolecule(s"log-$key"), data)
        data
      }

    def tap(f: A => Unit): Flow[A] =
      map(flow) { data =>
        f(data)
        data
      }

    def tapEachWithNode(f: (A, Node[Any]) => Unit): Flow[A] =
      val id = mid.map(environment.getNodeByID)
      lift(id, flow) { (node, data) =>
        f(data, node)
        data
      }

    def logComputedTimes(name: String): Flow[A] =
      flow.tapEachWithNode((_, node) => node.evaluated(name))
  extension (context: Context)
    def storeTicks(): Unit = if context.node.contains(Molecules.Rounds) then
      val ticks = context.node.getConcentration(Molecules.Rounds).asInstanceOf[Double]
      context.node.setConcentration(Molecules.Rounds, ticks + 1.0)
    else context.node.setConcentration(Molecules.Rounds, 0.0)
object DistributedFrpIncarnation:
  type FrpContext = DistributedFrpIncarnation[_]#Context
  type Export[A] = DistributedFrpIncarnation[_]#Export[A]
