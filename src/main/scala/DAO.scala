import slick.jdbc.SQLiteProfile.api._
import slick.sql.SqlProfile.ColumnOption.SqlType

import java.util.concurrent.Executors
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.util.{ Try, Using }

// DONE second column for count
// DONE rewrite to avoid looking like premature optimization
// DONE switch to logging
// DONE architect as CRUD API (DAO) + CLI
// DONE try with sqlite3 -> works!
// TODO update count
// TODO full-text search
// TODO factor out row type
// TODO make DB methods DRY

trait DAO {

  val dbName: String

  private val logger = org.log4s.getLogger

  // set up EC based on non-daemon executor
  val executor = Executors.newSingleThreadExecutor()
  // define context used by futures
  implicit val context = ExecutionContext.fromExecutor(executor)

  class Words(tag: Tag) extends Table[(String, Int)](tag, "WORDS") {
    def id = column[String]("WORD", O.PrimaryKey, SqlType("TEXT"))

    def count = column[Int]("COUNT")

    override def * = (id, count)
  }

  protected def close(): Unit = {
    // shut down non-daemon executor to allow main to complete
    executor.shutdown()
    logger.info("done")
  }

  //  protected def dbWrapper[R](action: Database => R): R = {
  //    action()
  //  }

  def createDatabase(): Unit = {
    // Scala equivalent of try-with-resource for auto-closing db
    Using(Database.forConfig("sqlite")) { db =>
      val f = for {
        () <- Future.unit // point for comprehension to the right monad

        // create table(s)
        words = TableQuery[Words]
        setup = DBIO.seq(words.schema.create)
        () <- db.run(setup)
        () = logger.info("created schema")

      } yield ()

      logger.info("waiting")
      Await.result(f, Duration.Inf)
    }

    close()
  }

  def showWordCounts(): Try[Seq[(String, Int)]] = {
    // Scala equivalent of try-with-resource for auto-closing db
    val rows = Using(Database.forConfig("sqlite")) { db =>
      val f = for {
        () <- Future.unit // point for comprehension to the right monad

        // create table(s)
        words = TableQuery[Words]
        rows <- db.run(words.result)

      } yield rows

      logger.info("waiting")
      Await.result(f, Duration.Inf)
    }

    close()
    rows
  }

  def addWord(word: String): Unit = {
    Using(Database.forConfig("sqlite")) { db =>
      val f = for {
        () <- Future.unit // point for comprehension to the right monad

        // delete matching row
        words = TableQuery[Words]
        insert = words += ((word, 0))
        count <- db.run(insert)
        () = logger.info(f"inserted $count word(s)")

      } yield count

      logger.info("waiting")
      Await.result(f, Duration.Inf)
    }

    logger.info(f"addWord $word")
    close()
  }

  def deleteWord(word: String): Unit = {
    val count = Using(Database.forConfig("sqlite")) { db =>
      val f = for {
        () <- Future.unit // point for comprehension to the right monad

        // delete matching row
        words = TableQuery[Words]
        delete = words.filter(_.id === word).delete
        count <- db.run(delete)
        () = logger.info(f"deleted $count word(s)")

      } yield count

      logger.info("waiting")
      Await.result(f, Duration.Inf)
    }

    logger.info(f"result: $count")
    close()
  }
}
