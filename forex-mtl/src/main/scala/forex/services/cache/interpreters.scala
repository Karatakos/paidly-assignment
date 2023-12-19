package forex.services.cache

import cats.Monad

import interpreters._

object Interpreters {
  def localCache[F[_]: Monad]: Algebra[F] = new LocalCache[F]()
}