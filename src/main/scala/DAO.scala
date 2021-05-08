import slick.jdbc.H2Profile.api._

import java.util.concurrent.Executors
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.util.Using

trait DAO {

  private val logger = org.log4s.getLogger

  // set up EC based on non-daemon executor
  val executor = Executors.newSingleThreadExecutor()
  // define context used by futures
  implicit val context = ExecutionContext.fromExecutor(executor)

  class Words(tag: Tag) extends Table[(String, Int)](tag, "WORDS") {
    def id = column[String]("WORD", O.PrimaryKey)

    def count = column[Int]("COUNT")

    override def * = (id, count)
  }

  def doSomething(): Unit = {

    // Scala equivalent of try-with-resource for auto-closing db
    val count = Using(Database.forConfig("h2file1")) { db =>
      val f = for {
        () <- Future.unit // point for comprehension to the right monad

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
        // DONE rewrite to avoid looking like premature optimization
        // DONE switch to logging
        // TODO update count
        // TODO full-text search
        // TODO try with sqlite3
        // TODO architect as CRUD API (DAO) + CLI

        // perform query
        () <- db.run(words.result).map(_.foreach(w => logger.info(w.toString)))

      } yield count

      logger.info("waiting")
      Await.result(f, Duration.Inf)
    }

    logger.info(f"result: $count")
    // shut down non-daemon executor to allow main to complete
    executor.shutdown()
    logger.info("done")
  }
}
