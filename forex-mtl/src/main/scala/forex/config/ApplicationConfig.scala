package forex.config

import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
    http: HttpConfig,
    services: ServicesConfig
)

case class HttpConfig(
    host: String,
    port: Int,
    timeout: FiniteDuration
)

case class ServicesConfig(
    oneframe: OneFrameConfig
)

case class OneFrameConfig(
    endpoint: String,
    token: String
)
