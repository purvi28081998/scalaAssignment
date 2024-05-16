/*
Create an application
  that asks you to enter (sno,name,city,department)
and add the the tuple record in the appropriate deparment
  and print the organization tree
Create a tree data structure of your own
  Note: Do not use map or any other collection

and for representing (sno,name,city) use tuple
  and the application must be interactive
  need to stop when you say exit

  Organization
    └── Finance
    ├── Payments
    │   ├── (1,Ravi,Chennai)
    │   ├── (2,Ram,Chennai)
    │
    │
    │
    │
    │
    └── Sales
    ├── Marketing
    │   ├── (3,"Rohan","Kolkata")
    │   ├── (4,"RAkesh","Mumbai")
    │
    ├── (5,Ravi,Mumbai)
    ├
    ├── Advertisements
    │   ├── (6,Ricky,Chennai)
    │
    │
    │
    └── SalesManagement
 */

/*
Create an application
  that asks you to enter (sno,name,city,department)
and add the the tuple record in the appropriate deparment
  and print the organization tree
Create a tree data structure of your own
  Note: Do not use map or any other collection

and for representing (sno,name,city) use tuple
  and the application must be interactive
  need to stop when you say exit

  Organization
    └── Finance
    ├── Payments
    │   ├── (1,Ravi,Chennai)
    │   ├── (2,Ram,Chennai)
    │
    │
    │
    │
    │
    └── Sales
    ├── Marketing
    │   ├── (3,"Rohan","Kolkata")
    │   ├── (4,"RAkesh","Mumbai")
    │
    ├── (5,Ravi,Mumbai)
    ├
    ├── Advertisements
    │   ├── (6,Ricky,Chennai)
    │
    │
    │
    └── SalesManagement
 */

import scala.collection.mutable.ArrayBuffer
import scala.io.StdIn
import scala.util.control.Breaks.{break, breakable}
case class Staff(id: Int, fullName: String, city: String)
class Section(val title: String, val employeesList: ArrayBuffer[Staff], val subDivisions: ArrayBuffer[Section]) {
    def enrollStaff(member: Staff, divisionTitle: String): Boolean = {
        if (title == divisionTitle) {
            employeesList += member
            true
        } else {
            subDivisions.exists(_.enrollStaff(member, divisionTitle))
        }
    }
    def exhibitHierarchy(indent: String = ""): Unit = {
        println(s"$indent$title")
        employeesList.foreach(e => println(s"$indent  (${e.id}, ${e.fullName}, ${e.city})"))
        subDivisions.foreach(_.exhibitHierarchy(indent + "  "))
    }
    override def toString: String = s"Section(title=$title, employeesList=${employeesList.mkString(", ")}, subDivisions=${subDivisions.mkString(", ")})"
}
@main def secretTask = {
    val paymentDept = new Section("Payments", ArrayBuffer[Staff](), ArrayBuffer[Section]())
    val financeDept = new Section("Finance", ArrayBuffer[Staff](), ArrayBuffer[Section](paymentDept))
    val marketingDept = new Section("Marketing", ArrayBuffer[Staff](), ArrayBuffer[Section]())
    val adDept = new Section("Advertisements", ArrayBuffer[Staff](), ArrayBuffer[Section]())
    val salesMgmtDept = new Section("SalesManagement", ArrayBuffer[Staff](), ArrayBuffer[Section]())
    val salesDept = new Section("Sales", ArrayBuffer[Staff](), ArrayBuffer[Section](marketingDept, adDept, salesMgmtDept))
    val orgDept = new Section("Organization", ArrayBuffer[Staff](), ArrayBuffer[Section](financeDept, salesDept))
    breakable {
        while (true) {
            println("Enter ID, Full Name, City, Division (or type 'exit' to quit):")
            val line = StdIn.readLine().trim
            if (line == "exit") break()
            val parts = line.split(",").map(_.trim)
            if (parts.length != 4) {
                println("Invalid input. Please enter ID, Full Name, City, and Division separated by commas.")
            } else {
                try {
                    val id = parts(0).toInt
                    val fullName = parts(1)
                    val city = parts(2)
                    val divisionTitle = parts(3)
                    val member = Staff(id, fullName, city)
                    val enrolled = orgDept.enrollStaff(member, divisionTitle)
                    if (!enrolled) {
                        println(s"Division '$divisionTitle' not found.")
                    } else {
                        println(s"Enrolled staff $member in division $divisionTitle.")
                    }
                } catch {
                    case _: NumberFormatException => println("Invalid ID. It should be an integer.")
                }
            }
            // Print the organization hierarchy after each addition
            println("Organization Structure:")
            orgDept.exhibitHierarchy()
        }
    }
}