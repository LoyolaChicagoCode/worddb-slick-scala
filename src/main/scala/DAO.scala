import slick.jdbc.SQLiteProfile.api._
import slick.sql.SqlProfile.ColumnOption.SqlType

import java.util.concurrent.Executors
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext }
import scala.util.{ Try, Using }

// DONE second column for count
// DONE rewrite to avoid looking like premature optimization
// DONE switch to logging
// DONE architect as CRUD API (DAO) + CLI
// DONE try with sqlite3 -> works!
// DONE make DB methods DRY
// TODO update count
// TODO full-text search
// TODO factor out row type

class DAO(val dbName: String) {

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

  def createDatabase(): Try[Unit] = dbWrapper {
    DBIO.seq(words.schema.create)
  }

  def showWordCounts(): Try[Seq[(String, Int)]] = dbWrapper {
    words.result
  }

  def addWord(word: String): Try[Int] = dbWrapper {
    words += ((word, 0))
  }

  def deleteWord(word: String): Try[Int] = dbWrapper {
    words.filter(_.id === word).delete
  }

  def incrementWordCount(word: String): Try[Unit] = dbWrapper {
    ???
  }

  def decrementWordCount(word: String): Try[Unit] = dbWrapper {
    ???
  }

  def findInWords(text: String): Try[Seq[(String, Int)]] = dbWrapper {
    ???
  }

  protected def dbWrapper[R, S <: NoStream, E <: Effect](action: => DBIOAction[R, S, E]): Try[R] = {
    // Scala equivalent of try-with-resource for auto-closing db
    val result = Using(Database.forConfig("sqlite")) { db =>
      val f = for {
        r <- db.run(action)
        () = logger.info(f"completed action with result $r")
      } yield r
      logger.info("waiting for future to complete")
      Await.result(f, Duration.Inf)
    }
    // shut down non-daemon executor to allow main to complete
    executor.shutdown()
    logger.info("done")
    result
  }
}