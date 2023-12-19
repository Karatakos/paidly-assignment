package forex.services.rates.interpreters

import forex.domain.{ Price, Rate, Timestamp }
import forex.services.rates.Algebra
import forex.services.rates.errors._

import com.typesafe.scalalogging.LazyLogging

import cats.Monad
import cats.syntax.applicative._
import cats.syntax.either._

class OneFrameLive[F[_]: Monad] extends LazyLogging with Algebra[F] {
  override def getRates(pairs: List[Rate.Pair]): F[Either[Error, List[Rate]]] = {
    logger.info(s"Fetching live rates for all registered pairs from our cache")

    pairs.map(pair => 
      Rate(pair, Price(BigDecimal(100)), Timestamp.now)).asRight[Error].pure[F]
  }
}