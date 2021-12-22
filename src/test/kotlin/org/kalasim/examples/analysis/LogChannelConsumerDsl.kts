//LogChannelConsumerDsl.kt
import org.kalasim.*

createSimulation {
    ComponentGenerator(iat = constant(1)) { Component("Car.${it}") }

    // add custom log consumer
    addAsyncEventListener<InteractionEvent> { event ->
        if (event.curComponent?.name == "ComponentGenerator.1")
            println(event)
    }

    // run the simulation
    run(10)
}