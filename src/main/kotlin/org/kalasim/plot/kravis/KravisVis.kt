package org.kalasim.plot.kravis

import jetbrains.letsPlot.label.ggtitle
import krangl.*
import kravis.*
import kravis.device.JupyterDevice
import org.kalasim.*
import org.kalasim.monitors.*
import java.awt.GraphicsEnvironment

internal fun canDisplay() = !GraphicsEnvironment.isHeadless() && hasR()

fun hasR(): Boolean {
    val rt = Runtime.getRuntime()
    val proc = rt.exec("R --help")
    proc.waitFor()
    return proc.exitValue() == 0
}

internal fun checkDisplay() {
    if (!canDisplay()) {
        throw IllegalArgumentException(" No display or R not found")
    }
}

internal fun printWarning(msg: String) {
    System.err.println("[kalasim] $msg")
}

private fun GGPlot.showNotJupyter(): GGPlot = also {
    if (SessionPrefs.OUTPUT_DEVICE !is JupyterDevice) {
        checkDisplay()
        show()
    }
}


fun MetricTimeline.display(title: String = name, from: TickTime? = null, to: TickTime? = null): GGPlot {
    val data = stepFun()
        .filter { from == null || it.first >= from.value }
        .filter { to == null || it.first <= to.value }

    return data.plot(
        x = Pair<Double, Double>::first,
        y = Pair<Double, Double>::second
    ).xLabel("time")
        .yLabel("")
        .geomStep()
        .title(title)
        .showNotJupyter()
}


fun NumericStatisticMonitor.display(title: String = name): GGPlot {
    val data = values.toList()

    return data.plot(x = { it })
        .geomHistogram()
        .title(title)
        .showNotJupyter()
}

fun <T> FrequencyTable<T>.display(title: String? = null): GGPlot {
    val data = toList()

    return data.plot(x = { it.first }, y = { it.second })
        .geomCol()
        .run { if (title != null) title(title) else this }
        .showNotJupyter()
}


fun <T> CategoryTimeline<T>.display(
    title: String = name,
    forceTickAxis: Boolean = false,
): GGPlot {
    val nlmStatsData = statsData()
    val data = nlmStatsData.stepFun()

    data class Segment<T>(val value: T, val start: Double, val end: Double)

    val segments = data.zipWithNext().map {
        Segment(
            it.first.second,
            it.first.first,
            it.second.first
        )
    }

    // why cant we use "x".asDiscreteVariable here?
    return segments.plot(
        x = Segment<T>::start,
        y = Segment<T>::value,
        xend = Segment<T>::end,
        yend = Segment<T>::value
    )
        .xLabel("time")
        .yLabel("")
        .geomSegment()
        .geomPoint()
        .title(title)
        .showNotJupyter()
}

//
// resources
//

fun List<ResourceActivityEvent>.display(title: String?,
                                        forceTickAxis: Boolean = false,
): GGPlot {
    val useWT = any { it.startWT != null } && !forceTickAxis

    return plot(y = { resource.name },
        yend = { resource.name },
        x = { if (useWT) startWT else start },
        xend = { if (useWT) endWT else end },
        color = { activity ?: "Other" })
        .geomSegment(size = 10.0)
        .yLabel("")
        .also { if (title != null) ggtitle(title) }
}


/**
 * @param forceTickAxis Even if a tick-transformation is defined, the x axis will show tick-times
 */
fun List<ResourceTimelineSegment>.display(
    title: String?,
    exclude: List<ResourceMetric> = listOf(
        ResourceMetric.Capacity,
        ResourceMetric.Occupancy,
        ResourceMetric.Availability
    ),
    forceTickAxis: Boolean = false,
): GGPlot {
    val useWT = any { it.startWT != null } && !forceTickAxis
    return filter { it.metric !in exclude }
        .plot(x = { if (useWT) startWT else start }, y = { value }, color = { metric })
        .geomStep()
        .facetWrap("color", ncol = 1, scales = FacetScales.free_y)
        .also { if (title != null) ggtitle(title) }
}


//
// Components

fun Component.display(
    title: String = statusTimeline.name,
    forceTickAxis: Boolean = false,
): GGPlot = statusTimeline.display(title = title, forceTickAxis = forceTickAxis)


fun List<Component>.displayStateTimeline(
    title: String? = null,
    componentName: String = "component",
    forceTickAxis: Boolean = false,
): GGPlot {
//    val df = csTimelineDF(componentName)
    val df = clistTimeline()

    val useWT = first().tickTransform !=null && !forceTickAxis
    fun wtTransform(tt: TickTime) = if(useWT) first().env.asWallTime(tt) else  tt

    return df.plot(y = { first.name }, yend = { first.name }, x = { wtTransform(TickTime(second.timestamp))})
        .geomStep()
        .also { if (title != null) ggtitle(title) }
        .xLabel(componentName)
}

//private fun List<Component>.csTimelineDF(componentName: String) = map { eqn ->
//    eqn.statusTimeline
//        .statsData().asList()
//        .asDataFrame().addColumn(componentName) { eqn }
//}.bindRows().rename("value" to "state")



fun List<Component>.displayStateProportions(
    title: String? = null,
): GGPlot {
    val df = clistTimeline()

    return df.plot(y = { first.name }, fill = {second.value}, weight = { second.duration})
        .geomBar(position = PositionFill())
        .also { if (title != null) ggtitle(title) }
}


// not needed because displayStateProportions works so much better here
//fun List<Component>.displayStateHeatmap(
//    title: String? = null,
//    componentName: String = "component",
//    forceTickAxis: Boolean = false,
//): GGPlot {
//    val df = clistTimeline()
//
//    val dfUnfold = df.asDataFrame().unfold<LevelStateRecord<ComponentState>>("second").rename("first" to "component")
//
//    val durationSummary = dfUnfold
//        .groupBy("state", "component")
//        .summarize("total_duration" `=` { it["duration"] })
//
//    val propMatrix = durationSummary
//        .groupBy("component")
//        .addColumn("total_dur_prop"){ it["total_duration"]/ it["total_duration"].sum()!! }
//
//    return propMatrix.plot("component", y="state", fill="total_dur_prop")
//        .geomTile()
//        .also { if (title != null) ggtitle(title) }
//}

private fun List<Component>.clistTimeline() = flatMap { eqn ->
    eqn.statusTimeline
        .statsData().asList().map { eqn to it }
}

