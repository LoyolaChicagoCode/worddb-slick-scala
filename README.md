# Overview

`word-db` is a simple command-line application for keeping word counts in a local database.

It is mainly intended to serve as an example of a Scala application with the following features:

- library-CLI architecture
- command-line option parsing
- persistence using an embedded database such as SQLite
- logging
- unit and integration testing
- idiomatic handling of database interactions including failure and streaming

# Description

```
<install-dir>/bin/worddb-slick-scala --help
word-db
a simple command-line application for keeping word counts in a local database
  -f --database <path>       name of database
  -c --create-database       create database
  -s --show-word-counts      shows all words with their counts
  -a --add-word <str>        adds a word to the database of words with count 0
  -x --delete-word <str>     deletes word if present
  -i --inc-word-count <str>  increments word count
  -d --dec-word-count <str>  decrements word count
  -w --find-in-words <str>   finds substring in any words and lists matches (NYI)
```

# Requirements

- sbt 1.x (for building)
- Java 11 or newer  (for building and deploying)

# Distribution

The task

```sbt universal:packageBin```

creates a universal zip file

```target/universal/worddb-slick-scala-<version>.zip```

that can be distributed and deployed by unzipping it in a suitable directory. 

To run, invoke

```<install-dir>/bin/worddb-slick-scala```

using the options listed above.

To create other distribution formats, follow the [sbt-native-packager documentation](https://www.scala-sbt.org/sbt-native-packager/gettingstarted.html#packaging-formats).
