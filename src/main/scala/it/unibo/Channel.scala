package it.unibo

import it.unibo.alchemist.model.implementations.actions.DistributedFrpIncarnation
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.distributedfrp.utils.Liftable.*

class Channel extends ProgramFactory:
  def create[P <: Position[P]](incarnation: DistributedFrpIncarnation[P]): incarnation.Flow[?] =
    import incarnation.{*, given}
    def gradient(src: Flow[Boolean]): Flow[Double] =
      loop(Double.PositiveInfinity) { distance =>
        mux(src) {
          constant(0.0)
        } {
          liftTwice(nbrRange, nbr(distance))(_ + _).withoutSelf.min
        }
      }

    def dilate(region: Flow[Boolean], width: Double): Flow[Boolean] =
      gradient(region).map(_ <= width)

    def broadcast[T](source: Flow[Boolean], value: Flow[T]): Flow[T] =
      val broadcastResult = loop[(Double, Option[T])]((Double.PositiveInfinity, None)) { d =>
        mux(source) {
          value.map(0.0 -> Some(_))
        } {
          val n = nbr(d)
          val distances = n.mapTwice(_._1)
          val values = n.mapTwice(_._2)
          val field = liftTwice(distances, nbrRange, values)((ds, ra, va) => (ds + ra) -> va)
          field.withoutSelf.map(_.values.minByOption(_._1).getOrElse((Double.PositiveInfinity, None)))
        }
      }
      lift(broadcastResult, value)(_._2.getOrElse(_))

    def distanceBetween(source: Flow[Boolean], destination: Flow[Boolean]): Flow[Double] =
      broadcast(destination, gradient(source))

    def channel(source: Flow[Boolean], destination: Flow[Boolean], width: Double): Flow[Boolean] =
      dilate(lift(gradient(source), gradient(destination), distanceBetween(source, destination))((s, d, dst) => s + d <= dst), width)

//    channel(sensor[Boolean]("source"), sensor[Boolean]("destination"), 3.0)
    val src = sensor[Boolean]("source")
    val dst = sensor[Boolean]("destination")
    val obs = sensor[Boolean]("obstacle")
    branch(obs) {
      constant(false)
    } {
      channel(src, dst, 1)
    }
    //.map(x => (x * 100).round / 100.0)


