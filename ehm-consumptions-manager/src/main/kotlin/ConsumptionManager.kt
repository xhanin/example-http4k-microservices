package ehm.consumptions

import org.http4k.core.*
import org.http4k.core.Status.Companion.ACCEPTED
import org.http4k.core.Status.Companion.OK
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.server.Netty
import org.http4k.server.asServer
import java.math.BigDecimal
import java.time.ZonedDateTime

data class Charge(
        val station:String,
        val media:String,
        val datetime: ZonedDateTime,
        val duration:DurationInS,
        val energy:EnergyInWh)

typealias DurationInS = Long
typealias EnergyInWh = Long

data class Price(val amount:BigDecimal, val currency: String)

data class ChargeConsumption(val id:String, val accountRef:String, val charge:Charge, val price:Price)

object ConsumptionManager {
    val consumptions = mutableListOf<ChargeConsumption>()

    fun clear() {
        consumptions.clear()
    }

    fun recordChargeConsumption(consumption:ChargeConsumption) {
        consumptions.add(consumption)
    }

    fun findConsumptionsByAccount(account:String) = consumptions.asSequence().filter { it.accountRef == account }

    val app: HttpHandler = routes(
            "/consumptions" bind Method.POST to { req: Request ->
                val consumption = Body.auto<ChargeConsumption>().toLens().extract(req)

                recordChargeConsumption(consumption)

                Response(ACCEPTED)
            },
            "/accounts/{account}/consumptions" bind Method.GET to { req: Request ->
                val account = req.path("account")?:""

                val consumptions = findConsumptionsByAccount(account)

                Response(OK)
                        .with(Body.auto<List<ChargeConsumption>>().toLens() of consumptions.toList())
            }

    )

    fun server(port:Int = 9003) = app.asServer(Netty(port))
}

fun main(args: Array<String>) {
    ConsumptionManager.server().start()
    println("consumptions-manager started on http://localhost:9003/")
}