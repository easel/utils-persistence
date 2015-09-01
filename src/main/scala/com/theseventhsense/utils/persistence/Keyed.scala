package com.theseventhsense.utils.persistence

trait Keyed {
  def key: String
}

object Keyed {
  implicit def ordering[T <: Keyed]: Ordering[T] = new Ordering[T] {
    override def compare(x: T, y: T): Int = {
      x.key.compareTo(y.key)
    }
  }
}
