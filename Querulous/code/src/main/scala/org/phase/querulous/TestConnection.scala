package org.phase.querulous

import com.twitter.querulous.evaluator.QueryEvaluator

trait TestConnection {
  val queryEvaluator = QueryEvaluator("org.sqlite.JDBC", "jdbc:sqlite:/Users/jallen/sandbox/sqlite/phase", "", "")
}
