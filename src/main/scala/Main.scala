import mainargs.{ Flag, ParserForClass, TokensReader, arg, main }

import java.text.MessageFormat
import java.util.{ Locale, ResourceBundle }
import scala.util.{ Try, Using }

object Main extends App {

  private val logger = org.log4s.getLogger

  val DEFAULT_DBNAME = "word-db.db"

  // format: OFF
  @main(name = "word-db", doc = "a simple command-line application for keeping word counts in a local database")
  case class Options(
    @arg(name = "database", short = 'f', doc = "name of database")
      database: os.Path = os.pwd / DEFAULT_DBNAME,
    @arg(name = "create-database", short = 'c', doc = "create database")
      createDatabase: Flag,
    @arg(name = "show-word-counts", short = 's', doc = "shows all words with their counts")
      showWordCounts: Flag,
    @arg(name = "add-word", short = 'a', doc = "adds a word to the database of words with count 0")
      addWord: Option[String],
    @arg(name = "delete-word", short = 'x', doc = "deletes word if present")
      deleteWord: Option[String],
    @arg(name = "inc-word-count", short = 'i', doc = "increments word count")
      incWordCount: Option[String],
    @arg(name = "dec-word-count", short = 'd', doc = "decrements word count")
      decWordCount: Option[String],
    @arg(name = "find-in-words", short = 'w', doc = "finds substring in any words and lists matches (NYI)")
      findInWords: Option[String]
  )
  // format: ON

  implicit object PathRead extends TokensReader[os.Path](
    "path",
    strs => Right(os.Path(strs.head, os.pwd)))

  val options = ParserForClass[Options].constructOrExit(args)

  logger.info(args.toString)
  logger.info(options.toString)

  val dbPath = options.database
  logger.info(f"dbPath = $dbPath")

  // WONTFIX figure out how to go from a positional to a named representation of actual options

  // Externalized resource bundle in src/main/resources.
  val bundle = ResourceBundle.getBundle("messages", Locale.US)

  def injectDAO(action: DAO => Try[Unit]): Unit = Using.resource(new DAOImpl(dbPath.toString)) { dao =>
    action(dao).fold(_ => printMessageFormat("failed"), _ => ())
  }

  def printMessageFormat(key: String, args: Any*): Unit = {
    val form = new MessageFormat(bundle.getString(key))
    println(form.format(args.toArray))
  }

  options match {
    case Options(_, Flag(false), Flag(false), None, None, None, None, None) =>
      printMessageFormat("noCommand")
    case Options(_, Flag(true), Flag(false), None, None, None, None, None) => injectDAO { dao =>
      dao.createDatabase().map(_ => printMessageFormat("created"))
    }
    case Options(_, Flag(false), Flag(true), None, None, None, None, None) => injectDAO { dao =>
      dao.showWordCounts().map {
        case Seq() => printMessageFormat("noWordCounts")
        case rows => rows.foreach(row => println(row._1 + " -> " + row._2))
      }
    }
    case Options(_, Flag(false), Flag(false), Some(word), None, None, None, None) => injectDAO { dao =>
      dao.addWord(word).map(_ => printMessageFormat("added", word))
    }
    case Options(_, Flag(false), Flag(false), None, Some(word), None, None, None) => injectDAO { dao =>
      dao.deleteWord(word).map(_ => printMessageFormat("deleted", word))
    }
    case Options(_, Flag(false), Flag(false), None, None, Some(word), None, None) => injectDAO { dao =>
      dao.incWordCount(word).map(count => printMessageFormat("incremented", word, count))
    }
    case Options(_, Flag(false), Flag(false), None, None, None, Some(word), None) => injectDAO { dao =>
      dao.decWordCount(word).map {
        case 0 => printMessageFormat("deleted", word)
        case count => printMessageFormat("decremented", word, count)
      }
    }
    case Options(_, Flag(false), Flag(false), None, None, None, None, Some(text)) => ???
    //q(_.findInWords(text))
    case _ =>
      printMessageFormat("multipleCommands")
  }
}
