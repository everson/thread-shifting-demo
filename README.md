# Scala Cats IO vs Scalaz Task example

## Description

Minimal example to show the different thread shifting happening in a request on Cats Effect 3 IO and Scalaz 7.2 Task.

Run it via: `sbt run`
Test by running: `curl localhost:8080/cats-io` and `curl localhost:8080/scalaz-task`. Look at thread names in the logs.




