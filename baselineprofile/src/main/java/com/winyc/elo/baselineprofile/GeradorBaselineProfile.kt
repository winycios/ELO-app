package com.winyc.elo.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWit

@RunWith(AndroidJUnit4::class)
class GeradorBaselineProfile {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun gerar() = rule.collect(
        packageName = PACOTE_APP,
        includeInStartupProfile = true,
    ) {
        pressHome()
        startActivityAndWait()
        device.waitForIdle(ESPERA_MS)

        repeat(3) {
            rolar(de = 0.75, para = 0.25)
            device.waitForIdle(ESPERA_MS)
        }

        repeat(2) {
            device.swipe(
                (device.displayWidth * 0.8).toInt(),
                device.displayHeight / 2,
                (device.displayWidth * 0.2).toInt(),
                device.displayHeight / 2,
                PASSOS_GESTO,
            )
            device.waitForIdle(ESPERA_MS)
        }

        repeat(3) { rolar(de = 0.25, para = 0.75) }
        device.waitForIdle(ESPERA_MS)
        device.click(device.displayWidth / 2, (device.displayHeight * 0.62).toInt())
        device.waitForIdle(ESPERA_MS)

        repeat(2) {
            rolar(de = 0.75, para = 0.25)
            device.waitForIdle(ESPERA_MS)
        }
        device.pressBack()
        device.waitForIdle(ESPERA_MS)
    }

    private fun androidx.benchmark.macro.MacrobenchmarkScope.rolar(de: Double, para: Double) {
        device.swipe(
            device.displayWidth / 2,
            (device.displayHeight * de).toInt(),
            device.displayWidth / 2,
            (device.displayHeight * para).toInt(),
            PASSOS_GESTO,
        )
    }

    private companion object {
        const val PACOTE_APP = "com.winyc.elo"
        const val ESPERA_MS = 3_000L
        const val PASSOS_GESTO = 20
    }
}
