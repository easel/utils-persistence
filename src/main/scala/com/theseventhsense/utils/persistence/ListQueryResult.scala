package com.theseventhsense.utils.persistence

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

object ListQueryResult {
  implicit def convert[A, B](
    a: ListQueryResult[A]
  )(implicit converter: (A) => B): ListQueryResult[B] = {
    val data: List[B] = a.data.map { item =>
      converter(item)
    }
    ListQueryResult[B](a.totalCount, data)
  }

  implicit def convertFuture[A, B](
    a: Future[ListQueryResult[A]]
  )(implicit
    converter: (A) => B,
    ec: ExecutionContext): Future[ListQueryResult[B]] = {
    a.map { source: ListQueryResult[A] =>
      val result: ListQueryResult[B] = convert(source)
      result
    }
  }
}
case class ListQueryResult[T](
  totalCount: Long,
  data:       List[T]
) extends QueryResult {
  def map[U](converter: (T) => U): ListQueryResult[U] = {
    val s = data.map(converter)
    ListQueryResult(totalCount, s)
  }
}
