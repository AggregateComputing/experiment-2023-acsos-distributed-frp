package it.unibo

import it.unibo.alchemist.model.implementations.actions.DistributedFrpIncarnation
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.distributedfrp.utils.Liftable.liftTwice

class Gradient extends ProgramFactory:
  def create[P <: Position[P], Any](incarnation: DistributedFrpIncarnation[P]): incarnation.Flow[?] =
    import incarnation.{*, given}
    def gradient(src: Flow[Boolean]): Flow[Double] =
      loop(Double.PositiveInfinity) { distance =>
        mux(src) {
          constant(0.0)
        } {
          liftTwice(nbrRange, nbr(distance))(_ + _).withoutSelf.min
        }
      }
      
    branch(sensor[Boolean]("obstacle")) {
      constant(-1.0)
    } {
      gradient(sensor[Boolean]("source"))
    }
