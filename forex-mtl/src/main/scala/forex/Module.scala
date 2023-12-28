package forex

import cats.effect.{ Timer, ConcurrentEffect }

import forex.config._
import forex.http.rates.RatesHttpRoutes
import forex.services._
import forex.programs._

import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{ AutoSlash, Timeout }
import org.http4s.client._

class Module[F[_]: Timer: ConcurrentEffect](config: ApplicationConfig, client: Client[F]) {
  
  private val ratesService: RatesService[F] = RatesServices.oneFrameLive[F]( 
    client,
    config.services.oneframe)

  private val cacheService: CacheService[F] = CacheServices.localCache[F]

  private val ratesProgram: RatesProgram[F] = RatesProgram[F](
    ratesService, 
    cacheService)

  private val ratesHttpRoutes: HttpRoutes[F] 
    = new RatesHttpRoutes[F](ratesProgram).routes

  type PartialMiddleware = HttpRoutes[F] => HttpRoutes[F]
  type TotalMiddleware   = HttpApp[F] => HttpApp[F]

  private val routesMiddleware: PartialMiddleware = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    }
  }

  private val appMiddleware: TotalMiddleware = { http: HttpApp[F] =>
    Timeout(config.http.timeout)(http)
  }

  private val http: HttpRoutes[F] = ratesHttpRoutes

  val httpApp: HttpApp[F] = appMiddleware(routesMiddleware(http).orNotFound)

}
