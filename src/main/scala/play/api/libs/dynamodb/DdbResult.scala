/**
 * Copyright (C) 2013-2014 MediaMath <http://www.mediamath.com>
 *
 * @author ihummel
 */
package play.api.libs.dynamodb

import play.api.libs.functional.{Alternative, Applicative}


sealed trait DdbResult[+A] {
  def map[X](f: A => X): DdbResult[X] = this match {
    case DdbSuccess(v) => DdbSuccess(f(v))
    case e: DdbError => e
  }

  def flatMap[X](f: A => DdbResult[X]): DdbResult[X] = this match {
    case DdbSuccess(v) => f(v)
    case e: DdbError => e
  }

  def foreach(f: A => Unit): Unit = this match {
    case DdbSuccess(a) => f(a)
    case _ => ()
  }

  def filter(p: A => Boolean): DdbResult[A] = this.flatMap { a => if (p(a)) DdbSuccess(a) else DdbError(Seq()) }

  def get: A

  def getOrElse[AA >: A](t: => AA): AA = this match {
    case DdbSuccess(a) => a
    case DdbError(_) => t
  }

  def orElse[AA >: A](t: => DdbError): DdbResult[AA] = this match {
    case s @ DdbSuccess(_) => s
    case DdbError(_) => t
  }

  def asOpt = this match {
    case DdbSuccess(v) => Some(v)
    case DdbError(_) => None
  }

  def asEither = this match {
    case DdbSuccess(v) => Right(v)
    case DdbError(e) => Left(e)
  }

  def recover[AA >: A](errManager: PartialFunction[DdbError, AA]): DdbResult[AA] = this match {
    case DdbSuccess(v) => DdbSuccess(v)
    case e: DdbError => if (errManager isDefinedAt e) DdbSuccess(errManager(e)) else this
  }

  def recoverTotal[AA >: A](errManager: DdbError => AA): AA = this match {
    case DdbSuccess(v) => v
    case e: DdbError => errManager(e)
  }
}

case class DdbSuccess[T](get: T) extends DdbResult[T]

case class DdbError(errors: Seq[String]) extends DdbResult[Nothing] {
  def get: Nothing = throw new NoSuchElementException("JsError.get")
}

object DdbResult {
  implicit val applicativeDdbResult: Applicative[DdbResult] = new Applicative[DdbResult] {
    def pure[A](a: A): DdbResult[A] = DdbSuccess(a)

    def map[A, B](m: DdbResult[A], f: A => B): DdbResult[B] = m.map(f)

    def apply[A, B](mf: DdbResult[A => B], ma: DdbResult[A]): DdbResult[B] = (mf, ma) match {
      case (DdbSuccess(f), DdbSuccess(a)) => DdbSuccess(f(a))
      case (DdbError(e1), DdbError(e2)) => DdbError((e1 ++ e2).distinct)
      case (DdbError(e), _) => DdbError(e)
      case (_, DdbError(e)) => DdbError(e)
    }
  }

  implicit def alternativeDdbResult(implicit a: Applicative[DdbResult]): Alternative[DdbResult] = new Alternative[DdbResult] {
    val app = a
    def |[A, B >: A](alt1: DdbResult[A], alt2: DdbResult[B]): DdbResult[B] = (alt1, alt2) match {
      case (DdbError(e), DdbSuccess(t)) => DdbSuccess(t)
      case (DdbSuccess(t), _) => DdbSuccess(t)
      case (DdbError(e1), DdbError(e2)) => DdbError(e1 ++ e2)
    }
    def empty: DdbResult[Nothing] = DdbError(Seq())
  }
}


