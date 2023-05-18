package it.unibo.alchemist.loader.`export`.exporters

import it.unibo.alchemist.model.interfaces.{Actionable, Environment, Position, Time}

import java.awt.image.BufferedImage

class SnapshotExporter[T, P <: Position[P]](
    val path: String,
    val name: String,
    val samplingInterval: Double,
    val maxSize: Int
) extends AbstractExporter[T, P](samplingInterval: Double) {
  var snapshots: List[BufferedImage] = List.empty
  override def exportData(environment: Environment[T, P], actionable: Actionable[T], time: Time, l: Long): Unit = {
    // get the current active windows of a swing application
    val currentWindow = java.awt.Window.getWindows.headOption
    currentWindow.foreach { currentWindow =>
      val rootPane = currentWindow.getComponents.head.asInstanceOf[javax.swing.JRootPane]
      val contentPane = rootPane.getContentPane.asInstanceOf[javax.swing.JPanel]
      val simulationPane = contentPane.getComponents.head.asInstanceOf[javax.swing.JPanel]
      val simulationDisplay = simulationPane.getComponents.head
      // create an image with the size of the current container
      val image = new java.awt.image.BufferedImage(
        simulationDisplay.getWidth,
        simulationDisplay.getHeight,
        java.awt.image.BufferedImage.TYPE_INT_ARGB
      )
      simulationDisplay.paint(image.getGraphics)
      javax.imageio.ImageIO.write(image, "png", new java.io.File(path + "/" + name + time.toDouble + ".png"))
    }
  }

  override def close(environment: Environment[T, P], time: Time, l: Long): Unit = {}

  override def setup(environment: Environment[T, P]): Unit = {}
}
