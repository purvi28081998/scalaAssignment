import scala.util.control.Breaks._
import scala.io.StdIn

def partition(arr: Array[Int], l: Int, h: Int): Int = {
  var low = l
  val pivot = arr(h)
  for (i <- l until h) {
    if (arr(i) <= pivot) {
      val temp = arr(low)
      arr(low) = arr(i)
      arr(i) = temp
      low += 1
    }
  }
  val temp = arr(low)
  arr(low) = arr(h)
  arr(h) = temp
  low
}

def quickSort(arr: Array[Int], l: Int, h: Int): Unit = {
  if (l < h) {
    val pivot = partition(arr, l, h)
    quickSort(arr, l, pivot - 1)
    quickSort(arr, pivot + 1, h)
  }
}

def mergeSort(arr: Array[Int]): Unit = {
  def mergeSortHelper(start: Int, end: Int): Unit = {
    if (start < end) {
      val mid = (start + end) / 2
      mergeSortHelper(start, mid)
      mergeSortHelper(mid + 1, end)
      merge(start, mid, end)
    }
  }

  def merge(start: Int, mid: Int, end: Int): Unit = {
    val left = arr.slice(start, mid + 1)
    val right = arr.slice(mid + 1, end + 1)
    var i = 0
    var j = 0
    var k = start

    while (i < left.length && j < right.length) {
      if (left(i) <= right(j)) {
        arr(k) = left(i)
        i += 1
      } else {
        arr(k) = right(j)
        j += 1
      }
      k += 1
    }

    while (i < left.length) {
      arr(k) = left(i)
      i += 1
      k += 1
    }

    while (j < right.length) {
      arr(k) = right(j)
      j += 1
      k += 1
    }
  }

  mergeSortHelper(0, arr.length - 1)
}

def binarySearch(arr: Array[Int], target: Int): Int = {
  var l = 0
  var h = arr.length - 1
  var mid = 0
  var result = -1
  breakable {
    while (l <= h) {
      mid = l + (h - l) / 2
      if (arr(mid) == target) {
        result = mid
        break()
      }
      if (arr(mid) < target) l = mid + 1
      else h = mid - 1
    }
  }
  result
}

def getAlgorithm(algoType: String): Either[Array[Int] => Unit, (Array[Int], Int) => Int] = {
  algoType match {
    case "bubble" => Left((arr: Array[Int]) => {
      for (i <- 0 until arr.length - 1) {
        for (j <- 0 until arr.length - i - 1) {
          if (arr(j) > arr(j + 1)) {
            val temp = arr(j)
            arr(j) = arr(j + 1)
            arr(j + 1) = temp
          }
        }
      }
    })
    case "insertion" => Left((arr: Array[Int]) => {
      for (i <- 1 until arr.length) {
        val key = arr(i)
        var j = i - 1
        while (j >= 0 && arr(j) > key) {
          arr(j + 1) = arr(j)
          j -= 1
        }
        arr(j + 1) = key
      }
    })
    case "quick" => Left((arr: Array[Int]) => {
      quickSort(arr, 0, arr.length - 1)
    })
    case "merge" => Left((arr: Array[Int]) => {
      mergeSort(arr)
    })
    case "binarySearch" => Right((arr: Array[Int], target: Int) => binarySearch(arr, target))
  }
}

@main def algorithms(): Unit = {
  println("Add numbers in the array : ")
  var input = StdIn.readLine()
  var arr: Array[Int]= input.split(" ").map(_.toInt)
  println("Choose an algorithm : ")
  var algoType: String = StdIn.readLine()
  if(algoType == "binarySearch"){
    var searchValue:Int = StdIn.readLine().toInt
    val searching = getAlgorithm(algoType)
    searching match {
      case Right(search) => {
        println("Searching in array:")
        val index = search(arr, searchValue)
        println(s"The index of the element is: $index")
      }
      case Left(_) => println("No searching algorithm provided")
    }
  }
  else {
    val sorting = getAlgorithm(algoType)


    sorting match {
      case Left(sort) => {
        println("Sorting array:")
        println(arr.mkString(" "))
        sort(arr)
        println(arr.mkString(" "))
      }
      case Right(_) => println("This is invalid choice.")
    }
  }


}
