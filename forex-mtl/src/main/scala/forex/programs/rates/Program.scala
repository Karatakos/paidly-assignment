package forex.programs.rates

import cats.Monad
import cats.data.EitherT
import cats.implicits._

import forex.domain._
import forex.services.RatesService
import forex.services.CacheService

import errors._

class Program[F[_]: Monad](
    ratesService: RatesService[F],
    cacheService: CacheService[F]
) extends Algebra[F] {
  override def getRate(request: Protocol.GetRatesRequest): F[Either[Error, Rate]] = {
    getCachedRate(Rate.Pair(request.from, request.to))
  }

  private def getCachedRate(pair: Rate.Pair): F[Either[Error, Rate]] = {
    cacheService
      .get(pair)
      .flatMap {
        case Some(rate) => rate.asRight[Error].pure[F]
        case None => getLiveRateAndRefreshCache(pair)
      }
  }

  private def getLiveRateAndRefreshCache(pair: Rate.Pair): F[Either[Error, Rate]] = {

    // Debt: Not clean. Not sure how I can combine in a single for-comprehension
    // since cacheService.getPairs() doesnt need to return an Either. Could
    // box it into an either but that seems just as bad
    //
    cacheService.getPairs().flatMap {
      list => {
        val pairs = pair :: list

        (for {
          rates <- EitherT(ratesService.getRates(pairs)).leftMap(toProgramError)
          rate <- EitherT.fromOptionF({
                    rates.foreach(rate => cacheService.add(rate))
                    // Bug: And if it's not found!?!
                    //
                    rates.find(_.pair == pair)}.pure[F],
                    Error.RateLookupFailed(s"Could not add $pair to cache"): Error)
        } yield rate).value
      }
    }
  }
}

object Program {
  def apply[F[_]: Monad](
      ratesService: RatesService[F],
      cacheService: CacheService[F]
  ): Algebra[F] = new Program[F](ratesService, cacheService)
}
