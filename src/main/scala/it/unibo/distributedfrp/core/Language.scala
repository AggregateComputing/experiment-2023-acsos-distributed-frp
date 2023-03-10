package it.unibo.distributedfrp.core

import it.unibo.distributedfrp.utils.Liftable

trait Language:
  self: Core =>

  type DeviceId
  type NeighborField[+_]
  type LocalSensorId
  type NeighborSensorId

  val flowLiftable: Liftable[Flow]
  val neighborFieldLiftable: Liftable[NeighborField]
  given Liftable[Flow] = flowLiftable
  given Liftable[NeighborField] = neighborFieldLiftable

  def mid: Flow[DeviceId]
  def constant[A](a: A): Flow[A]
  def sensor[A](id: LocalSensorId): Flow[A]
  def branch[A](cond: Flow[Boolean])(th: Flow[A])(el: Flow[A]): Flow[A]
  def mux[A](cond: Flow[Boolean])(th: Flow[A])(el: Flow[A]): Flow[A]
  def loop[A](init: A)(f: Flow[A] => Flow[A]): Flow[A]
  def nbr[A](a: Flow[A]): Flow[NeighborField[A]]
  def nbrSensor[A](id: NeighborSensorId): Flow[NeighborField[A]]

  extension[A] (field: NeighborField[A])
    def foldLeft[T](seed: T)(combine: (T, A) => T): T
    def withNeighbor(id: DeviceId, value: A): NeighborField[A]
    def withoutNeighbor(id: DeviceId): NeighborField[A]
