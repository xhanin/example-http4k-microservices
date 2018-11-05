package ehm.core

import ehm.accountmanager.AccountManager
import org.http4k.client.JavaHttpClient
import org.http4k.core.*
import org.http4k.filter.ClientFilters
import org.http4k.server.Http4kServer
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class EVChargeManagerTest {
    @Test
    fun `should handle charge - in memory`() {
        // tests EVChargeManager app handler, wired directly to its dependencies app handlers
        // (no network involved)
        shouldHandleChargeWithHandler(EVChargeManager(
                accountManagerHandler = AccountManager.app
        ).app)
    }

    @Test
    fun `should handle charge - with real dependencies`() {
        // tests EVChargeManager app handler, calling the dependencies through http
        val accountManagerPort = 10021
        listOf(AccountManager.server(accountManagerPort)).use {
            shouldHandleChargeWithHandler(EVChargeManager(
                    accountManagerHandler = localhostClient(accountManagerPort)
            ).app)
        }
    }

    @Test
    fun `should handle charge - IT`() {
        // full integration test for EVChargeManager
        val coreManagerPort    = 10030
        val accountManagerPort = 10031
        listOf(
                AccountManager.server(accountManagerPort),
                EVChargeManager(
                        accountManagerHandler = localhostClient(accountManagerPort)
                ).server(coreManagerPort)
        ).use {
            shouldHandleChargeWithHandler(localhostClient(coreManagerPort))
        }
    }


    private fun shouldHandleChargeWithHandler(evChargeManager: HttpHandler) {
        val resp = evChargeManager(Request(Method.POST, "/charges").body("""
                {
                "station":"FR*SOD*12345", "media":"1234ABCD",
                "datetime":"2018-11-09T16:03:25+01:00[Europe/Paris]","duration":1234,"energy":5432
                }
            """.trimIndent()))

        expectThat(resp) {
            get { status }.isEqualTo(Status.ACCEPTED)
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
