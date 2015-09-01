package com.theseventhsense.utils.persistence

case class IteratorQuery[T](
  totalCount: Long,
  data:       Iterator[T]
)

