package forex.domain

case class Price(value: BigDecimal) extends AnyVal

// Question: Is this needed for case class?
//
object Price {
  def apply(value: Integer): Price =
    Price(BigDecimal(value))
}
