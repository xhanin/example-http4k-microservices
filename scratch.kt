
    springboot
    ----------

    Started BlogApplicationKt in 5.433 seconds (JVM running for 5.823)

    ----------


    typealias HttpHandler = (Request) -> Response

    val echo = { req -> Reponse(OK).body(req.body) }
    val resp = echo(Request(POST, "/echo").body("hello"))


    typealias HttpFilter = (HttpHandler) -> HttpHandler


    val twitterFilter = Filter { next:HttpHandler ->
        {req-> next(req.body(req.bodyString().take(140)))}
    }
    val tweetHandler = twitterFilter.then(echo)


    typealias Router = (Request) -> HttpHandler?

    val routes: HttpHandler = routes(
            "/echo" bind POST to echo,
            "/twitter" bind routes(
                    "/tweet" bind POST to tweetHandler
            )
    )

    val server = echo.asServer(Netty(9000)).start()


    val client:HttpHandler = JavaHttpClient()

    val resp = client(Request(POST, "http://localhost:9000/echo")
            .body("hello"))


    class EchoTest {
        @Test
        fun `should echoes input`() {
            val input = Request(POST, "/echo").body("Hello!")
            val expected = Response(OK).body("Hello!")

            expectThat(echo(input)).isEqualTo(expected)
        }
    }




    {"station":"FR*SOD*12345",
        "media":"1234ABCD",
        "datetime":"2018-11-09T16:03:25+01:00[Europe/Paris]",
        "duration":1234,"energy":5432}

    [{"datetime":"2018-11-09T16:03:25+01:00[Europe/Paris]",
        "amount":2.25,"currency":"EUR"}]