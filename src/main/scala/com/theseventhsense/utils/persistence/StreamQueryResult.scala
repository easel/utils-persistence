package com.theseventhsense.utils.persistence

import akka.stream.scaladsl.Source

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

object StreamQueryResult {
  implicit def convert[A, B](
    a: StreamQueryResult[A]
  )(implicit converter: (A) => B): StreamQueryResult[B] = {
    val stream: Source[B, _] = a.stream.map { item =>
      converter(item)
    }
    StreamQueryResult[B](a.totalCount, stream)
  }

  implicit def convertFuture[A, B](
    a: Future[StreamQueryResult[A]]
  )(implicit
    converter: (A) => B,
    ec: ExecutionContext): Future[StreamQueryResult[B]] = {
    a.map { source: StreamQueryResult[A] =>
      val result: StreamQueryResult[B] = convert(source)
      result
    }
  }
}
case class StreamQueryResult[T](
  totalCount: Long,
  stream:     Source[T, _]
) extends QueryResult {
  def map[U](converter: (T) => U): StreamQueryResult[U] = {
    val s = stream.map(converter)
    StreamQueryResult(totalCount, s)
  }
}
