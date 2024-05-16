/*
Create a function that takes callback as a parameter

Application:

* the function should pass a 10x10 matrix  with seat numbers

* filled seats must be represented with X

* available seats must be represented with seat numbers

* the user callback must print the matrix

* should return an array of seat numbers to the function

* the function would allocate the seats and callback
with modified matrix

* new seat booking can continue again

* if the user instead of sending array send null
the process should stop
 */
import scala.io.StdIn

def printMatrix(x:Array[Array[String]]): Unit = {
  println("Available seats :")
  for(row <- 0 to rows-1){
    for(col<-0 to cols-1){
      print(matrix(row)(col)+" ")
    }
    println()
  }

}


def seatAllocation(x: Array[Array[String]], seatNumbers: Array[Int]) :Unit={
  for( seat<- seatNumbers){
    var row : Int =  seat/10
    var col : Int = (seat %10)-1
    x(row)(col) = "X"
  }
}

val rows: Int = 10
val cols: Int = 10
var matrix: Array[Array[String]] = Array.ofDim[String](rows, cols)

@main def main(): Unit= {

  for (row <- 0 to rows - 1) {
    for (col <- 0 to cols - 1) {
      matrix(row)(col) = ((row * 10) + (col + 1)).toString
    }
  }
  var flag = true
  printMatrix(matrix)
  while(flag) {
    println("Input space seperated seat numbers to reserve :")
    var seatNumbersList: String = StdIn.readLine()

    if(seatNumbersList =="0")
    {
      println("Process terminated")
      flag = false

    }
    else {
      var seatNumbers: Array[Int] = seatNumbersList.split(" ").map(_.toInt)
      seatAllocation(matrix, seatNumbers)
      printMatrix(matrix)
    }
  }
}