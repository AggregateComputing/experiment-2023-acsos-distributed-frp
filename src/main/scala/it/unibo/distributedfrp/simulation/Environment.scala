package it.unibo.distributedfrp.simulation

trait Environment:
  def nDevices: Int
  def position(device: Int): (Double, Double)
  def neighbors(device: Int): Iterable[Int]

object Environment:
  def singleNode: Environment = new Environment:
    override def nDevices: Int = 1
    override def position(device: Int): (Double, Double) = (0, 0)
    override def neighbors(device: Int): Iterable[Int] = Iterable.empty
  
  def manhattanGrid(cols: Int, rows: Int): Environment =
    grid(cols, rows, (col, row) => Seq(
      (col, row),
      (col + 1, row),
      (col - 1, row),
      (col, row + 1),
      (col, row - 1),
    ))

  def euclideanGrid(cols: Int, rows: Int): Environment =
    grid(cols, rows, (col, row) =>
      for
        c <- -1 to 1
        r <- -1 to 1
      yield (col + c, row + r))

  private def grid(cols: Int, rows: Int, candidateNeighbors: (Int, Int) => Iterable[(Int, Int)]): Environment = new Environment:
    private def row(device: Int): Int = device / cols

    private def col(device: Int): Int = device % cols

    override def nDevices: Int = rows * cols

    override def position(device: Int): (Double, Double) = (col(device), row(device))

    override def neighbors(device: Int): Iterable[Int] =
      val deviceCol = col(device)
      val deviceRow = row(device)
      val candidates = candidateNeighbors(deviceCol, deviceRow)
      candidates
        .filter((c, r) => c >= 0 && r >= 0 && c < cols && r < rows)
        .map((c, r) => r * cols + c)
