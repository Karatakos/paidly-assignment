package forex

// Question: What if we want to layer services or implement
//           cross cutting concerns as dependent services?
//
package object services {
  type RatesService[F[_]] = rates.Algebra[F]
  type CacheService[F[_]] = cache.Algebra[F]

  final val RatesServices = rates.Interpreters
  final val CacheServices = cache.Interpreters
}
