package ehm.consumptions

import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.junit.jupiter.api.Test
import strikt.api.expect
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import java.math.BigDecimal
import java.time.ZonedDateTime

class ConsumptionManagerTest {
    @Test
    fun `should record consumption`() {
        val resp = ConsumptionManager.app(
                Request(Method.POST, "/consumptions")
                        .body("""
                            {
                              "id":"67890", "accountRef":"123",
                              "charge": {
                                 "station":"FR*SOD*12345", "media":"1234ABCD",
                                 "datetime":"2018-11-09T16:03:25+01:00[Europe/Paris]","duration":1234,"energy":5432
                              },
                              "price":{"amount":7,"currency":"EUR"}}
                            }
                        """.trimIndent())
        )

        expect {
            that(resp) {
                get { status }.isEqualTo(Status.ACCEPTED)
            }
            that(ConsumptionManager.consumptions) {
                hasSize(1)
                contains(ChargeConsumption("67890", "123",
                        Charge("FR*SOD*12345", "1234ABCD",
                                ZonedDateTime.parse("2018-11-09T16:03:25+01:00[Europe/Paris]"),
                                1234, 5432),
                        Price(BigDecimal.valueOf(7), "EUR")))
            }
        }
    }
}