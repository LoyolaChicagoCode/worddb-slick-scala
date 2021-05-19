# Overview

`word-db` is a simple command-line application for keeping word counts in a local database.

It is mainly intended to serve as an example of a Scala application with the following features:

- library-CLI architecture
- command-line option parsing
- interaction with an embedded database such as SQLite   
- logging
- unit and integration testing

# Description

```
<install-dir>/bin/worddb-slick-scala --help
WordDB 0.1.0
Usage: word-db [options]
  --usage  <bool>
        Print usage and exit
  --help | -h  <bool>
        Print help message and exit
  --database | -f  <string?>
        name of database
  --create-database | -c  <bool>
        create database
  --show-word-counts | -s  <bool>
        shows all words with their counts
  --add-word | -a  <string?>
        adds a word to the database of words with count 0
  --delete-word | -x  <string?>
        deletes word if present
  --inc-word-count | -i  <string?>
        increments word count
  --dec-word-count | -d  <string?>
        decrements word count
  --find-in-words | -w  <string?>
        finds substring in any words and lists matches (NYI)
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
