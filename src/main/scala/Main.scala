import cats.data.OptionT
import cats.implicits._
import mainargs.{ Flag, ParserForClass, TokensReader, arg, main }

import java.text.MessageFormat
import java.util.{ Locale, ResourceBundle }
import scala.util.{ Try, Using }

// DONE for each for-comprehension, add inline comment indicating corresponding command-line option

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

  // TODO refactor to avoid implicit
  implicit object PathRead extends TokensReader[os.Path](
    "path",
    strs => Right(os.Path(strs.head, os.pwd)))
  val options = ParserForClass[Options].constructOrExit(args.toIndexedSeq)
  logger.info(args.toString)
  logger.info(options.toString)

  val dbPath = options.database
  logger.info(f"dbPath = $dbPath")

  // Externalized resource bundle in src/main/resources.
  val bundle = ResourceBundle.getBundle("messages", Locale.US)
  logger.debug("loaded resource bundle")

  // compute the number of options present to check mutual exclusion, not counting database path
  val numOptions = -1 + productToMap(options).count {
    case (_, None) => false
    case (_, Flag(false)) => false
    case _ => true
  }
  logger.debug(s"number of options besides database: ${numOptions}")

  numOptions match {
    case 0 => printMessageFormat("noCommand")
    case 1 =>
      // --create-database/-f
      for {
        true <- OptionT.fromOption[Try](options.createDatabase.toOption)
        () <- withDAO { dao => dao.createDatabase() }
      } yield printMessageFormat("created")

      // alternative to above using Try without OptionT:
      //      for {
      //        () <- Try(())
      //        if options.createDatabase.isDefined
      //        () <- withDAO { dao => dao.createDatabase() }
      //      } yield printMessageFormat("created")

      // --show-word-counts/-s
      for {
        true <- OptionT.fromOption[Try](options.showWordCounts.toOption)
        counts <- withDAO { dao => dao.showWordCounts() }
      } yield counts match {
        case Seq() => printMessageFormat("noWordCounts")
        case rows => rows.foreach(row => println(row._1 + " -> " + row._2))
      }

      // --add-word/-a word
      // with for-comprehension *and* OptionT: can mix Try and Option sequentially without nesting
      for {
        word <- OptionT.fromOption[Try](options.addWord)
        () <- withDAO { dao => dao.addWord(word) }
      } yield printMessageFormat("added", word)

      // alternative to above without for-comprehension: deep nesting
      //      options.addWord.foreach { word =>
      //        withDAO { dao =>
      //          dao.addWord(word).map(_ => printMessageFormat("added", word))
      //        }
      //      }

      // --delete-word/-x word
      for {
        word <- OptionT.fromOption[Try](options.deleteWord)
        () <- withDAO { dao => dao.deleteWord(word) }
      } yield printMessageFormat("deleted", word)

      // alternative to above without OptionT:
      //      for {
      //        () <- Try(())
      //        if options.deleteWord.isDefined
      //        word = options.deleteWord.get
      //        () <- withDAO { dao => dao.deleteWord(word) } // requires simplified return type for withDAO
      //      } yield printMessageFormat("deleted", word)

      // --inc-word-count/-i word
      for {
        word <- OptionT.fromOption[Try](options.incWordCount)
        count <- withDAO { dao => dao.incWordCount(word) }
      } yield printMessageFormat("incremented", word, count)

      // --dec-word-count/-d word
      for {
        word <- OptionT.fromOption[Try](options.decWordCount)
        count <- withDAO { dao => dao.decWordCount(word) }
      } yield count match {
        case 0 => printMessageFormat("deleted", word)
        case count => printMessageFormat("decremented", word, count)
      }

      // --find-in-words/--w word
      for {
        _ <- options.findInWords
      } yield printMessageFormat("nyi")
    case _ => printMessageFormat("multipleCommands")
  }

  def productToMap(cc: Product) = cc.productElementNames.zip(cc.productIterator).toMap

  def printMessageFormat(key: String, args: Any*): Unit = {
    val form = new MessageFormat(bundle.getString(key))
    println(form.format(args.toArray))
  }

  def withDAO[R](action: DAO => Try[R]): OptionT[Try, R] = OptionT.liftF[Try, R] {
    Using.resource(new DAOImpl(dbPath.toString)) { dao =>
      val result = action(dao)
      if (result.isFailure) printMessageFormat("failed")
      result
    }
  }

  // conversion of Flag to Option to support for comprehensions
  implicit class FlagToOption(val self: Flag) {
    val toOption: Option[Boolean] = if (self.value) Some(true) else None
  }
}
