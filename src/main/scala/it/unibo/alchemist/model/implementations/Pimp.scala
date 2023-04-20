package it.unibo.alchemist.model.implementations

import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.interfaces.{Molecule, Node}
import it.unibo.distributedfrp.frp.IncrementalCellSink

object PimpAlchemist:
  extension (node: Node[Any])
    def getSinkFromMolecule(molecule: Molecule): IncrementalCellSink[Any] =
      node.getConcentration(SimpleMolecule(molecule.getName + "-sink")).asInstanceOf[IncrementalCellSink[Any]]

    def createSinkFromMolecule(molecule: Molecule): IncrementalCellSink[Any] =
      val sink = IncrementalCellSink(node.getConcentration(molecule))
      node.setConcentration(SimpleMolecule(molecule.getName + "-sink"), sink)
      sink
