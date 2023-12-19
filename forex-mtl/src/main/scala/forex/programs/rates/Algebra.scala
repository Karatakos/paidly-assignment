package forex.programs.rates

import forex.domain.Rate

import errors._

// Question: Why do we leverage algebra for programs? Do we expect to 
//           support multiple implementations of a program as we do with 
//           interpreters?
//
trait Algebra[F[_]] {
  def getRate(request: Protocol.GetRatesRequest): F[Either[Error, Rate]]
}
