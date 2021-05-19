import caseapp._

import java.text.MessageFormat
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

    // Externalized resource bundle in src/main/resources.
    val bundle = ResourceBundle.getBundle("messages", Locale.US)

    def injectDAO(action: DAO => Try[Unit]): Unit = Using.resource(new DAOImpl(dbPath)) { dao =>
      action(dao).fold(_ => printMessageFormat("failed"), _ => ())
    }

    def printMessageFormat(key: String, args: Any*): Unit = {
      val form = new MessageFormat(bundle.getString(key))
      println(form.format(args.toArray))
    }

    options match {
      case Options(_, false, false, None, None, None, None, None) =>
        printMessageFormat("noCommand")
      case Options(_, true, false, None, None, None, None, None) => injectDAO { dao =>
        dao.createDatabase().map(_ => printMessageFormat("created"))
      }
      case Options(_, false, true, None, None, None, None, None) => injectDAO { dao =>
        dao.showWordCounts().map {
          case Seq() => printMessageFormat("noWordCounts")
          case rows => rows.foreach(row => println(row._1 + " -> " + row._2))
        }
      }
      case Options(_, false, false, Some(word), None, None, None, None) => injectDAO { dao =>
        dao.addWord(word).map(_ => printMessageFormat("added", word))
      }
      case Options(_, false, false, None, Some(word), None, None, None) => injectDAO { dao =>
        dao.deleteWord(word).map(_ => printMessageFormat("deleted", word))
      }
      case Options(_, false, false, None, None, Some(word), None, None) => injectDAO { dao =>
        dao.incWordCount(word).map(count => printMessageFormat("incremented", word, count))
      }
      case Options(_, false, false, None, None, None, Some(word), None) => injectDAO { dao =>
        dao.decWordCount(word).map {
          case 0 => printMessageFormat("deleted", word)
          case count => printMessageFormat("decremented", word, count)
        }
      }
      case Options(_, false, false, None, None, None, None, Some(text)) => ???
      //q(_.findInWords(text))
      case _ =>
        printMessageFormat("multipleCommands")
    }
  }
}
