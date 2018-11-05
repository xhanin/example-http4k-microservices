package ehm.chargepricer

import org.http4k.core.*
import org.http4k.core.Status.Companion.OK
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Netty
import org.http4k.server.asServer
import java.math.BigDecimal
import java.time.ZonedDateTime

data class Charge(
        val tariffRef:String,
        val datetime: ZonedDateTime,
        val duration:DurationInS,
        val energy:EnergyInWh)

typealias DurationInS = Long
typealias EnergyInWh = Long

data class Price(val amount:BigDecimal, val currency: String)

data class PriceRequest(val charge:Charge, val currency: String, val price:Price? = null)

object ChargePricer {
    fun price(charge:Charge, currency: String):Price = when (charge.tariffRef) {
        "FREE" -> Price(BigDecimal.ZERO, currency)
        else -> Price(
                BigDecimal.valueOf(charge.duration * charge.energy)
                        / BigDecimal.valueOf(1_000_000),
                currency
        )
    }

    val app: HttpHandler = routes(
            "/price-requests" bind Method.POST to { req: Request ->
                val bodyLens = Body.auto<PriceRequest>().toLens()

                val priceRequest = bodyLens.extract(req)
                val price = price(priceRequest.charge, priceRequest.currency)

                Response(OK).with(bodyLens of priceRequest.copy(price = price))
            }
    )

    fun server(port:Int = 9002) = app.asServer(Netty(port))
}

fun main(args: Array<String>) {
    ChargePricer.server().start()
    println("charge-pricer started on http://localhost:9002/")
}