package it.unibo.distributedfrp.simulation

import it.unibo.distributedfrp.core.Incarnation
import nz.sodium.Transaction

import java.util.concurrent.{ExecutorService, Executors}

class Simulator(val incarnation: SimulationIncarnation,
                executor: ExecutorService = Executors.newSingleThreadExecutor):

  import incarnation._

  def run[A](flow: Flow[A]): Unit =
    val contexts = (0 until incarnation.environment.nDevices).map(context)
    Transaction.runVoid(() => {
      val exports = contexts.map(ctx => (ctx.selfId, flow.run(Seq.empty)(using ctx)))
      exports.foreach((id, exp) => exp.listen(e => {
        println(s"Device $id exported:\n$e")
        incarnation.environment.neighbors(id).foreach { n =>
          executor.execute(() => contexts(n).receiveExport(id, e))
        }
      }))
    })


