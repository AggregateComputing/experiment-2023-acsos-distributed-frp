package it.unibo.distributed.frp

import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule

object Molecules:
  val Context = SimpleMolecule("context")
  val LastComputationTime = SimpleMolecule("lastTime")
  val Root = SimpleMolecule("root")
  val Export = SimpleMolecule("export")
  val TimeDifference = SimpleMolecule("timeDiff")
  val Rounds = SimpleMolecule("rounds")
  val MessagesSent = SimpleMolecule("messages")
