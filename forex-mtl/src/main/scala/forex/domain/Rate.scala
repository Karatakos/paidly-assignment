package forex.domain

case class Rate(
    pair: Rate.Pair,
    price: Price,
    timestamp: Timestamp,
    isReciprocal: Boolean = false
) {
  def reciprocal(): Rate = {
    val invertedPair = Rate.Pair(this.pair.to, this.pair.from)
    val invertedPrice = Price(1 / this.price.value)
    val invertedTimestamp = this.timestamp

    new Rate(invertedPair, invertedPrice, invertedTimestamp, true)
  }
}

object Rate {
  final case class Pair(
      from: Currency,
      to: Currency
  ) {
    override def toString: String = {
      s"$from$to"
    }
  }
}
