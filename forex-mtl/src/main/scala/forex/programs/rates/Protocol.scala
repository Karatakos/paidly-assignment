package forex.programs.rates

import forex.domain.Currency

object Protocol {

  // Question: Why don't we use domain objects, i.e. Pair, since 
  //           a program encapsulates business logic? Wouldn't GetRatesRequest
  //           be more aplicable only to the http interface (RatesHttpRoutes)?
  //
  final case class GetRatesRequest(
      from: Currency,
      to: Currency
  )

}
