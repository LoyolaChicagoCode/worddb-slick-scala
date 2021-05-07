import slick.jdbc.H2Profile.api._

import java.util.concurrent.Executors
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.Using

object SlickStuff extends App {

  private val logger = org.log4s.getLogger

  // set up EC based on non-daemon executor
  val executor = Executors.newSingleThreadExecutor()
  implicit val context = ExecutionContext.fromExecutor(executor)

  class Words(tag: Tag) extends Table[(String, Int)](tag, "WORDS") {
    def id = column[String]("WORD", O.PrimaryKey)
    def count = column[Int]("COUNT")

    override def * = (id, count)
  }

/*
  --database name of database
  --add-word word  adds a word to the database of words with count 0
  --delete-word word deletes word, if present
  --inc-word-count word increments
  --dec-word-count word decrements
  --show-word-counts
  --find-in-word substring
*/

  // Scala equivalent of try-with-resource for auto-closing db
  val t = Using(Database.forConfig("h2file1")) { db =>
    val f = for { () <- Future.unit // point for comprehension to the right monad

      // create table(s)
      words = TableQuery[Words]
      setup = DBIO.seq(words.schema.create)
      () <- db.run(setup)
      () = logger.info("created schema")

      // insert a few rows
      insert = words ++= Seq("hello", "world", "what", "up").zip(Iterator.continually(0))
      Some(count) <- db.run(insert)
      () = logger.info(f"inserted $count items")

      // DONE second column for count
      // DONE? rewrite to avoid looking like premature optimization
      // TODO update count
      // TODO full-text search
      // TODO switch to logging

      // perform query
      r <- db.run(words.result).map(_.foreach(w => logger.info(w.toString)))

    } yield r
    
    logger.info("waiting")
    Await.result(f, Duration.Inf)
  }

  logger.info(f"result: $t")
  executor.shutdown()
  logger.info("done")
}
