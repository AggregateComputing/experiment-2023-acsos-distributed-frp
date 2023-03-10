package it.unibo.distributedfrp.core

import nz.sodium._

trait Core:
  type Context
  type Path
  type Export[+_]

  trait Flow[A]:
    def run(path: Path)(using Context): Cell[Export[A]]
