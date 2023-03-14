package it.unibo.distributedfrp.core

import it.unibo.distributedfrp.core.*
import it.unibo.distributedfrp.core.Slot.*
import it.unibo.distributedfrp.frp.FrpExtensions.*
import it.unibo.distributedfrp.frp.FrpExtensions.given
import it.unibo.distributedfrp.utils.Liftable
import it.unibo.distributedfrp.utils.Liftable.lift
import nz.sodium.time.SecondsTimerSystem
import nz.sodium.{Cell, CellLoop, Operational, Stream, Transaction}

trait Semantics:
  self: Core with Language =>

  type NeighborState <: BasicNeighborState
  override type Context <: BasicContext
  override type NeighborField[+A] = Map[DeviceId, A]
  override type Export[+A] = ExportTree[A]
  override type Path = Seq[Slot]

  trait BasicNeighborState:
    def sensor[A](id: NeighborSensorId): A
    def exported: Export[Any]

  trait BasicContext:
    def selfId: DeviceId
    def sensor[A](id: LocalSensorId): Cell[A]
    def neighbors: Cell[Map[DeviceId, NeighborState]]

  override val neighborFieldLiftable: Liftable[NeighborField] = new Liftable[NeighborField]:
    def lift[A, B](a: NeighborField[A])(f: A => B): NeighborField[B] =
      a.map((d, x) => (d, f(x)))

    override def lift[A, B, C](a: NeighborField[A], b: NeighborField[B])(f: (A, B) => C): NeighborField[C] =
      val commonDevices = a.keySet intersect b.keySet
      commonDevices.map(x => (x, f(a(x), b(x)))).toMap

    override def lift[A, B, C, D](a: NeighborField[A], b: NeighborField[B], c: NeighborField[C])(f: (A, B, C) => D): NeighborField[D] =
      val commonDevices = a.keySet intersect b.keySet intersect c.keySet
      commonDevices.map(x => (x, f(a(x), b(x), c(x)))).toMap

  extension[A] (field: NeighborField[A])
    def withNeighbor(neighborId: DeviceId, value: A): NeighborField[A] =
      field + (neighborId -> value)

    def withoutNeighbor(neighborId: DeviceId): NeighborField[A] =
      field - neighborId

    def foldLeft[R](seed: R)(combine: (R, A) => R): R =
      field.values.foldLeft(seed)(combine)

  object Flows:
    def of[A](f: Context ?=> Path => Cell[Export[A]]): Flow[A] = new Flow[A]:
      override def run(path: Path)(using Context): Cell[Export[A]] = f(path).calm

    def fromCell[A](cell: Context ?=> Cell[A]): Flow[A] = of(_ => cell.map(ExportTree(_)))

    def constant[A](a: Context ?=> A): Flow[A] = fromCell(new Cell(a))

  private def ctx(using Context): Context = summon[Context]

  override val flowLiftable: Liftable[Flow] = new Liftable[Flow]:
    override def lift[A, B](a: Flow[A])(f: A => B): Flow[B] =
      Flows.of { path =>
        a.run(path :+ Operand(0)).map(e => ExportTree(f(e.root), Operand(0) -> e))
      }

    override def lift[A, B, C](a: Flow[A], b: Flow[B])(f: (A, B) => C): Flow[C] =
      Flows.of { path =>
        Liftable.lift(
          a.run(path :+ Operand(0)),
          b.run(path :+ Operand(1))
        )(
          (aa, bb) => ExportTree(
            f(aa.root, bb.root),
            Operand(0) -> aa,
            Operand(1) -> bb)
        )
      }

    override def lift[A, B, C, D](a: Flow[A], b: Flow[B], c: Flow[C])(f: (A, B, C) => D): Flow[D] =
      Flows.of { path =>
        Liftable.lift(
          a.run(path :+ Operand(0)),
          b.run(path :+ Operand(1)),
          c.run(path :+ Operand(2))
        )(
          (aa, bb, cc) => ExportTree(
            f(aa.root, bb.root, cc.root),
            Operand(0) -> aa,
            Operand(1) -> bb,
            Operand(2) -> cc)
        )
      }

  private def alignWithNeighbors[T](path: Path, extract: (Export[Any], NeighborState) => T)(using ctx: Context): Cell[Map[DeviceId, T]] =
    def alignWith(neighborId: DeviceId, neighborState: NeighborState): Option[(DeviceId, T)] =
      neighborState
        .exported
        .followPath(path)
        .map(alignedExport => (neighborId, extract(alignedExport, neighborState)))

    ctx.neighbors.map(_.flatMap((neighborId, neighborState) => alignWith(neighborId, neighborState)))

  override val mid: Flow[DeviceId] = Flows.constant(ctx.selfId)

  override def constant[A](a: A): Flow[A] = Flows.constant(a)

  override def sensor[A](id: LocalSensorId): Flow[A] = Flows.fromCell(ctx.sensor[A](id))

  override def nbr[A](a: Flow[A]): Flow[NeighborField[A]] =
    Flows.of { path =>
      val alignmentPath = path :+ Nbr
      val neighboringValues = alignWithNeighbors(alignmentPath, (e, _) => e.root.asInstanceOf[A])
      lift(a.run(alignmentPath), neighboringValues){ (exp, n) =>
        val neighborField = n + (ctx.selfId -> exp.root)
        ExportTree(neighborField, Nbr -> exp)
      }
    }

  override def nbrSensor[A](id: NeighborSensorId): Flow[NeighborField[A]] =
    Flows.of { path =>
      alignWithNeighbors(path, (_, n) => n.sensor[A](id)).map(ExportTree(_))
    }

  private def conditional[A](cond: Flow[Boolean])(th: Flow[A])(el: Flow[A])(combine: (Export[Boolean], Export[A], Export[A]) => Export[A]): Flow[A] =
    Flows.of { path =>
      val condExport = cond.run(path :+ Condition)
      val thenExport = th.run(path :+ Then)
      val elseExport = el.run(path :+ Else)
      lift(condExport, thenExport, elseExport)(combine)
    }

  override def branch[A](cond: Flow[Boolean])(th: Flow[A])(el: Flow[A]): Flow[A] =
    conditional(cond)(th)(el) { (c, t, e) =>
      val selected = if c.root then t else e
      val selectedSlot = if c.root then Then else Else
      ExportTree(selected.root, Condition -> c, selectedSlot -> selected)
    }

  def mux[A](cond: Flow[Boolean])(th: Flow[A])(el: Flow[A]): Flow[A] =
    conditional(cond)(th)(el) { (c, t, e) =>
      ExportTree(if c.root then t.root else e.root, Condition -> c, Then -> t, Else -> e)
    }

  override def loop[A](init: A)(f: Flow[A] => Flow[A]): Flow[A] =
    Flows.of { path =>
      val prev = ctx
        .neighbors
        .map(nbrs => {
          nbrs
            .get(ctx.selfId)
            .flatMap(_.exported.followPath(path))
            .map(e => ExportTree(e.root.asInstanceOf[A]))
            .getOrElse(ExportTree(init))
        })
      f(Flows.of(_ => prev)).run(path)
    }