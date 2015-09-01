package com.theseventhsense.utils.persistence
import scala.language.implicitConversions

object DTO {
  implicit def convertOption[A, B](opt: Option[A])(implicit convert: (A) => B): Option[B] = {
    val b: Option[B] = opt.map { a: A =>
      convert(a)
    }
    b
  }
}
trait DTO {
}

