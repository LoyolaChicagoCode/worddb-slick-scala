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

  val words = TableQuery[Words]

  protected def close(): Unit = {
    // shut down non-daemon executor to allow main to complete
    executor.shutdown()
    logger.info("done")
  }

  protected def dbWrapper[R](action: Database => Future[R]): Try[R] = {
    val result = Using(Database.forConfig("sqlite")) { db =>
      val f = action(db)
      logger.info("waiting")
      Await.result(f, Duration.Inf)
    }
    close()
    result
  }

  def createDatabase(): Unit = dbWrapper { db =>
    // Scala equivalent of try-with-resource for auto-closing db
    for {
      () <- db.run {
        DBIO.seq(words.schema.create)
      }
      () = logger.info("created schema")
    } yield ()
  }

  def showWordCounts(): Try[Seq[(String, Int)]] = dbWrapper { db =>
    for {
      rows <- db.run {
        words.result
      }
      () = logger.info(f"retrieved rows $rows")
    } yield rows
  }

  def addWord(word: String): Unit = dbWrapper { db =>
    for {
      count <- db.run {
        words += ((word, 0))
      }
      () = logger.info(f"inserted $count word(s)")
    } yield count
  }

  def deleteWord(word: String): Unit = dbWrapper { db =>
    for {
      count <- db.run {
        words.filter(_.id === word).delete
      }
      () = logger.info(f"deleted $count word(s)")
    } yield count
  }
}
