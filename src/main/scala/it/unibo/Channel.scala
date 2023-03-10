package it.unibo

import it.unibo.alchemist.model.implementations.actions.DistributedFrpIncarnation
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.distributedfrp.utils.Liftable.{lift, liftTwice}

class Channel extends ProgramFactory:
  def create[P <: Position[P], Any](incarnation: DistributedFrpIncarnation[P]): incarnation.Flow[?] =
    import incarnation.{*, given}
    def distanceTo(src: Flow[Boolean]): Flow[Double] =
      loop(Double.PositiveInfinity) { distance =>
        mux(src) {
          constant(0.0)
        } {
          liftTwice(nbrRange, nbr(distance))(_ + _).withoutSelf.min
        }
      }

    def distanceBetween(source: Flow[Boolean], destination: Flow[Boolean]): Flow[Double] =
      val gradient = distanceTo(source)
      val distance = distanceTo(destination)
      mux(source) {
        distance
      } {
        nbr(lift(distance, gradient)(_ -> _)).withoutSelf.toSet.map(set => set.minByOption(_._2).map(_._1).getOrElse(Double.PositiveInfinity))
      }

    def channel(source: Flow[Boolean], destination: Flow[Boolean], width: Double): Flow[Boolean] =
      val distance = lift(distanceTo(source), distanceTo(destination))(_ - _)
      lift(distance, distanceBetween(source, destination))(_ <= _ + width)

    branch(sensor[Boolean]("obstacle")) {
      constant(false)
    } {
      channel(sensor[Boolean]("source"), sensor[Boolean]("destination"), 0.5)
    }

