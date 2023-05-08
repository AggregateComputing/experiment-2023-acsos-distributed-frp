package it.unibo.distributed.frp

import it.unibo.distributedfrp.core.Incarnation

object lang:
  extension [I <: Incarnation](incarnation: I)
    def scope[A](computation: I ?=> incarnation.Flow[A]): incarnation.Flow[A] =
      computation(using incarnation)
