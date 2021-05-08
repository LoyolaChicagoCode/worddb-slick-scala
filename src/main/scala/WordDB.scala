import caseapp._
import slick.jdbc.H2Profile.api._

import java.util.concurrent.Executors
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.Duration
import scala.util.Using

@AppName("WordDB")
@AppVersion("0.1.0")
@ProgName("word-db")
case class Options(
  @HelpMessage("name of database")@ExtraName("f") database: Option[String],
  @HelpMessage("adds a word to the database of words with count 0")@ExtraName("a") addWord: Option[String],
  @HelpMessage("deletes word if present")@ExtraName("x") deleteWord: Option[String],
  @HelpMessage("increments word count")@ExtraName("i") incWordCount: Option[String],
  @HelpMessage("increments word count")@ExtraName("d") decWordCount: Option[String],
  @HelpMessage("shows all words with their counts")@ExtraName("s") showWordCounts: Boolean = false,
  @HelpMessage("finds substring in any words and lists matches")@ExtraName("w") findInWord: Option[String])

object WordDB extends CaseApp[Options] {

  private val logger = org.log4s.getLogger

  // set up EC based on non-daemon executor
  val executor = Executors.newSingleThreadExecutor()
  // define context used by futures
  implicit val context = ExecutionContext.fromExecutor(executor)

  class Words(tag: Tag) extends Table[(String, Int)](tag, "WORDS") {
    def id = column[String]("WORD", O.PrimaryKey)
    def count = column[Int]("COUNT")
    override def * = (id, count)
  }

  override def run(options: Options, remainingArgs: RemainingArgs): Unit = {

    logger.info(options.toString)
    logger.info(remainingArgs.toString)

    // Scala equivalent of try-with-resource for auto-closing db
    val count = Using(Database.forConfig("h2file1")) { db =>
      val f = for {
        () <- Future.unit // point for comprehension to the right monad

        // create table(s)
        words = TableQuery[Words]
        setup = DBIO.seq(words.schema.create)
        () <- db.run(setup)
        () = logger.info("created schema")

        // insert a few rows
        insert = words ++= Seq("hello", "world", "what", "up").zip(Iterator.continually(0))
        Some(count) <- db.run(insert)
        () = logger.info(f"inserted $count items")

        // DONE second column for count
        // DONE rewrite to avoid looking like premature optimization
        // DONE switch to logging
        // TODO update count
        // TODO full-text search
        // TODO try with sqlite3
        // TODO architect as CRUD API (DAO) + CLI

        // perform query
        () <- db.run(words.result).map(_.foreach(w => logger.info(w.toString)))

      } yield count

      logger.info("waiting")
      Await.result(f, Duration.Inf)
    }

    logger.info(f"result: $count")
    // shut down non-daemon executor to allow main to complete
    executor.shutdown()
    logger.info("done")
  }
}
