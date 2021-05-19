import DAO.Row

import scala.util.Try

object DAO {
  type Row = (String, Long)
}

trait DAO extends AutoCloseable {

  /** Creates the database schema. */
  def createDatabase(): Try[Unit]

  /** Returns the words and their counts. */
  def showWordCounts(): Try[Seq[Row]]

  /** Adds the word with initial count of 1. */
  def addWord(word: String): Try[Unit]

  /** Deletes the word regardless of count. */
  def deleteWord(word: String): Try[Unit]

  /** Increments and returns the count for the given word. */
  def incWordCount(word: String): Try[Long]

  /** Decrements and returns the count for the given word, and deletes the word if zero was reached. */
  def decWordCount(word: String): Try[Long]

  /** Finds a substring with the words. */
  def findInWords(text: String): Try[Seq[Row]]

  /** Removes all data. */
  def clear(): Try[Unit]
}
