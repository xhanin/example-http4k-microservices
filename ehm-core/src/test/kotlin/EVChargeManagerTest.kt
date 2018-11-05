package ehm.core

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import ehm.accountmanager.AccountManager
import ehm.chargepricer.ChargePricer
import ehm.consumptions.ChargeConsumption
import ehm.consumptions.ConsumptionManager
import ehm.consumptions.Price
import org.http4k.client.JavaHttpClient
import org.http4k.core.*
import org.http4k.filter.ClientFilters
import org.http4k.format.defaultKotlinModuleWithHttp4kSerialisers
import org.http4k.server.Http4kServer
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.math.BigDecimal


class EVChargeManagerTest {
    @Test
    fun `should handle charge - in memory`() {
        // tests EVChargeManager app handler, wired directly to its dependencies app handlers
        // (no network involved)
        shouldHandleChargeWithHandler(EVChargeManager(
                accountManagerHandler      = AccountManager.app,
                chargePricerHandler        = ChargePricer.app,
                consumptionManagerHandler  = ConsumptionManager.app
        ).app)
    }

    @Test
    fun `should handle charge - with real dependencies`() {
        // tests EVChargeManager app handler, calling the dependencies through http
        val accountManagerPort = 10021
        val chargePricerPort = 10022
        val consumptionManagerPort = 10023
        listOf(AccountManager.server(accountManagerPort),
                ChargePricer.server(chargePricerPort),
                ConsumptionManager.server(consumptionManagerPort)).use {
            shouldHandleChargeWithHandler(EVChargeManager(
                    accountManagerHandler      = localhostClient(accountManagerPort),
                    chargePricerHandler        = localhostClient(chargePricerPort),
                    consumptionManagerHandler  = localhostClient(consumptionManagerPort)
            ).app)
        }
    }

    @Test
    fun `should handle charge - IT`() {
        // full integration test for EVChargeManager
        val coreManagerPort         = 10030
        val accountManagerPort      = 10031
        val chargePricerPort        = 10032
        val consumptionManagerPort  = 10033

        listOf(
                AccountManager.server(accountManagerPort),
                ChargePricer.server(chargePricerPort),
                ConsumptionManager.server(consumptionManagerPort),
                EVChargeManager(
                        accountManagerHandler       = localhostClient(accountManagerPort),
                        chargePricerHandler         = localhostClient(chargePricerPort),
                        consumptionManagerHandler   = localhostClient(consumptionManagerPort)
                ).server(coreManagerPort)
        ).use {
            shouldHandleChargeWithHandler(localhostClient(coreManagerPort))
        }
    }


    private fun shouldHandleChargeWithHandler(evChargeManagerHandler: HttpHandler) {

        val resp = evChargeManagerHandler(Request(Method.POST, "/charges").body("""
                {
                "station":"FR*SOD*12345", "media":"1234ABCD",
                "datetime":"2018-11-09T16:03:25+01:00[Europe/Paris]","duration":1234,"energy":5432
                }
            """.trimIndent()))

        expectThat(resp) {
            get { status }.isEqualTo(Status.ACCEPTED)
        }

        val cons = evChargeManagerHandler(Request(Method.GET, "/accounts/123/consumptions"))

        expectThat(cons) {
            get { status }.isEqualTo(Status.OK)
            get {
                jackson()
                        .readValue<List<ChargeConsumption>>(bodyString(),
                                object : TypeReference<List<ChargeConsumption>>() {})
            }
                    .get { last() }
                    .and {
                        get { accountRef }.isEqualTo("123")
                        get { price }.isEqualTo(Price(BigDecimal.valueOf(7), "EUR"))
                    }
        }
    }
}

private fun localhostClient(port: Int): HttpHandler =
        ClientFilters.SetBaseUriFrom(Uri.of("http://localhost:$port")).then(JavaHttpClient())

private fun List<Http4kServer>.use(function: () -> Unit) {
    this.forEach { it.start() }
    try {
        function()
    } finally {
        this.forEach { it.stop() }
    }
}



private fun jackson() = ObjectMapper()
        .registerModule(defaultKotlinModuleWithHttp4kSerialisers)
        .disableDefaultTyping()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
        .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
        .configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true)
