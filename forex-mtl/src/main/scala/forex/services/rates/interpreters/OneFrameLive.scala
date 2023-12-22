package forex.services.rates.interpreters

import forex.domain.{ Price, Rate, Timestamp, Currency }
import forex.services.rates.Algebra
import forex.services.rates.errors._
import forex.config.OneFrameConfig

import com.typesafe.scalalogging.LazyLogging

import cats.data.EitherT
import cats.effect.{ Sync, ConcurrentEffect }
import cats.implicits._

import io.circe.Decoder
import io.circe.generic.semiauto._

import org.http4s._
import org.http4s.client.Client
import org.http4s.circe._

import org.typelevel.ci.CIString

case class OneFrameRate (
  from: String,
  to: String,
  bid: Double,
  ask: Double,
  price: Double,
  time_stamp: String
) 

object OneFrameRate {
  implicit val decoder: Decoder[OneFrameRate] = deriveDecoder[OneFrameRate]
  implicit def entityDecoder[F[_]: Sync]: EntityDecoder[F, List[OneFrameRate]] = 
    jsonOf[F, List[OneFrameRate]]
}

class OneFrameLive[F[_]: ConcurrentEffect](
  client: Client[F], config: OneFrameConfig) 
  extends LazyLogging with Algebra[F] {

  override def getRates(pairs: List[Rate.Pair]): F[Either[Error, List[Rate]]] = {
    logger.info(s"Fetching live rates for all registered pairs from the One Frame service.")

    // TODO: Add Logger middleware for logging req & res
    //       https://http4s.org/v1/docs/server-middleware.html#requestlogger-responselogger-logger 

    (for {
      uri <- EitherT(buildUri(pairs))
      oneframeRates <- EitherT(get(uri))
      rates <- EitherT({
        oneframeRates
          .map { r =>
            // Second thoughts this be a program responsibility? :/
            //
            Rate(
              Rate.Pair(
                Currency.fromString(r.from), 
                Currency.fromString(r.to)), 
              Price(BigDecimal(r.price)), 
              Timestamp.now) }
          .asRight[Error]
          .pure[F] })
    } yield rates).value
  }

  private def get(uri: Uri): F[Either[Error, List[OneFrameRate]]] = {
    val req = Request[F](
      method = Method.GET,
      uri = uri,
      // Happy to never work with http4s client ever again
      //
      headers = Headers(
        List(
          Header.Raw(CIString("Accept"), "application/json"), 
          // Security: Need secret storage for config.token
          //
          Header.Raw(CIString("token"), config.token))))     

    client.expect[List[OneFrameRate]](req)
      .redeem (
        error => {
          logger.error(error.getMessage, error)
          
          // Bug: Client will throw anyway
          //
          error
            .asLeft[List[OneFrameRate]]
            .leftMap(e => Error.OneFrameLookupFailed(e.getMessage): Error)
        },
        value => { value.asRight[Error] }
      )
  }

  private def buildUri(pairs: List[Rate.Pair]): F[Either[Error, Uri]] = {
    val forPath = s"${config.endpoint}/rates" 
    val withQueryParams =
      pairs
        .map(pair => s"&pair=${pair.from}${pair.to}")
        // Note: Tried reducing into a Uri via withQueryParams for a cleaner solution
        //       but it would only spit out a single distint pair. 
        //
        .reduce((acc, str) => acc + str)
        .drop(1)
    
    val uriStr = s"$forPath?$withQueryParams"

    val uri = Uri.fromString(uriStr)
      .leftMap(e => Error.OneFrameLookupFailed(e.getMessage): Error)

    uri.pure[F]
  }

}