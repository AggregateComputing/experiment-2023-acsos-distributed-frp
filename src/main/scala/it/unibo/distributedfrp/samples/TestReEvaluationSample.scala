package it.unibo.distributedfrp.samples

import it.unibo.distributedfrp.simulation.{Environment, SimulationIncarnation, Simulator}
import it.unibo.distributedfrp.frp.IncrementalCellSink

@main def testReEvaluation(): Unit =
  val sourcesSink = IncrementalCellSink(Set.empty[Int])
  val environment = Environment.singleNode
  val incarnation = SimulationIncarnation(environment, sources = sourcesSink.cell)
  val simulator = Simulator(incarnation)

  import simulator.incarnation._
  import simulator.incarnation.given

  def someIntenseComputation(input: String): String =
    println("Doing some intense computation...")
    input

  simulator.run {
    branch(source) {
      constant("I'm a source device").map(someIntenseComputation)
    } {
      constant("I'm not a source device")
    }
  }

  sourcesSink.update(_ + 0)
  sourcesSink.update(_ - 0)
  sourcesSink.update(_ + 0)
  sourcesSink.update(_ - 0)
