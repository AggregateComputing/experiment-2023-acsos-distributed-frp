package it.unibo.distributedfrp.samples

import it.unibo.distributedfrp.simulation.{Environment, SimulationIncarnation, Simulator}
import it.unibo.distributedfrp.utils.Liftable.lift

@main def loopSample(): Unit =
  val environment = Environment.manhattanGrid(2, 1)
  val incarnation = SimulationIncarnation(environment)
  val simulator = Simulator(incarnation)

  import simulator.incarnation._
  import simulator.incarnation.given

  simulator.run {
    loop(0) { x => x.map(_ + 1) }
  }
