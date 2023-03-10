package it.unibo.distributedfrp.simulation

import it.unibo.distributedfrp.core.Incarnation

trait TestLocalSensors:
  self: Incarnation =>

  override type LocalSensorId = SimulationLocalSensor

  enum SimulationLocalSensor:
    case Source
    case Obstacle
  
  import SimulationLocalSensor._

  def source: Flow[Boolean] = sensor[Boolean](Source)

  def obstacle: Flow[Boolean] = sensor[Boolean](Obstacle)
