import DAO.Row
import caseapp._

import java.util.{ Locale, ResourceBundle }
import scala.util.{ Try, Using }

// format: OFF
@AppName("WordDB")
@AppVersion("0.1.0")
@ProgName("word-db")
case class Options(
  @HelpMessage("name of database")
  @ExtraName("f")
    database: Option[String],
  @HelpMessage("create database")
  @ExtraName("c")
    createDatabase: Boolean = false,
  @HelpMessage("shows all words with their counts")
  @ExtraName("s")
    showWordCounts: Boolean = false,
  @HelpMessage("adds a word to the database of words with count 0")
  @ExtraName("a")
    addWord: Option[String],
  @HelpMessage("deletes word if present")
  @ExtraName("x")
    deleteWord: Option[String],
  @HelpMessage("increments word count")
  @ExtraName("i")
    incWordCount: Option[String],
  @HelpMessage("decrements word count")
  @ExtraName("d")
    decWordCount: Option[String],
  @HelpMessage("finds substring in any words and lists matches (NYI)")
  @ExtraName("w")
    findInWords: Option[String])
// format: ON

object Main extends CaseApp[Options] {

  private val logger = org.log4s.getLogger

  val DEFAULT_DBNAME = "word-db.db"

  override def run(options: Options, remainingArgs: RemainingArgs): Unit = {

    logger.info(options.toString)
    logger.info(remainingArgs.toString)

    val dbPath = options match {
      case Options(Some(s), _, _, _, _, _, _, _) => s
      case _ => DEFAULT_DBNAME
    }
    logger.info(f"dbPath = $dbPath")

    // WONTFIX figure out how to go from a positional to a named representation of actual options
    // val optionsMap = options.productElementNames.zip(options.productIterator).filter { case (_, None) => false case _ => true }.toMap

    /** Externalized resource bundle in src/main/resources. */
    val bundle = ResourceBundle.getBundle("messages", Locale.US)

    /** Prints externalized message for given key. */
    def p[R](key: String): R => Unit = _ => println(bundle.getString(key))

    /** Invokes DAO command and, if successful, prints message for given key. */
    def c[R](command: DAO => Try[R], key: String): Unit = Using(new DAOImpl(dbPath)) { dao =>
      command(dao).fold(p("failed"), _ => p(key))
    }

    /** Invokes DAO command and, if successful, prints resulting word counts. */
    def q(command: DAO => Try[Seq[Row]]): Unit = Using(new DAOImpl(dbPath)) { dao =>
      command(dao).map(rows => rows.foreach(row => println(row._1 + " -> " + row._2)))
    }

    options match {
      case Options(_, false, false, None, None, None, None, None) =>
        p("noCommand")
      case Options(_, true, false, None, None, None, None, None) =>
        c(_.createDatabase(), "created")
      case Options(_, false, true, None, None, None, None, None) =>
        q(_.showWordCounts())
      case Options(_, false, false, Some(word), None, None, None, None) =>
        c(_.addWord(word), "added")
      case Options(_, false, false, None, Some(word), None, None, None) =>
        c(_.deleteWord(word), "deleted")
      case Options(_, false, false, None, None, Some(word), None, None) =>
        c(_.incWordCount(word), "incremented")
      case Options(_, false, false, None, None, None, Some(word), None) =>
        c(_.decWordCount(word), "decremented")
      case Options(_, false, false, None, None, None, None, Some(text)) =>
        q(_.findInWords(text))
      case _ =>
        p("multipleCommands")
    }
  }
}
