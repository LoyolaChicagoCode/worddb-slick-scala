import caseapp._

// format: OFF
@AppName("WordDB")
@AppVersion("0.1.0")
@ProgName("word-db")
case class Options(
  @HelpMessage("name of database")
  @ExtraName("f")
    database: Option[String],
  @HelpMessage("adds a word to the database of words with count 0")
  @ExtraName("a")
    addWord: Option[String],
  @HelpMessage("deletes word if present")
  @ExtraName("x")
    deleteWord: Option[String],
  @HelpMessage("increments word count")
  @ExtraName("i")
    incWordCount: Option[String],
  @HelpMessage("increments word count")
  @ExtraName("d")
    decWordCount: Option[String],
  @HelpMessage("shows all words with their counts")
  @ExtraName("s")
    showWordCounts: Boolean = false,
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
      case Options(Some(dbName), _, _, _, _, _, _) => dbName
      case _ => DEFAULT_DBNAME
    }

    logger.info(f"dbname = $dbName")

    val command = options match {
      case Options(_, Some(word), _, _, _, _, _) => "add " + word
      case Options(_, _, Some(word), _, _, _, _) => "delete " + word
      case _ => "unsupported command"
    }

    logger.info(f"command = $command")

    // TODO flesh out DAO and map commands to methods
    new DAO {}.doSomething()
  }
}
