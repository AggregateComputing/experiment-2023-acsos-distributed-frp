package it.unibo.distributedfrp.simulation

import it.unibo.distributedfrp.core.Incarnation

trait TestNeighborSensors:
  self: Incarnation =>

  override type NeighborSensorId = SimulationNeighborSensor

  enum SimulationNeighborSensor:
    case NbrRange

  import SimulationNeighborSensor._

  def nbrRange: Flow[NeighborField[Double]] = nbrSensor[Double](NbrRange)