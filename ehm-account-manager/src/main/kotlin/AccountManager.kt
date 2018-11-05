package ehm.accountmanager

import org.http4k.core.*
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.server.Netty
import org.http4k.server.asServer

data class Account(val id:String, val fullName:String, val currency:String, val tariffRef:String)

object AccountManager {
    fun findByMedia(media:String):Account? = when (media) {
        "1234ABCD" -> Account("123", "John Doe", "EUR", "F1")
        else -> null
    }

    val app: HttpHandler = routes(
            "/media/{media}/account" bind Method.GET to { req: Request ->
                val media: String = req.path("media")?:""

                val account = findByMedia(media)

                account
                        ?.let { Response(OK).with(Body.auto<Account>().toLens() of it) }
                        ?: Response(NOT_FOUND)
            }
    )

    fun server(port:Int = 9001) = app.asServer(Netty(port))
}

fun main(args: Array<String>) {
    AccountManager.server().start()
    println("account-manager started on http://localhost:9001/")
}