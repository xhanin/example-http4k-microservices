import ehm.accountmanager.AccountManager
import ehm.chargepricer.ChargePricer
import ehm.consumptions.ConsumptionManager
import ehm.core.EVChargeManager
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    val servers = listOf(
            AccountManager.server(),
            ChargePricer.server(),
            ConsumptionManager.server(),
            EVChargeManager.default().server()
    )

    val time = measureTimeMillis {
        servers.forEach {
            it.start()
        }
    }

    println("""
===========================================================================
    .__     __    __            _____  __
    |  |___/  |__/  |_______   /  |  ||  | __
    |  |  \   __\   __\____ \ /   |  ||  |/ /
    |   Y  \  |  |  | |  |_> >    ^   /    <
    |___|  /__|  |__| |   __/\____   ||__|_ \
         \/           |__|        |__|     \/

    ${servers.size} servers started in $time ms
===========================================================================
    """)
}