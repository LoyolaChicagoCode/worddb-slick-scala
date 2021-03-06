import DAO.Row
import slick.jdbc.SQLiteProfile.api._
import slick.sql.SqlProfile.ColumnOption.SqlType

import java.util.concurrent.Executors
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.util.Try

// DONE second column for count
// DONE rewrite to avoid looking like premature optimization
// DONE switch to logging
// DONE architect as CRUD API (DAO) + CLI
// DONE try with sqlite3 -> works!
// DONE make DB methods DRY
// DONE update count
// DONE programmatically set DB name
// DONE factor out row type
// DONE review decWordCount
// DONE keep row count from leaking outside DAO - use Success vs. Failure - update tests accordingly
// DONE made more sequential by using for comprehensions

class DAOImpl(val dbPath: String = "default") extends DAO {

  private val logger = org.log4s.getLogger

  // set up execution context for futures based on non-daemon executor
  private val executor = Executors.newSingleThreadExecutor()
  private implicit val context = ExecutionContext.fromExecutor(executor)

  class Words(tag: Tag) extends Table[Row](tag, "WORDS") {
    def id = column[String]("WORD", O.PrimaryKey, SqlType("TEXT"))

    def count = column[Long]("COUNT")

    override def * = (id, count)
  }

  private val words = TableQuery[Words]

  private val db = Database.forURL(f"jdbc:sqlite:$dbPath")

  logger.debug(db.toString)

  override def createDatabase(): Try[Unit] = withDB {
    DBIO.seq(words.schema.create)
  }

  override def showWordCounts(): Try[Seq[Row]] = withDB {
    // TODO db.stream(words.result)
    words.result
  }

  // before: higher-order functions
  //  override def addWord(word: String): Try[Unit] = withDB {
  //    words += ((word, 1))
  //  } filter { _ == 1 } map unit

  // with for comprehension: sequential, probably easier to read
  override def addWord(word: String): Try[Unit] = for {
    numRows <- withDB { words += ((word, 1)) }
    if numRows == 1
  } yield ()

  override def deleteWord(word: String): Try[Unit] = for {
    numRows <- withDB { words.filter(_.id === word).delete }
    if numRows == 1
  } yield ()

  protected def currentWordCount(word: String) =
    words.filter(_.id === word).map(_.count).result.head

  override def incWordCount(word: String): Try[Long] = withDB {
    (for {
      numRows <- sqlu"UPDATE words SET count = count + 1 WHERE word = $word"
      count <- currentWordCount(word)
    } yield count).transactionally
  }

  override def decWordCount(word: String): Try[Long] = withDB {
    (for {
      numRows1 <- sqlu"UPDATE words SET count = count - 1 WHERE word = $word AND count > 0" // decrement
      count <- currentWordCount(word)
      numRows2 <- sqlu"DELETE FROM words WHERE word = $word AND count = 0" // remove row if count reaches 0
    } yield count).transactionally
  }

  override def findInWords(text: String): Try[Seq[Row]] = withDB {
    ???
  }

  override def clear(): Try[Unit] = for {
    numRows <- withDB { sqlu"DROP TABLE words" }
    if numRows > 0
  } yield ()

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
  protected def withDB[R, S <: NoStream, E <: Effect](action: => DBIOAction[R, S, E]): Try[R] = {
    logger.debug(f"operating on $db")

    val act = action
    val f = for {
      // start by pointing for comprehension to desired monad, i.e., Future
      // <- is a monadic binding, = a val binding
      // using () unit pattern on left instead of _ or unused variable
      () <- Future.unit
      () = logger.debug(f"attempting action $act")
      // perform action on database
      r <- db.run(act)
      () = logger.debug(f"completed action $act with result $r")
    } yield r

    // join background activity
    logger.debug("waiting for future to complete")
    val result = Await.ready(f, Duration.Inf)
    logger.debug(f"returning result $result")
    result.value.get
  }

  protected def dbStreamingWrapper[R, S <: NoStream, E <: Effect](action: => DBIOAction[R, S, E]): Try[R] = {
    ???
  }

  override def close(): Unit = {
    // shut down non-daemon executor to allow main to complete
    executor.shutdown()
  }
}
