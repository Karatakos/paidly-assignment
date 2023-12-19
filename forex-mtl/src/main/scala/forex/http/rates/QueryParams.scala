package forex.http.rates

import forex.domain.Currency
import org.http4s.{ QueryParamDecoder, ParseFailure }
import org.http4s.dsl.impl.ValidatingQueryParamDecoderMatcher
import scala.util.Try
import cats.data.Validated

object QueryParams {

  private[http] implicit val currencyQueryParam: QueryParamDecoder[Currency] =
    QueryParamDecoder[String].emapValidatedNel {
      curr => 
        Validated.fromTry(Try(Currency.fromString(curr)))
          .leftMap(_ => ParseFailure(curr, ""))
          .toValidatedNel
    }

  object FromQueryParam extends ValidatingQueryParamDecoderMatcher[Currency]("from")
  object ToQueryParam extends ValidatingQueryParamDecoderMatcher[Currency]("to")

}
