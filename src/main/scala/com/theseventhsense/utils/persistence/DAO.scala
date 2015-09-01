package com.theseventhsense.utils.persistence

import com.theseventhsense.utils.logging.Logging

import scala.concurrent.Future

trait DAO[T <: DTO] extends Logging {
  def find(meta: QueryMeta): Future[ListQueryResult[T]]

  def get(id: Long): Option[T]

  def delete(id: Long): Int

  def save(obj: T): T

  def count: Long

  def create(obj: T): T

  def createBulk(seq: Seq[T]): Seq[T] = {
    seq.map(create)
  }

  def forceInsert(obj: T): T = {
    save(obj)
  }

  def insertOrUpdate(obj: T): T = {
    save(obj)
  }

  def forceInsertOrUpdate(obj: T): T = {
    save(obj)
  }

  def saveBulk(seq: Seq[T]): Seq[T] = {
    seq.map(save)
  }

  def commit(): Unit = {}
}
