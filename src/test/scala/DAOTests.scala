class DAOTests extends munit.FunSuite {

  daos.test("Database creation succeeds") {
    Function.const(())
  }

  daos.test("Database creation succeeds again") {
    Function.const(())
  }

  daos.test("Database has zero rows after creation") { dao =>
    assertSuccess {
      dao.showWordCounts().filter(_.isEmpty)
    }
  }

  daos.test("Database has one row after insertion") { dao =>
    assertSuccess {
      for {
        1 <- dao.addWord("hello")
        Seq((_, 1)) <- dao.showWordCounts()
        r = ()
      } yield r
    }
  }

  daos.test("Database has one row after a separate insertion") { dao =>
    assertSuccess {
      for {
        1 <- dao.addWord("hello")
        Seq((_, 1)) <- dao.showWordCounts()
        r = ()
      } yield r
    }
  }

  daos.test("Database is not affected by deleting nonexistent word") { dao =>
    assertSuccess {
      for {
        0 <- dao.deleteWord("hello")
        Seq() <- dao.showWordCounts()
        r = ()
      } yield r
    }
  }

  daos.test("Database is not affected by decrementing count of nonexistent word") { dao =>
    assertSuccess {
      for {
        0 <- dao.decWordCount("hello")
        Seq() <- dao.showWordCounts()
        r = ()
      } yield r
    }
  }

  daos.test("Database is not affected by incrementing count of nonexistent word") { dao =>
    assertSuccess {
      for {
        0 <- dao.incWordCount("hello")
        Seq() <- dao.showWordCounts()
        r = ()
      } yield r
    }
  }

  daos.test("Database is not affected by adding and then deleting word") { dao =>
    val word = "hello"
    assertSuccess {
      for {
        1 <- dao.addWord(word)
        Seq((`word`, 1)) <- dao.showWordCounts()
        1 <- dao.deleteWord(word)
        Seq() <- dao.showWordCounts()
        r = ()
      } yield r
    }
  }

  daos.test("Database reflects adding word and then incrementing its count") { dao =>
    val word = "hello"
    assertSuccess {
      for {
        1 <- dao.addWord(word)
        Seq((`word`, 1)) <- dao.showWordCounts()
        1 <- dao.incWordCount(word)
        Seq((`word`, 2)) <- dao.showWordCounts()
        r = ()
      } yield r
    }
  }

  daos.test("Database has zero rows after deleting word with count > 1") { dao =>
    val word = "hello"
    assertSuccess {
      for {
        1 <- dao.addWord(word)
        Seq((`word`, 1)) <- dao.showWordCounts()
        1 <- dao.incWordCount(word)
        Seq((`word`, 2)) <- dao.showWordCounts()
        1 <- dao.deleteWord(word)
        Seq() <- dao.showWordCounts()
        r = ()
      } yield r
    }
  }

  lazy val daos = FunFixture[DAO](
    setup = { test =>
      val dao = new DAO("memory")
      dao.createDatabase()
      dao
    },
    teardown = { dao => dao.clear() }
  )

  // format: OFF
  def assertSuccess(
     block: => scala.util.Try[Any],
     clue: => Any = "assertion failed"
   )(implicit loc: munit.Location): Unit = assert(block.isSuccess, clue)
}
