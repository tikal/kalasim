package org.kalasim.test.models

import io.kotest.matchers.shouldBe
import org.junit.Test
import org.kalasim.examples.er.EmergencyRoom
import org.kalasim.test.testModel
import kotlin.time.Duration.Companion.days

class ERTests {

    @Test
    fun `it should save some patients deterministically`() = testModel(EmergencyRoom()){
        waitingLine.sizeTimeline.enabled = true
        waitingLine.sizeTimeline.printSummary()

        run(2.days)

        treatedMonitor[now] shouldBe 94
        deceasedMonitor[now] shouldBe 12
        waitingLine.sizeTimeline[now] shouldBe 11
    }
}


