import org.kalasim.*

// can components be extended into data classes? Yes they can.

data class Foo(val bar: String): Component(){
    override fun process(): Sequence<Component> {
        return super.process()
    }
}

fun main() {
    ConsoleTraceLogger.setColumnWidth(ConsoleTraceLogger.TraceTableColumn.time, 30)

    createSimulation(true) {
        Foo("").apply {
            println(toString())
        }



    }.run(10)
}