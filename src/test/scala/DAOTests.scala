class DAOTests extends munit.FunSuite {

  daos.test("Database creation succeeds") { _ => }

  daos.test("Database creation succeeds again") { _ => }

  daos.test("Database is empty after creation") { dao =>
    assert {
      dao.showWordCounts().filter(_.isEmpty).isSuccess
    }
  }

  daos.test("Database has one row after insertion") { dao =>
    assertSuccess {
      for {
        () <- dao.addWord("hello")
        Seq((_, 1)) <- dao.showWordCounts()
      } yield ()
    }
  }

  daos.test("Database has one row after a separate insertion") { dao =>
    assertSuccess {
      for {
        () <- dao.addWord("hello")
        Seq((_, 1)) <- dao.showWordCounts()
      } yield ()
    }
  }

  daos.test("Database does not allow deleting nonexistent word") { dao =>
    assertFailure {
      dao.deleteWord("hello")
    }
  }

  daos.test("Database does not allow decrementing count of nonexistent word") { dao =>
    assertFailure {
      dao.decWordCount("hello")
    }
  }

  daos.test("Database does not allow incrementing count of nonexistent word") { dao =>
    assertFailure {
      dao.incWordCount("hello")
    }
  }

  daos.test("Database is not affected by adding and then deleting word") { dao =>
    val word = "hello"
    assertSuccess {
      for {
        () <- dao.addWord(word)
        Seq((`word`, 1)) <- dao.showWordCounts()
        () <- dao.deleteWord(word)
        Seq() <- dao.showWordCounts()
      } yield ()
    }
  }

  daos.test("Database reflects adding word and then incrementing its count") { dao =>
    val word = "hello"
    assertSuccess {
      for {
        () <- dao.addWord(word)
        Seq((`word`, 1)) <- dao.showWordCounts()
        () <- dao.incWordCount(word)
        Seq((`word`, 2)) <- dao.showWordCounts()
      } yield ()
    }
  }

  daos.test("Database has zero rows after deleting word with count > 1") { dao =>
    val word = "hello"
    assertSuccess {
      for {
        () <- dao.addWord(word)
        Seq((`word`, 1)) <- dao.showWordCounts()
        () <- dao.incWordCount(word)
        Seq((`word`, 2)) <- dao.showWordCounts()
        () <- dao.deleteWord(word)
        Seq() <- dao.showWordCounts()
      } yield ()
    }
  }

  daos.test("Database keeps word and decrements count when count > 1") { dao =>
    val word = "hello"
    assertSuccess {
      for {
        () <- dao.addWord(word)
        Seq((`word`, 1)) <- dao.showWordCounts()
        () <- dao.incWordCount(word)
        Seq((`word`, 2)) <- dao.showWordCounts()
        false <- dao.decWordCount(word)
        Seq((`word`, 1)) <- dao.showWordCounts()
      } yield ()
    }
  }

  daos.test("Database removes word when count reaches 0") { dao =>
    val word = "hello"
    assertSuccess {
      for {
        () <- dao.addWord(word)
        Seq((`word`, 1)) <- dao.showWordCounts()
        true <- dao.decWordCount(word)
        Seq() <- dao.showWordCounts()
      } yield ()
    }
  }

  lazy val daos = FunFixture[DAO](
    setup = { test =>
      val dao = new DAO("/tmp/DAOTests.db")
      assert(dao.createDatabase().isSuccess) // enforce this precondition for each test
      dao
    },
    teardown = { dao =>
      dao.clear()
      dao.close() // even if clear fails
    })

  // format: OFF
  def assertSuccess(
     block: => scala.util.Try[Any],
     clue: => Any = "operation failed"
   )(implicit loc: munit.Location): Unit = assert(block.isSuccess, clue)

  def assertFailure(
     block: => scala.util.Try[Any],
     clue: => Any = "operation succeeded but was expected to fail"
   )(implicit loc: munit.Location): Unit = assert(block.isFailure, clue)
}
