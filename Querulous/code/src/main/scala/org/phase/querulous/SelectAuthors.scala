package org.phase.querulous

case class Author(id: Int, firstName: String, lastName: String)

object ShowAuthors extends TestConnection {

  def main(args: Array[String])
  {
    getAuthors
  }

  def getAuthors() {
    val users = queryEvaluator.select("SELECT * FROM author WHERE id IN (?) OR first_name = ?", List(1,2), "Bill") { row =>
      new Author(row.getInt("id"), row.getString("first_name"), row.getString("last_name"))
    }

    users.map(author => println(author))
  }
}
