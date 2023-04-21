package it.unibo.distributed.frp.lib

import it.unibo.distributedfrp.core.Incarnation

class IncarnationProvider[I <: Incarnation](val I: I):
  extension [A](flow: I.Flow[A])
    def adapt(incarnation: I): incarnation.Flow[A] =
      flow.asInstanceOf[incarnation.Flow[A]]
