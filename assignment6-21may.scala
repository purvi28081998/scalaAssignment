
/*
Task1
----- 
Modify the task of day5 
to process the csv records using thread pool
to encourage parellel processing

and modify the employee table slightly
with additional columns threadname and timestamp just
to have an entry of the thread id that
entered the record

Task2
-----

From the employee table a 4 member
from sales need to be sent on assignment
to client

Create a function that gives
all the combinations that are possible (Note: Not
permutations)
Use Backtracking concept to come up with the algorithm

*/

import java.sql.{Connection, DriverManager, ResultSet, Statement, Timestamp}
import scala.io.Source
import java.util.concurrent.*
import java.time.LocalDateTime
import scala.collection.mutable.ArrayBuffer


case class Employee2(sno: Int,name: String,city: String,salary:Int,departmentName:String, threadName: String,timestamp : Timestamp)
case class Department1(id: Int,name: String)


def createConnection1() = {
  // Establish a connection
  Class.forName("com.mysql.cj.jdbc.Driver")
  val url = "jdbc:mysql://hadoop-server.mysql.database.azure.com:3306/purvi"
  val username = "sqladmin"
  val password = "Password@12345"
  val connection: Connection = DriverManager.getConnection(url, username, password)
  connection
}

def findCombinations(nums: List[Int]): List[List[Int]] = {
  val result = scala.collection.mutable.ListBuffer[List[Int]]()
  def backtrack(start: Int, current: List[Int]): Unit = {
    result += current
    for (i <- start until nums.length) {
      backtrack(i + 1, current :+ nums(i))
    }
  }
  backtrack(0, List())
  result.toList
}

@main def employeeInsertSQL1(): Unit = {
  val filePath = "/Users/purvigupta/Desktop/scala Assignment New/src/main/scala/assignment4-17may/employeeCSV.csv"
  val bufferedSource = Source.fromFile(filePath)
  val lines = bufferedSource.getLines().drop(1).toList

  bufferedSource.close()


  var employeeList : Array[Employee2] = lines.map{ line =>
    val Array(snoStr, name, city, salaryStr, department )  = line.split(",").map(_.trim)
    Employee2(snoStr.toInt, name, city, salaryStr.toInt, department.toString ,"".toString , Timestamp.valueOf("1900-01-01 00:00:00"))
  }.toArray

  //1. From the CSV filter out all the departments and
  //add the department names to the department Table
  val distinctDept = employeeList.map(employee => employee.departmentName).distinct
  //println(distinctDept)

  var conn = createConnection()
  try {
    val statement: Statement = conn.createStatement()

    var dropEmpTableSQL = "drop TABLE IF EXISTS Employees"
    statement.execute(dropEmpTableSQL)
    println("Employees Table Dropped successfully.")

    val dropDeptTableSQL = "DROP TABLE IF EXISTS Department"
    statement.execute(dropDeptTableSQL)
    println("Department Table Dropped successfully.")

    var createTableDeptSQL =
      """
        |CREATE TABLE IF NOT EXISTS Department (
        |  id INT AUTO_INCREMENT PRIMARY KEY,
        |  name VARCHAR(100)
        |)
        |""".stripMargin

    statement.execute(createTableDeptSQL)

    println("Department Table created successfully.")

    // Insert data into department
    for (dept <- distinctDept) {
      var insertDeptSQL = "INSERT INTO department(name) VALUES ('" + dept + "')"
      statement.executeUpdate(insertDeptSQL)
    }

    println("Data inserted successfully into Department.")

    //2. create a table employee that refers to department
    //and add the corresponding employee details into
    //employees table


    var createTableEmpSQL =
      """
        |CREATE TABLE IF NOT EXISTS Employees (
        |  sno INT,
        |  name VARCHAR(100),
        |  city VARCHAR(100),
        |  salary INT,
        |  departmentID INT,
        |  threadName VARCHAR(100),
        |  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        |  FOREIGN KEY (departmentID) REFERENCES Department(id)
        |)
        |""".stripMargin

    statement.execute(createTableEmpSQL)

    println("Employees Table created successfully.")


    // Create a thread pool with fixed number of threads
    //val pool: ExecutorService = Executors.newFixedThreadPool(distinctDept.length)
    val pool: ExecutorService = Executors.newFixedThreadPool(4)

    // Submit tasks to the thread pool
    for (employee <- employeeList) {
      pool.submit(new Runnable {
        def run(): Unit = {
          var dept = employee.departmentName
          var getDeptIDQuery = s"select id from department where name = '$dept'"
          var resultSet = statement.executeQuery(getDeptIDQuery)
          var getDeptID = -1
          while (resultSet.next()) {
            getDeptID = resultSet.getInt("id")
          }
          //println(s"deptID for $dept  = $getDeptID")
          var insertEmpSQL = "INSERT INTO employees(sno,name,city,salary,departmentID,threadName) VALUES (" + employee.sno + ",'" + employee.name + "','" + employee.city + "'," + employee.salary + "," + getDeptID + ",'" + Thread.currentThread().getName + "')"
          println(insertEmpSQL)
          statement.executeUpdate(insertEmpSQL)

        }
      })
    }

    // Shutdown the thread pool after tasks are complete
    pool.shutdown()
    try {
      if (!pool.awaitTermination(100, TimeUnit.SECONDS)) {
        println("Thread pool did not terminate in the specified time.")
        pool.shutdownNow()
      } else {
        println("Thread pool terminated successfully.")
      }
    } catch {
      case e: InterruptedException =>
        println("Thread pool termination was interrupted.")
        pool.shutdownNow()
    }






    //4. Finally print the department and the employees
    //under the department in the form of tree for all the
    //departments
    var getDeptTable = s"select * from department"
    var resultSetDept = statement.executeQuery(getDeptTable)
    while (resultSetDept.next()) {
      var departmentID = resultSetDept.getInt("id")
      var departmentName = resultSetDept.getString("name")
      println(Department1(departmentID,departmentName).toString)
      var getfilteredEmployess = s"select * from employees where departmentID = $departmentID"
      val statementEmp: Statement = conn.createStatement()
      var resultSetEmployees = statementEmp.executeQuery(getfilteredEmployess)
      while (resultSetEmployees.next()) {
        var empSno = resultSetEmployees.getInt("sno")
        var empName = resultSetEmployees.getString("name")
        var empCity = resultSetEmployees.getString("city")
        var empSalary = resultSetEmployees.getInt("salary")
        var threadName = resultSetEmployees.getString("threadName")
        var timestamp = resultSetEmployees.getTimestamp("timestamp")
        println("|--" + Employee2(empSno, empName, empCity, empSalary, departmentName, threadName , timestamp ).toString)
      }
      resultSetEmployees.close()
    }
    resultSetDept.close()


    //-----Task 2 --------//
    var getsalesEmpQuery = s"select sno from employees where departmentID = (select id from department where name = 'Sales')"
    var resultSetSalesEmp = statement.executeQuery(getsalesEmpQuery)
    var empID : ArrayBuffer[Int] = ArrayBuffer.empty[Int]
    while (resultSetSalesEmp.next()) {
      var resultSetSalesEmpSno = resultSetSalesEmp.getInt("sno")
      empID+=resultSetSalesEmpSno
    }
    var empIDL  = empID.toList

    var comb = findCombinations(empIDL)
    for(ele <- comb){
      println(ele)
    }

  }
  catch {
    case e: Exception => e.printStackTrace()
  } finally {
    // Close Statement and Connection
    conn.close()
  }
  }


