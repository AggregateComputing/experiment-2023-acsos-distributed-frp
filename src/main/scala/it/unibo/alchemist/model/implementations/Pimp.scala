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

    def updateConcentration[A](molecule: Molecule, logic: A => A): Unit =
      node.setConcentration(molecule, logic(node.getConcentration(molecule).asInstanceOf[A]))
    
    def evaluated(what: String): Unit =
      if(!node.contains(SimpleMolecule(what))) node.setConcentration(SimpleMolecule(what), 0)
      node.updateConcentration[Int](SimpleMolecule(what), _ + 1)  
    