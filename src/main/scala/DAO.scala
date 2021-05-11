import slick.jdbc.SQLiteProfile.api._
import slick.sql.SqlProfile.ColumnOption.SqlType

import java.util.concurrent.Executors
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Try, Using}

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

  /**
   * Performs a database action in a future and with logging.
   * This wrapper method keeps the public DAO methods very DRY.
   * The main challenge was to figure out the correct type parameters including upper bounds.
   * @param action the database action
   * @tparam R result type of the database action
   * @tparam S item type of the stream, if any
   * @tparam E effect type
   * @return result of the action, with the possibility of failure
   */
  protected def dbWrapper[R, S <: NoStream, E <: Effect](action: => DBIOAction[R, S, E]): Try[R] = {
    // set up execution context for futures based on non-daemon executor
    val executor = Executors.newSingleThreadExecutor()
    implicit val context = ExecutionContext.fromExecutor(executor)

    // Scala equivalent of try-with-resource for auto-closing db
    val result = Using(Database.forConfig("sqlite")) { db =>
      val f = for {
        // start by pointing for comprehension to desired monad, i.e., Future
        // <- is a monadic binding, = a val binding
        // using () unit pattern on left instead of _ or unused variable
        () <- Future.unit
        () = logger.debug(f"attempting action")
        // perform action on database
        r <- db.run(action)
        () = logger.debug(f"completed action with result $r")
      } yield r

      // join background activity
      logger.debug("waiting for future to complete")
      Await.result(f, Duration.Inf)
    }

    // shut down non-daemon executor to allow main to complete
    executor.shutdown()
    logger.debug(f"returning result $result")
    result
  }
}
