package forex.services.cache.interpreters

import forex.services.cache.Algebra
import forex.domain.{ Rate }

import com.typesafe.scalalogging.LazyLogging

import cats.Applicative
import cats.implicits._

import java.time.OffsetDateTime

import scala.collection.concurrent.TrieMap

class LocalCache[F[_]: Applicative](defaultExpiryMins: Int = 5) extends LazyLogging with Algebra[F]{
  // Needed something mutable and concurrency friendly
  // https://www.scala-lang.org/api/2.12.8/scala/collection/concurrent/TrieMap.html 
  //
  private val cache: TrieMap[String, Rate] = TrieMap()

  override def add(rate: Rate): F[Unit]  = {
    val reciprocal = rate.reciprocal()

    addOrUpdate(rate)
    addOrUpdate(reciprocal)

    logger.info(s"Adding or updating live rate ${rate.pair} to cache as well as it's reciprocal ${reciprocal.pair}") 
    
    ().pure[F]
  }

  private def addOrUpdate(rate: Rate): F[Unit] = {
    val key = rate.pair.toString
    val value = rate

    if (cache.contains(key)) 
      cache -= key

    cache += (key -> value)

    ().pure[F]
  }

  override def get(pair: Rate.Pair): F[Option[Rate]] = {
    logger.info(s"Looking for cache hit for pair $pair")

    val keyStr = pair.toString

    (for {
        rate <- cache.get(keyStr) 
        if rate.timestamp.value.plusMinutes(defaultExpiryMins.toLong).isAfter(OffsetDateTime.now())
      } yield rate).pure[F]
  }
   
  override def getPairs(): F[List[Rate.Pair]] = {
    // Debt: Are these statements supposed to be captured as side effects via F?
    //
    logger.info(s"Fetching all registered pairs from the cache excluding reciprocals")

    cache
      .values
      .toList
      .filter(rate => !rate.isReciprocal)
      .map(rate => rate.pair)
      .pure[F]
  }
}