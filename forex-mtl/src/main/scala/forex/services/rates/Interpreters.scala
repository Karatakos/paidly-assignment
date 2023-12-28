package forex.services.rates

import cats.effect.ConcurrentEffect

import forex.config.OneFrameConfig

import org.http4s.client._

import interpreters._

object Interpreters {
  def oneFrameLive[F[_]: ConcurrentEffect](client: Client[F], config: OneFrameConfig): Algebra[F] = 
    new OneFrameLive[F](client, config)
}
