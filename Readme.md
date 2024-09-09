Scalable minimum path sum calculator

This program computes the minimum path sum for a given triangle.

## Pre-requisites

Install a recent version of [scala](https://www.scala-lang.org) like 3.5 or [scala-cli](https://scala-cli.virtuslab.org).

## Run

```scala
scala run . --main-class run -- data_big.txt

```

Or, using scala-cli,

```scala
scala-cli run . --main-class run -- data_big.txt

```

## Test

```scala
scala test .

```

Or, using scala-cli,

```scala
scala-cli test .

```


## Running from the commandline

```scala

cat << EOF |  scala-cli run . --main-class cmd
7
6 3
3 8 5
11 2 10 9
EOF

```
