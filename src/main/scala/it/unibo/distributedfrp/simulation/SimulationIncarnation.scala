package it.unibo.distributedfrp.simulation

import it.unibo.distributedfrp.core.Incarnation
import it.unibo.distributedfrp.frp.IncrementalCellSink
import nz.sodium.Cell

import scala.math._

class SimulationIncarnation(environment: Environment,
                            sources: Cell[Set[Int]] = new Cell(Set.empty),
                            obstacles: Cell[Set[Int]] = new Cell(Set.empty))
  extends Incarnation
    with IncarnationWithEnvironment(environment)
    with TestLocalSensors
    with TestNeighborSensors:


  override type Context = SimulationContext
  override type NeighborState = SimulationNeighborState

  override def context(selfId: DeviceId): Context = SimulationContext(selfId)

  class SimulationContext(val selfId: DeviceId) extends BasicContext:
    private val neighborsSink = new IncrementalCellSink[Map[DeviceId, NeighborState]](Map.empty, calm = true)

    def receiveExport(neighborId: DeviceId, exported: Export[Any]): Unit =
      neighborsSink.update(_ + (neighborId -> SimulationNeighborState(selfId, neighborId, exported)))

    override def neighbors: Cell[Map[DeviceId, NeighborState]] = neighborsSink.cell

    import SimulationLocalSensor._
    override def sensor[A](id: LocalSensorId): Cell[A] = id match
      case Source => sources.map(_.contains(selfId).asInstanceOf[A])
      case Obstacle => obstacles.map(_.contains(selfId).asInstanceOf[A])

  case class SimulationNeighborState(selfId: DeviceId,
                                     neighborId: DeviceId,
                                     exported: Export[Any])
    extends BasicNeighborState:
    import SimulationNeighborSensor._
    override def sensor[A](id: NeighborSensorId): A = id match
      case NbrRange =>
        val selfPos = environment.position(selfId)
        val neighborPos = environment.position(neighborId)
        hypot(selfPos._1 - neighborPos._1, selfPos._2 - neighborPos._2).asInstanceOf[A]