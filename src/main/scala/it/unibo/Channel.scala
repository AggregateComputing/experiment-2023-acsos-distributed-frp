package it.unibo

import it.unibo.alchemist.model.implementations.actions.DistributedFrpIncarnation
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.distributed.frp.lib.{GradientLib, IncarnationProvider}
import it.unibo.distributedfrp.utils.Liftable.*

class Channel extends ProgramFactory:
  def create[P <: Position[P]](incarnation: DistributedFrpIncarnation[P]): incarnation.Flow[?] =
    val libs = new IncarnationProvider(incarnation) with GradientLib
    import libs.*
    import libs.I.{given, *}

    def channel(source: Flow[Boolean], destination: Flow[Boolean], width: Double): Flow[Boolean] =
      lift(
        gradient(source),
        gradient(destination),
        distanceBetween(source, destination)
      )((source, destination, distanceBetween) =>
        !((source + destination).isInfinite && distanceBetween.isInfinite) && source + destination <= distanceBetween + width
      )

//    channel(sensor[Boolean]("source"), sensor[Boolean]("destination"), 3.0)
    val src = sensor[Boolean]("source")
    val dst = sensor[Boolean]("destination")
    val obs = sensor[Boolean]("obstacle")
    branch(obs) {
      constant(false)
    } {
      channel(src, dst, 0.1)
    }.adapt(incarnation)
//.map(x => (x * 100).round / 100.0)
