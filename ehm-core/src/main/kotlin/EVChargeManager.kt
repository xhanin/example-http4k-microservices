package ehm.core

import ehm.accountmanager.Account
import ehm.chargepricer.Charge
import ehm.chargepricer.Price
import ehm.chargepricer.PriceRequest
import ehm.consumptions.ChargeConsumption
import org.http4k.client.JavaHttpClient
import org.http4k.core.*
import org.http4k.core.Status.Companion.ACCEPTED
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.filter.ClientFilters
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Netty
import org.http4k.server.asServer
import java.time.ZonedDateTime
import java.util.*

/*
{"station":"FR*SOD*12345", "media":"1234ABCD", "datetime":"2018-11-09T16:03:25+0100","duration":1234,"energy":5432}
 */
data class EVCharge(val station:String, val media:String,
                    val datetime:ZonedDateTime,
                    val duration:DurationInS,
                    val energy:EnergyInWh)

typealias DurationInS = Long
typealias EnergyInWh = Long

class EVChargeManager(
    private val accountManagerHandler:HttpHandler,
    private val chargePricerHandler:HttpHandler,
    private val consumptionManagerHandler: HttpHandler
) {

    fun findAccountByMedia(media:String):Account? =
            accountManagerHandler(Request(Method.GET, "/media/$media/account")).let {
                when (it.status) {
                    OK -> Body.auto<Account>().toLens().extract(it)
                    NOT_FOUND -> null
                    BAD_REQUEST -> throw IllegalArgumentException("${it.status} ${it.bodyString()}")
                    else -> throw IllegalStateException("${it.status} ${it.bodyString()}")
                }
            }

    fun price(charge: Charge, currency: String): Price =
            chargePricerHandler(Request(Method.POST, "/price-requests").with(
                    Body.auto<PriceRequest>().toLens() of PriceRequest(charge, currency))).let {
                Body.auto<PriceRequest>().toLens().extract(it).price
                        ?:throw IllegalStateException("pricer didn't give any price")
            }

    fun recordChargeConsumption(consumption: ChargeConsumption) =
            consumptionManagerHandler(Request(Method.POST, "/consumptions").with(
                    Body.auto<ChargeConsumption>().toLens() of consumption
            ))


    fun handleCharge(charge:EVCharge) {
        val account = findAccountByMedia(charge.media)
                ?:throw IllegalArgumentException("media not found ${charge.media}")

        val price = price(Charge(
                account.tariffRef, charge.datetime, charge.duration, charge.energy), account.currency)

        val consumption = ChargeConsumption(
                UUID.randomUUID().toString(), account.id,
                ehm.consumptions.Charge(charge.station, charge.media, charge.datetime, charge.duration, charge.energy),
                ehm.consumptions.Price(price.amount, price.currency)
        )

        recordChargeConsumption(consumption)

        println("recorded consumption $consumption")
    }

    val app: HttpHandler = routes(
            "/charges" bind Method.POST to { req: Request ->
                val charge = Body.auto<EVCharge>().toLens().extract(req)

                try {
                    handleCharge(charge)

                    Response(ACCEPTED)
                } catch (e:IllegalArgumentException) {
                    Response(BAD_REQUEST).body(e.toString())
                } catch (e:Exception) {
                    Response(INTERNAL_SERVER_ERROR).body(e.toString())
                }
            },
            "/accounts/{account}/consumptions" bind Method.GET
                    to consumptionManagerHandler // proxy to consumption manager micro service

    )

    fun server(port:Int = 9000) = app.asServer(Netty(port))

    companion object {
        fun default() = JavaHttpClient().let {
            EVChargeManager(
                    ClientFilters.SetBaseUriFrom(Uri.of("http://localhost:9001")).then(it),
                    ClientFilters.SetBaseUriFrom(Uri.of("http://localhost:9002")).then(it),
                    ClientFilters.SetBaseUriFrom(Uri.of("http://localhost:9003")).then(it)
            )
        }
    }
}

fun main(args: Array<String>) {
    EVChargeManager.default().server().start()
    println("ev-charge-manager started on http://localhost:9000/")
}