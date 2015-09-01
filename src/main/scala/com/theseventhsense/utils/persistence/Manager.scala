package com.theseventhsense.utils.persistence

import scala.concurrent.Future

trait Manager[T] {
  def find(meta: QueryMeta): Future[ListQueryResult[T]]
  def get(id: Long): Option[T]
  def delete(id: Long): Unit
  def create(obj: T): T
  def createBulk(tSeq: Seq[T]): Seq[T]
  def forceInsert(obj: T): T
  def forceInsertOrUpdate(obj: T): T
  def insertOrUpdate(obj: T): T
  def save(obj: T): T
  def saveBulk(tSeq: Seq[T]): Seq[T]
  def count: Long
}
