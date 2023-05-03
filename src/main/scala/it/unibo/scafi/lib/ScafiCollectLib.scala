package it.unibo.scafi.lib

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist.*
trait ScafiCollectLib:
  self: AggregateProgram =>

  def C[V](potential: Double, acc: (V, V) => V, local: V, Null: V): V =
    rep(local) { v =>
      acc(
        local,
        foldhood(Null)(acc) {
          mux(nbr(findParent(potential)) == mid()) {
            nbr(v)
          } {
            nbr(Null)
          }
        }
      )
    }

  def findParent(potential: Double): ID = {
    val (minPotential, devIdWithMinPotential) = minHood(nbr((potential, mid)))
    mux(smaller(minPotential, potential)) {
      devIdWithMinPotential
    } {
      Int.MaxValue
    }
  }

  private def smaller(a: Double, b: Double): Boolean =
    a < b