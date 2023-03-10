package it.unibo.distributedfrp.samples

import it.unibo.distributedfrp.simulation.{Simulator, Environment, SimulationIncarnation}
import it.unibo.distributedfrp.utils.Liftable.*

@main def neighborSetSample(): Unit =
  val environment = Environment.euclideanGrid(2, 2)
  val incarnation = new SimulationIncarnation(environment)
  val simulator = new Simulator(incarnation)

  import simulator.incarnation._
  import simulator.incarnation.given

  simulator.run {
    nbr(mid).toSet
  }

