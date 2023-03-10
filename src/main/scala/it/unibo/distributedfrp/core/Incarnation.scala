package it.unibo.distributedfrp.core

import it.unibo.distributedfrp.core.*

trait Incarnation extends Core, RichLanguage, Semantics:
  def context(selfId: DeviceId): Context
