import slick.jdbc.H2Profile.api._

import java.util.concurrent.Executors
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.Using

object SlickStuff extends App {

  // set up EC based on non-daemon executor
  val executor = Executors.newSingleThreadExecutor()
  implicit val context = ExecutionContext.fromExecutor(executor)

  class Words(tag: Tag) extends Table[(String, Int)](tag, "WORDS") {
    def id = column[String]("WORD", O.PrimaryKey)
    def count = column[Int]("COUNT")

    override def * = (id, count)
  }

  // Scala equivalent of try-with-resource for auto-closing db
  val t = Using(Database.forConfig("h2file1")) { db =>
    val f = for { () <- Future.unit // point for comprehension to the right monad

      // create table(s)
      words = TableQuery[Words]
      setup = DBIO.seq(words.schema.create)
      () <- db.run(setup)
      () = println("+created schema")

      // insert a few rows
      insert = words ++= Seq("hello", "world", "what", "up").zip(Iterator.continually(0))
      Some(count) <- db.run(insert)
      () = println(f"+inserted $count items")

      // DONE second column for count
      // DONE? rewrite to avoid looking like premature optimization
      // TODO update count
      // TODO full-text search
      // TODO switch to logging

      // perform query
      r <- db.run(words.result).map(_.foreach(println))

    } yield r
    println("+waiting")
    Await.result(f, Duration.Inf)
  }

  println(f"+result: $t")
  executor.shutdown()
  println("+done")
}
