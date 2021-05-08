import caseapp._
import slick.jdbc.H2Profile.api._

import java.util.concurrent.Executors
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.Duration
import scala.util.Using

// --database name of database
// --add-word word  adds a word to the database of words with count 0
// --delete-word word deletes word, if present
// --inc-word-count word increments
// --dec-word-count word decrements
// --show-word-counts
// --find-in-word substring

case class Options(
  @ExtraName("f") database: Option[String],
  @ExtraName("a") addWord: Option[String],
  @ExtraName("x") deleteWord: Option[String],
  @ExtraName("i") incWordCount: Option[String],
  @ExtraName("d") decWordCount: Option[String],
  @ExtraName("s") showWordCounts: Boolean = false,
  @ExtraName("w") findInWord: Option[String])

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
