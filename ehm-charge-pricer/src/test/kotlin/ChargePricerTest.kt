package ehm.chargepricer

import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ChargePricerTest {
    @Test
    fun `should price free charge`() {
        val resp = ChargePricer.app(
                Request(Method.POST, "/price-requests")
                        .body("""
                            {
                              "charge": {
                                 "tariffRef":"FREE",
                                 "datetime":"2018-11-09T16:03:25+01:00[Europe/Paris]","duration":1234,"energy":5432
                              },
                              "currency": "EUR"
                            }
                        """.trimIndent())
        )

        expectThat(resp) {
            get{status}.isEqualTo(Status.OK)
            get{bodyString()}.isEqualTo("""{"charge":{"tariffRef":"FREE","datetime":"2018-11-09T16:03:25+01:00[Europe/Paris]","duration":1234,"energy":5432},"currency":"EUR","price":{"amount":0,"currency":"EUR"}}""")
        }
    }

    @Test
    fun `should price f1 charge`() {
        val resp = ChargePricer.app(
                Request(Method.POST, "/price-requests")
                        .body("""
                            {
                              "charge": {
                                 "tariffRef":"F1",
                                 "datetime":"2018-11-09T16:03:25+01:00[Europe/Paris]","duration":1234,"energy":5432
                              },
                              "currency": "EUR"
                            }
                        """.trimIndent())
        )

        expectThat(resp) {
            get{status}.isEqualTo(Status.OK)
            get{bodyString()}.isEqualTo("""{"charge":{"tariffRef":"F1","datetime":"2018-11-09T16:03:25+01:00[Europe/Paris]","duration":1234,"energy":5432},"currency":"EUR","price":{"amount":7,"currency":"EUR"}}""")
        }
    }
}