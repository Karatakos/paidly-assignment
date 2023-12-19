package forex.services.rates

import cats.Monad

import interpreters._

object Interpreters {
  def oneFrameLive[F[_]: Monad]: Algebra[F] = new OneFrameLive[F]()
}
