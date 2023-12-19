package forex.services.cache.interpreters

import forex.services.cache.Algebra
import forex.domain.{ Rate }

import com.typesafe.scalalogging.LazyLogging

import cats.Applicative
import cats.implicits._

import java.time.OffsetDateTime

class LocalCache[F[_]: Applicative](defaultExpiryMins: Int = 5) extends LazyLogging with Algebra[F]{
  private var cache: Map[String, Rate] = Map()

  override def add(rate: Rate): F[Unit]  = {
    val reciprocal = rate.reciprocal()

    // Debt: This is really dirty. Should use State or an external solution
    //
    cache = cache + (rate.pair.toString -> rate)
    cache = cache + (reciprocal.pair.toString -> reciprocal)

    logger.info(s"Adding live rate to cache for requested pair ${rate.pair} as well as it's reciprocal (calculated) ${reciprocal.pair}")
  
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
    logger.info(s"Fetching all registered pairs from the cache (excludes reciprocal pairs)")

    cache
      .values
      .toList
      .filter(rate => !rate.isReciprocal)
      .map(rate => rate.pair)
      .pure[F]
  }
}