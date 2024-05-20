/*
extend the day4  Task

1. From the CSV filter out all the departments and
add the department names to the department Table

2. create a table employee that refers to department
and add the corresponding employee details into
employees table

3. create case classes for both departments and employees

4. Finally print the department and the employees
under the department in the form of tree for all the
departments
*/

import java.sql.{Connection, DriverManager, ResultSet, Statement}
import scala.io.Source
case class Employee(sno: Int,name: String,city: String,salary:Int,departmentName:String)
case class Department(id: Int,name: String)


def createConnection() = {
  // Establish a connection
  Class.forName("com.mysql.cj.jdbc.Driver")
  val url = "jdbc:mysql://hadoop-server.mysql.database.azure.com:3306/purvi"
  val username = "sqladmin"
  val password = "Password@12345"
  val connection: Connection = DriverManager.getConnection(url, username, password)
  connection
}

@main def employeeInsertSQL(): Unit = {
  val filePath = "/Users/purvigupta/Desktop/scalaAssignment/scala/assignment4-17may/employeeCSV.csv"
  val bufferedSource = Source.fromFile(filePath)
  val lines = bufferedSource.getLines().drop(1).toList
  bufferedSource.close()


  val employeeList : List[Employee] = lines.map{ line =>
    val Array(snoStr, name, city, salaryStr, department)  = line.split(",").map(_.trim)
    Employee(snoStr.toInt, name, city, salaryStr.toInt, department.toString)
  }

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

    println("Data inserted successfully.")

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
        |  FOREIGN KEY (departmentID) REFERENCES Department(id)
        |)
        |""".stripMargin

    statement.execute(createTableEmpSQL)

    println("Employees Table created successfully.")

    for (employee <- employeeList) {
      var dept = employee.departmentName
      var getDeptIDQuery = s"select id from department where name = '$dept'"
      var resultSet = statement.executeQuery(getDeptIDQuery)
      var getDeptID = -1
      while (resultSet.next()) {
        getDeptID = resultSet.getInt("id")
      }
      //println(s"deptID for $dept  = $getDeptID")
      var insertEmpSQL = "INSERT INTO employees(sno,name,city,salary,departmentID) VALUES (" + employee.sno + ",'" + employee.name + "','" + employee.city + "'," + employee.salary + "," + getDeptID + ")"
      //println(insertEmpSQL)
      statement.executeUpdate(insertEmpSQL)
    }

    println("Data inserted successfully.")


    //4. Finally print the department and the employees
    //under the department in the form of tree for all the
    //departments
    var getDeptTable = s"select * from department"
    var resultSetDept = statement.executeQuery(getDeptTable)
    while (resultSetDept.next()) {
      var departmentID = resultSetDept.getInt("id")
      var departmentName = resultSetDept.getString("name")
      println(Department(departmentID,departmentName).toString)
      var getfilteredEmployess = s"select * from employees where departmentID = $departmentID"
      val statementEmp: Statement = conn.createStatement()
      var resultSetEmployees = statementEmp.executeQuery(getfilteredEmployess)
      while (resultSetEmployees.next()) {
        var empSno = resultSetEmployees.getInt("sno")
        var empName = resultSetEmployees.getString("name")
        var empCity = resultSetEmployees.getString("city")
        var empSalary = resultSetEmployees.getInt("salary")
        println("|--" + Employee(empSno, empName, empCity, empSalary, departmentName).toString)
      }
      resultSetEmployees.close()
    }
    resultSetDept.close()
  }
  catch {
    case e: Exception => e.printStackTrace()
  } finally {
    // Close Statement and Connection
    conn.close()
  }
  }


