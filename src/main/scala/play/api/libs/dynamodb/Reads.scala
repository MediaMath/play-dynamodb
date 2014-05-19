/**
 * Copyright (C) 2014 MediaMath <http://www.mediamath.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author themodernlife
 * @author alekseyig
 */
package play.api.libs.dynamodb

import scala.collection.generic
import scala.language.higherKinds

import org.joda.time.{DateTime, LocalDate}
import org.joda.time.format.{DateTimeFormat, ISODateTimeFormat}
import play.api.libs.functional.{Alternative, Applicative, Functor}

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
      case i: DdbItem => key.findWithin(i).flatMap(reads.reads)
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

  implicit object FloatReads extends Reads[Float] {
    def reads(item: DdbValue) = item match {
      case DdbNumber(n) => DdbSuccess(n.toFloat)
      case _ => DdbError(Seq("error.expected.ddbnumber"))
    }
  }

  implicit object DoubleReads extends Reads[Double] {
    def reads(item: DdbValue) = item match {
      case DdbNumber(n) => DdbSuccess(n.toDouble)
      case _ => DdbError(Seq("error.expected.ddbnumber"))
    }
  }

  implicit object BigDecimalReads extends Reads[BigDecimal] {
    def reads(item: DdbValue) = item match {
      case DdbNumber(n) => DdbSuccess(n.underlying())
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

  implicit def OptionReads[T](implicit fmt: Reads[T]): Reads[Option[T]] = new Reads[Option[T]] {
    def reads(json: DdbValue) = fmt.reads(json) match {
      case DdbSuccess(v) => DdbSuccess(Some(v))
      case _ => DdbSuccess(None)
    }
  }

  def fromDdbValue[T](value: DdbValue)(implicit fdv: Reads[T]): DdbResult[T] = fdv.reads(value)

  implicit def traversableReads[F[_], A](implicit bf: generic.CanBuildFrom[F[_], A, F[A]], ra: Reads[A]): Reads[F[A]] = new Reads[F[A]] {
    def reads(item: DdbValue) = item match {
      case DdbSet(xs) =>
        var hasErrors = false

        val either = xs.map{x =>
          fromDdbValue[A](x)(ra) match {
            case DdbSuccess(v) => Right(v)
            case DdbError(e) =>
              hasErrors = true
              Left(e)
          }
        }

        if (hasErrors) {
          val allErrors = either.map(_.left.get).foldLeft(List[String]())((acc,v) => acc ++ v)
          DdbError(allErrors)
        } else {
          val builder = bf()
          either.foreach(builder += _.right.get)
          DdbSuccess(builder.result())
        }

      case _ => DdbError(Seq("error.expected.ddbset"))
    }
  }

  def localDateReads(pattern: String, corrector: String => String = identity) = new Reads[LocalDate] {
    val df = if (pattern == "") ISODateTimeFormat.localDateParser else DateTimeFormat.forPattern(pattern)

    private def parseDate(input: String): Option[LocalDate] =
      scala.util.control.Exception.allCatch[LocalDate] opt LocalDate.parse(input, df)

    def reads(item: DdbValue) = item match {
      case DdbString(n) => parseDate(corrector(n)) match {
        case Some(d) => DdbSuccess(d)
        case None => DdbError(Seq("error.expected.localdate.format", pattern))
      }
      case _ => DdbError(Seq("error.expected.ddbdate_string"))
    }
  }

  implicit val DefaultLocalDateReads = localDateReads("")


  def dateTimeReads(pattern: String, corrector: String => String = identity) = new Reads[DateTime] {
    val df = if (pattern == "") ISODateTimeFormat.dateTimeNoMillis() else DateTimeFormat.forPattern(pattern)

    private def parseDate(input: String): Option[DateTime] =
      scala.util.control.Exception.allCatch[DateTime] opt DateTime.parse(input, df)

    def reads(item: DdbValue) = item match {
      case DdbString(n) => parseDate(corrector(n)) match {
        case Some(d) => DdbSuccess(d)
        case None => DdbError(Seq("error.expected.datetime.format", pattern))
      }
      case _ => DdbError(Seq("error.expected.ddbdate_string"))
    }
  }

  implicit val DefaultDateTimeReads = dateTimeReads("")
}