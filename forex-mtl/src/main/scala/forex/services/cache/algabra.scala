package forex.services.cache

import forex.domain.{Rate}

trait Algebra[F[_]] {
  def add(rate: Rate): F[Unit]
  def get(pair: Rate.Pair): F[Option[Rate]]
  def getPairs(): F[List[Rate.Pair]]
}
