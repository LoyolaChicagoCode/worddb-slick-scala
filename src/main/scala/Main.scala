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

  val DEFAULT_DBNAME = "word-db.db"

  override def run(options: Options, remainingArgs: RemainingArgs): Unit = {

    logger.info(options.toString)
    logger.info(remainingArgs.toString)

    val dbPath = options match {
      case Options(Some(dbName), _, _, _, _, _, _, _) => dbName
      case _ => DEFAULT_DBNAME
    }
    logger.info(f"dbname = $dbPath")

    // TODO use Using with resource
    val dao = new DAO("default")

    // TODO figure out how to go from a positional to a named representation of actual options
    // val optionsMap = options.productElementNames.zip(options.productIterator).filter { case (_, None) => false case _ => true }.toMap

    // format: OFF
    val result = options match {
      case Options(_, true,  false, None, None, None, None, None)       => dao.createDatabase()
      case Options(_, false, true,  None, None, None, None, None)       => dao.showWordCounts()
      case Options(_, false, false, Some(word), None, None, None, None) => dao.addWord(word)
      case Options(_, false, false, None, Some(word), None, None, None) => dao.deleteWord(word)
      case Options(_, false, false, None, None, Some(word), None, None) => dao.incWordCount(word)
      case Options(_, false, false, None, None, None, Some(word), None) => dao.decWordCount(word)
      case Options(_, false, false, None, None, None, None, Some(text)) => dao.findInWords(text)
      case _ => Failure(new IllegalArgumentException("more than one command given"))
    }
    // format: ON

    dao.close()

    // TODO user-facing output
    logger.info(f"result = $result")
  }
}
