package ehm.accountmanager

import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class AccountManagerTest {
    @Test
    fun `should get account by media`() {
        val resp = AccountManager.app(Request(Method.GET, "/media/1234ABCD/account"))

        expectThat(resp) {
            get{status}.isEqualTo(Status.OK)
            get{bodyString()}.isEqualTo("""{"id":"123","fullName":"John Doe","currency":"EUR","tariffRef":"F1"}""")
        }
    }
    @Test
    fun `should account by media not found return 404`() {
        val resp = AccountManager.app(Request(Method.GET, "/media/4567/account"))

        expectThat(resp) {
            get{status}.isEqualTo(Status.NOT_FOUND)
        }
    }
}