package com.theseventhsense.utils.persistence

trait ListRTO[T <: RTO] {
  val meta: QueryMeta
  val data: Seq[T]
}

