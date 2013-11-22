/**
 * Copyright (C) 2013-2014 MediaMath <http://www.mediamath.com>
 *
 * @author ihummel
 */
package play.api.libs.dynamodb

import scala._
import play.api.libs.functional.{Functor, Alternative, Applicative}












trait Reads[A] { self =>
  def reads(value: DdbValue): DdbResult[A]

  def map[B](f: A => B): Reads[B] = Reads[B] { value => self.reads(value).map(f) }

  def flatMap[B](f: A => Reads[B]): Reads[B] = Reads[B] { value => self.reads(value).flatMap(t => f(t).reads(value)) }

  def filter(f: A => Boolean): Reads[A] =	Reads[A] { item => self.reads(item).filter(f) }
}

object Reads {
  def apply[A](f: DdbValue => DdbResult[A]): Reads[A] = new Reads[A] {
    def reads(value: DdbValue) = f(value)
  }

  def at[A](key: DdbKey)(implicit reads: Reads[A]): Reads[A] = {
    Reads[A] {
      case i: DdbItem => key.findWithin(i).flatMap(reads.reads(_))
      case _ => DdbError(Seq("error.expected.ddbitem"))
    }
  }

  implicit def applicative(implicit applicativeDdbResult: Applicative[DdbResult]): Applicative[Reads] = new Applicative[Reads] {
    def pure[A](a: A): Reads[A] = Reads[A] { _ => DdbSuccess(a) }

    def map[A, B](m: Reads[A], f: A => B): Reads[B] = m.map(f)

    def apply[A, B](mf: Reads[A => B], ma: Reads[A]): Reads[B] = new Reads[B] {
      def reads(value: DdbValue) = applicativeDdbResult(mf.reads(value), ma.reads(value))
    }
  }

  implicit def alternative(implicit a: Applicative[Reads]): Alternative[Reads] = new Alternative[Reads] {
    val app = a
    def |[A, B >: A](alt1: Reads[A], alt2: Reads[B]): Reads[B] = new Reads[B] {
      def reads(value: DdbValue) = alt1.reads(value) match {
        case d1 @ DdbSuccess(_) => d1
        case DdbError(es1) => alt2.reads(value) match {
          case d1 @ DdbSuccess(_) => d1
          case DdbError(es2) => DdbError(es1 ++ es2)
        }
      }
    }
    def empty: Reads[Nothing] = new Reads[Nothing] { def reads(value: DdbValue) = DdbError(Seq()) }
  }


  implicit def functorReads(implicit a: Applicative[Reads]) = new Functor[Reads] {
    def fmap[A, B](reads: Reads[A], f: A => B): Reads[B] = a.map(reads, f)
  }




  implicit object IntReads extends Reads[Int] {
    def reads(item: DdbValue) = item match {
      case DdbNumber(n) => DdbSuccess(n.toInt)
      case _ => DdbError(Seq("error.expected.ddbnumber"))
    }
  }

  implicit object LongReads extends Reads[Long] {
    def reads(item: DdbValue) = item match {
      case DdbNumber(n) => DdbSuccess(n.toLong)
      case _ => DdbError(Seq("error.expected.ddbnumber"))
    }
  }

  implicit object ByteArrayReads extends Reads[Array[Byte]] {
    def reads(item: DdbValue) = item match {
      case DdbBinary(n) => DdbSuccess(n)
      case _ => DdbError(Seq("error.expected.ddbbinary"))
    }
  }

  implicit object StringReads extends Reads[String] {
    def reads(item: DdbValue) = item match {
      case DdbString(n) => DdbSuccess(n)
      case _ => DdbError(Seq("error.expected.ddbstring"))
    }
  }
}