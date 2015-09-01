package com.theseventhsense.utils.persistence.db

import akka.stream.scaladsl.Source
import play.api.db.slick.{HasDatabaseConfig, HasDatabaseConfigProvider}
import slick.backend.DatabaseConfig
import slick.dbio.Effect.Write
import slick.driver.JdbcProfile
import com.theseventhsense.utils.logging.Logging
import com.theseventhsense.utils.persistence._
import slick.profile.FixedSqlAction

import scala.collection.immutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.implicitConversions

trait SlickDTO extends DTO {
  def id: SlickDTO.Id
}

object SlickDTO {
  type Id = Long
}

trait SlickTable extends HasDatabaseConfig[JdbcProfile] {
  import driver.api._

  protected trait IdentifiedTable[T <: SlickDTO] extends Table[T] {
    def id: Rep[SlickDTO.Id]
  }

  implicit val dateTimeColumnType = MappedColumnType.base[org.joda.time.DateTime, java.sql.Timestamp](
    { dt => new java.sql.Timestamp(dt.getMillis) },
    { ts => new org.joda.time.DateTime(ts) }
  )
}
object SlickDAO {

}

trait SlickDAO[T <: SlickDTO]
    extends DAO[T]
    with HasDatabaseConfigProvider[JdbcProfile]
    with SlickTable
    with Logging {
  import driver.api._

  implicit def block[A](fut: Future[A]): A = {
    Await.result(fut, 10.seconds)
  }

  type Items <: IdentifiedTable[T]
  def table: TableQuery[Items]

  def createTable(): Unit = {
    val createAction = table.schema.create
    logger.info(s"Creating table:\n${createAction.statements}")
    block(db.run(createAction))
    ()
  }

  def dropTable(): Unit = {
    block(db.run(table.schema.drop))
    ()
  }

  def count: Long = {
    db.run(table.length.result).map(_.toLong)
  }

  def get(id: Long): Option[T] = {
    val query = table.filter(_.id === id).result.headOption
    block(db.run(query))
  }

  def getOrCreate(item: T): T = {
    get(item.id).getOrElse(create(item))
  }

  def delete(id: Long): Int = {
    val action = table.filter(_.id === id).delete
    block(db.run(action))
  }

  // Commented out since the type bounds don't quite work out (yet). Descendant
  // classes will need a message such as this if you want to be able to insert
  // records and get back a copy with the newly set id in it.
  //  def create(obj: T): T = {
  //    db withTransaction { implicit session =>
  //      (table returning table.map(_.id)
  //        into ((item, id) => item.copy(id = id))) += obj
  //    }
  //  }

  def save(obj: T): T = {
    val q = table
      .filter(_.id === obj.id)
      .update(obj)
    db.run(q).map(x => obj)
  }

  override def forceInsert(obj: T): T = {
    val q = table.forceInsert(obj)
    db.run(q).map(x => obj)
  }

  override def insertOrUpdate(obj: T): T = {
    val q = table.insertOrUpdate(obj)
    db.run(q).map(x => obj)
  }

  override def forceInsertOrUpdate(obj: T): T = {
    db.run(table.filter(_.id === obj.id).length.result)
      .flatMap { count: Int =>
        if (count == 0) {
          db.run(table.forceInsert(obj))
        } else {
          db.run(table.insertOrUpdate(obj))
        }
      }.map(x => obj)
  }

  override def saveBulk(seq: Seq[T]): Seq[T] = {
    val q = seq map { obj =>
      db.run(table.filter(_.id === obj.id).update(obj)).map(x => obj)
    }
    Future.sequence(q)
  }

  def createFindResult(query: Query[Items, T, Seq], meta: QueryMeta): Future[ListQueryResult[T]] = {
    val totalFut: Future[Int] = db.run(query.length.result)
    val recordsFut = db.run(filterByMeta(query, meta).result)
    val listQuery = for {
      total: Int <- totalFut
      records <- recordsFut
    } yield ListQueryResult(total.toLong, records.toList)
    listQuery
  }

  def createCountResult(query: Query[Items, T, Seq], meta: QueryMeta): Future[Int] = {
    db.run(query.length.result)
  }

  def createDeleteResult(query: Query[Items, T, Seq]): Future[Int] = {
    db.run(query.delete)
  }

  def find(meta: QueryMeta): Future[ListQueryResult[T]] = {
    createFindResult(table, meta)
  }

  protected def filterByMeta(query: Query[Items, T, Seq], meta: QueryMeta) = {
    var q = query.drop(meta.offset)
    meta.limit.foreach(limit => q = q.take(limit))
    meta.sort match {
      case None =>
        if (meta.sortAsc) {
          q = q.sortBy(_.id)
        } else {
          q = q.sortBy(_.id.desc)
        }
      case Some(s: String) =>
        if (meta.sortAsc) {
          q = q.sortBy(_.id)
        } else {
          q = q.sortBy(_.id.desc)
        }
    }
    q
  }

  def createStreamResult(query: Query[Items, T, Seq], meta: QueryMeta): Future[StreamQueryResult[T]] = {
    val totalFut: Future[Int] = db.run(query.length.result)
    val records = db.stream(filterByMeta(query, meta).result)
    val streamQuery: Future[StreamQueryResult[T]] = for {
      total <- totalFut
    } yield StreamQueryResult(total.toLong, Source(records))
    streamQuery
  }

  def stream(meta: QueryMeta): Future[StreamQueryResult[T]] = {
    createStreamResult(table, meta)
  }

  def resetSequence(): Unit = {
    db.run(table.map(_.id).max.result).map { max =>
      if (max.isDefined) {
        val sequence = s"${table.baseTableRow.tableName}_id_seq"
        logger.debug(s"Resetting $sequence sequence to ${max.get}")
        db.run(sqlu"SELECT setval('$sequence', ${max.get});")
      }
    }
    ()
  }

  override def commit(): Unit = {}
}
