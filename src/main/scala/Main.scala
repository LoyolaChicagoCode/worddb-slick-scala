import caseapp._

import scala.util.Failure

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
  @HelpMessage("finds substring in any words and lists matches")
  @ExtraName("w")
    findInWord: Option[String])
// format: ON

object Main extends CaseApp[Options] {

  private val logger = org.log4s.getLogger

  val DEFAULT_DBNAME = "word-db"

  override def run(options: Options, remainingArgs: RemainingArgs): Unit = {

    logger.info(options.toString)
    logger.info(remainingArgs.toString)

    val dbName = options match {
      case Options(Some(dbName), _, _, _, _, _, _, _) => dbName
      case _ => DEFAULT_DBNAME
    }

    logger.info(f"dbname = $dbName")

    val dao = new DAO(dbName)

    val result = options match {
      case Options(_, true, _, _, _, _, _, _) => dao.createDatabase()
      case Options(_, _, true, _, _, _, _, _) => dao.showWordCounts()
      case Options(_, _, _, Some(word), _, _, _, _) => dao.addWord(word)
      case Options(_, _, _, _, Some(word), _, _, _) => dao.deleteWord(word)
      case Options(_, _, _, _, _, Some(word), _, _) => dao.incWordCount(word)
      case Options(_, _, _, _, _, _, Some(word), _) => dao.decWordCount(word)
      case Options(_, _, _, _, _, _, _, Some(text)) => dao.findInWords(text)
      case _ => Failure(new IllegalArgumentException("more than one command given"))
    }

    logger.info(f"result = $result")
  }
}
