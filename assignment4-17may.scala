/*
sno,name,city,salary,department
1,John Doe,New York,60000,Marketing
2,Jane Smith,Los Angeles,55000,Finance
3,Michael Johnson,Chicago,62000,HR
4,Sarah Lee,San Francisco,58000,Engineering
5,David Brown,Houston,63000,Operations
6,Emily Taylor,Miami,54000,Sales
7,James Wilson,Dallas,59000,Marketing
8,Emma Anderson,Seattle,61000,Finance
9,Matthew Martinez,Boston,57000,HR
10,Olivia Thomas,Atlanta,64000,Engineering
11,Liam Garcia,Denver,53000,Operations
12,Sophia Hernandez,Phoenix,60000,Sales
13,Alexander Lopez,Philadelphia,56000,Marketing
14,Mia Gonzalez,Detroit,62000,Finance
15,Ethan Perez,Minneapolis,55000,HR
16,Isabella Carter,Portland,63000,Engineering
17,Mason Torres,Baltimore,52000,Operations
18,Ava Ramirez,San Diego,64000,Sales
19,Logan Sanders,Washington,57000,Marketing
20,Harper King,Birmingham,61000,Finance
21,Lucas Rivera,Memphis,59000,HR
22,Evelyn Scott,Nashville,60000,Engineering
23,Avery Green,Milwaukee,56000,Operations
24,Lincoln Taylor,Indianapolis,62000,Sales
25,Sofia Cooper,Louisville,54000,Marketing
26,Aiden Bennett,Jacksonville,63000,Finance
27,Madison Flores,Charlotte,58000,HR
28,Ella Collins,San Jose,65000,Engineering
29,Chloe Howard,San Antonio,53000,Operations
30,Carter Hughes,Oklahoma City,61000,Sales
31,Aria Nelson,Tucson,57000,Marketing
32,Grayson Baker,Cleveland,60000,Finance
33,Zoey Morris,Mesa,55000,HR
34,Nova Ward,Kansas City,64000,Engineering
35,Hudson James,Virginia Beach,52000,Operations
36,Leah Watson,Atlanta,62000,Sales
37,Riley Brooks,Colorado Springs,54000,Marketing
38,Emery Kelly,Raleigh,59000,Finance
39,Luke Reed,Omaha,61000,HR
40,Layla Cooper,Miami,56000,Engineering
41,Ayden Rivera,Tulsa,63000,Operations
42,Penelope Coleman,Oakland,58000,Sales
43,Lincoln Powell,Minneapolis,65000,Marketing
44,Aubrey Long,Arlington,60000,Finance
45,Grace Hughes,Bakersfield,55000,HR
46,Nolan Ward,Tampa,64000,Engineering
47,Bella Barnes,Honolulu,52000,Operations
48,Ellie Howard,Anaheim,62000,Sales
49,Liam Wright,Pittsburgh,56000,Marketing
50,Emma Evans,Cincinnati,61000,Finance

Store the above data in a csv

1. Read the text file (csv)
2. create the case Class to Represent the entity
3. add all the records in to List
4. Perform filter operations based on salary and departments
5. perform map operations to produce formatted report
6. perform reduce operation to find total Salary, average
Salary, number of employees department wise
 */

import scala.io.Source
//2. create the case Class to Represent the entity
case class Employee(sno: Int,name: String,city: String,salary:Int,department:String)

@main def employeeAssignment(): Unit = {
  val filePath = "./employeeCSV.csv"
  // 1. Read the text file (csv)
  val bufferedSource = Source.fromFile(filePath)
  val lines = bufferedSource.getLines().drop(1).toList
  bufferedSource.close()

  //3. add all the records in to List
  val employeeList : List[Employee] = lines.map{ line =>
    val Array(snoStr, name, city, salaryStr, department)  = line.split(",").map(_.trim)
    Employee(snoStr.toInt, name, city, salaryStr.toInt, department)
  }

  //4. Perform filter operations based on salary and departments
  val filteredList = employeeList.filter(employee => employee.salary > 56000 && employee.department == "Sales")


  //5. perform map operations to produce formatted report
  val formattedReport = filteredList.map{employee => employee.toString}
  println("-------Formatted Report Starts----------")
  formattedReport.foreach(println)
  println("-------Formatted Report Ends------------")
  //6. perform reduce operation to find total Salary, average Salary, number of employees department wise
  val groupedByDepartment = employeeList.groupBy(_.department)
  val departmentStats = groupedByDepartment.mapValues { employees =>
    val totalSalary = employees.map(_.salary).sum
    val averageSalary = totalSalary.toDouble / employees.length
    val numEmployees = employees.length
    (totalSalary, averageSalary, numEmployees)
  }
  println("\n-------Departmentwise Report Starts----------")
  for(dept <- departmentStats){
    println("department:" + dept(0) +", totalSalary: "+dept(1)(0)+", averageSalary: "+dept(1)(1)+", numEmployees:"+dept(1)(1))

  }
  println("-------Departmentwise Report Ends----------")


}