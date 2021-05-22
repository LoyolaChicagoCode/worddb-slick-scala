# word-db

## Project metrics

  - In-process  
    [![Scala CI](https://img.shields.io/github/workflow/status/LoyolaChicagoCode/worddb-slick-scala/Scala%20CI)](https://github.com/LoyolaChicagoCode/worddb-slick-scala/actions)
    [![codecov](https://img.shields.io/codecov/c/github/LoyolaChicagoCode/worddb-slick-scala)](https://codecov.io/gh/LoyolaChicagoCode/worddb-slick-scala)
    ![Commit Activity](https://img.shields.io/github/commit-activity/m/LoyolaChicagoCode/worddb-slick-scala)
    [![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/LoyolaChicagoCode/worddb-slick-scala.svg)](http://isitmaintained.com/project/LoyolaChicagoCode/worddb-slick-scala "Average time to resolve an issue")
    [![Percentage of issues still open](http://isitmaintained.com/badge/open/LoyolaChicagoCode/worddb-slick-scala.svg)](http://isitmaintained.com/project/LoyolaChicagoCode/worddb-slick-scala "Percentage of issues still open")
  
  - Complexity  
    ![Code Size](https://img.shields.io/github/languages/code-size/LoyolaChicagoCode/worddb-slick-scala)
    [![Codacy Badge](https://img.shields.io/codacy/grade/20f5854f50c94a448968683ad33a687f)](https://www.codacy.com/gh/LoyolaChicagoCode/worddb-slick-scala/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=LoyolaChicagoCode/worddb-slick-scala&amp;utm_campaign=Badge_Grade)
    [![Maintainability](https://img.shields.io/codeclimate/maintainability/LoyolaChicagoCode/worddb-slick-scala)](https://codeclimate.com/github/LoyolaChicagoCode/worddb-slick-scala/maintainability)
    [![Technical Debt](https://img.shields.io/codeclimate/tech-debt/LoyolaChicagoCode/worddb-slick-scala)](https://codeclimate.com/github/LoyolaChicagoCode/worddb-slick-scala/trends/technical_debt)
    [![CodeFactor](https://img.shields.io/codefactor/grade/github/LoyolaChicagoCode/worddb-slick-scala)](https://www.codefactor.io/repository/github/LoyolaChicagoCode/worddb-slick-scala)
 
  - Other  
    [![License](http://img.shields.io/:license-mit-blue.svg)](http://doge.mit-license.org)

## Learning objectives

`word-db` is a simple command-line application for keeping word counts in a local database.

It is mainly intended to serve as an example of a Scala application with the following features:

- library-CLI architecture
- command-line option parsing
- persistence using an embedded database such as SQLite
- logging
- unit and integration testing
- idiomatic handling of database interactions including failure and streaming

## Description

```default
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

## Requirements

- sbt 1.x (for building)
- Java 11 or newer  (for building and deploying)

## Distribution

The task

```sbt universal:packageBin```

creates a universal zip file

```target/universal/worddb-slick-scala-<version>.zip```

that can be distributed and deployed by unzipping it in a suitable directory. 

To run, invoke

```<install-dir>/bin/worddb-slick-scala```

using the options listed above.

To create other distribution formats, follow the [sbt-native-packager documentation](https://www.scala-sbt.org/sbt-native-packager/gettingstarted.html#packaging-formats).
