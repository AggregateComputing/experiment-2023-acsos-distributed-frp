package it.unibo.distributedfrp.samples

import it.unibo.distributedfrp.frp.IncrementalCellSink
import it.unibo.distributedfrp.simulation.{Environment, SimulationIncarnation, Simulator}
import it.unibo.distributedfrp.utils.Liftable
import it.unibo.distributedfrp.utils.Liftable.*
import nz.sodium.{Cell, CellSink}

def runGradientSimulation(environment: Environment,
                          sources: Cell[Set[Int]],
                          obstacles: Cell[Set[Int]]): Unit =
  val incarnation = SimulationIncarnation(environment, sources, obstacles)
  val simulator = Simulator(incarnation)

  import simulator.incarnation._
  import simulator.incarnation.given

  def gradient(src: Flow[Boolean]): Flow[Double] =
    loop(Double.PositiveInfinity) { distance =>
      mux(src) {
        constant(0.0)
      } {
        liftTwice(nbrRange, nbr(distance))(_ + _).withoutSelf.min
      }
    }

  simulator.run {
    branch(obstacle) {
      constant(-1.0)
    } {
      gradient(source)
    }
  }

@main def gradientSample(): Unit =
  val sourcesSink = IncrementalCellSink(Set(0))
  val obstaclesSink = IncrementalCellSink(Set(2, 7, 12))
  val environment = Environment.manhattanGrid(5, 5)

  runGradientSimulation(environment, sourcesSink.cell, obstaclesSink.cell)

//  Thread.sleep(5000)
//  println("============================================================================================================")
//  sourcesSink.set(Set(4))
//
//  Thread.sleep(5000)
//  println("============================================================================================================")
//  obstaclesSink.set(Set(3, 8, 13, 14))

