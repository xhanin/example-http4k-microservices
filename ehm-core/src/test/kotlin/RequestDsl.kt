package ehm.core

import org.http4k.client.JavaHttpClient
import org.http4k.core.Method
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request


fun main(args: Array<String>) {
    (HTTP GET "/media/1234ABCD/account").send()

    GET("/media/1234ABCD/account").send()

    (HTTP POST "/charges")
            .body("""
        {
                "station":"FR*SOD*12345", "media":"1234ABCD",
                "datetime":"2018-11-09T16:03:25+01:00[Europe/Paris]","duration":1234,"energy":5432
                }
            """).send()

    POST("/charges")
            .body("""
        {
                "station":"FR*SOD*12345", "media":"1234ABCD",
                "datetime":"2018-11-09T16:03:25+01:00[Europe/Paris]","duration":1234,"energy":5432
                }
            """).send()

}

object HTTP {
    infix fun GET(path:String) = Request(Method.GET, path)
    infix fun POST(path:String) = Request(Method.POST, path)
}

operator fun Method.invoke(path:String) = Request(this, path)

fun Request.send() = JavaHttpClient().invoke(this)
