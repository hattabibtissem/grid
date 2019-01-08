package lib.elasticsearch.impls.elasticsearch6

import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.searches.queries.Query
import lib.querysyntax._

class QueryBuilder() {

  private def makeQueryBit(condition: Match): Query = {
    condition.field match {
      case SingleField(field) => condition.value match {
        case Phrase(value) => matchPhraseQuery(field, value)
        case e => throw InvalidQuery(s"Cannot do single field query on $e")
      }
      case _ => throw new RuntimeException("Not implemented")
    }
  }

  def makeQuery(conditions: List[Condition]): Query = conditions match {
    case Nil => matchAllQuery()
    case condList => {
      val (_, normal: List[Condition]) = (
        condList collect { case n: Nested => n },
        condList collect { case c: Condition => c }
      )

      val query = normal.foldLeft(boolQuery) {
        case (query, cond@Match(_, _)) => query.withMust(makeQueryBit(cond))
        case (query, _) => query
      }

      query
    }
  }

}
