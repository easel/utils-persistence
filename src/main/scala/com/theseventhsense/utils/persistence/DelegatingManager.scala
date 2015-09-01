package com.theseventhsense.utils.persistence

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

trait DelegatingManager[A <: DomainObject, B <: DTO] extends Manager[A] {
  implicit def from(obj: A): B

  implicit def asDomain(dto: B): A

  implicit def fromListQuery(l: ListQueryResult[B]): ListQueryResult[A] = {
    val data = l.data.map(asDomain)
    ListQueryResult(l.totalCount, data)
  }

  implicit def fromOptionDTO(o: Option[B]): Option[A] = {
    o.map(asDomain)
  }

  def delegate: DAO[B]

  def find(meta: QueryMeta): Future[ListQueryResult[A]] = {
    ListQueryResult.convertFuture(delegate.find(meta))
  }

  def get(id: Long): Option[A] = {
    delegate.get(id).map(asDomain)
  }

  def delete(id: Long): Unit = {
    delegate.delete(id)
    delegate.commit()
  }

  def create(obj: A): A = {
    val created = delegate.create(obj)
    delegate.commit()
    created
  }

  def createBulk(tSeq: Seq[A]): Seq[A] = {
    val results = delegate.createBulk(tSeq.map(from))
    results.map(asDomain)
  }

  def forceInsert(obj: A): A = {
    delegate.forceInsert(obj)
  }

  def insertOrUpdate(obj: A): A = {
    delegate.insertOrUpdate(obj)
  }

  def forceInsertOrUpdate(obj: A): A = {
    delegate.forceInsertOrUpdate(obj)
  }

  def save(obj: A): A = {
    val result = delegate.save(obj)
    delegate.commit()
    result
  }

  def saveBulk(tSeq: Seq[A]): Seq[A] = {
    val results = delegate.saveBulk(tSeq.map(from))
    results.map(asDomain)
  }

  def count: Long = {
    delegate.count
  }
}
