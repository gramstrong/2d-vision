package vision

import data.Point
import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.stage.Stage


class Vision : Application() {

    private var position: Point = Point(Double.NaN,Double.NaN)
    private val radius = 10.0

    override fun start(primaryStage: Stage) {
        val canvas = Canvas(400.0, 300.0)

        canvas.onMouseMoved = EventHandler { event: MouseEvent ->
            render(Point(event.x-radius, event.y-radius), canvas)
        }

        val root = StackPane(canvas)
        val scene = Scene(root, 400.0, 300.0)
        primaryStage.scene = scene
        primaryStage.show()
    }

    fun render(point: Point, canvas: Canvas)
    {
        Thread()
        {
            val diameter = radius * 2.0
            val gc = canvas.graphicsContext2D
            gc.clearRect(position.x, position.y, diameter, diameter)

            // Draw a circle at (x, y)
            gc.fillOval(point.x, point.y, diameter, diameter)
            position = point
        }.start()
    }
}

fun main(args: Array<String>) {
    Application.launch(Vision::class.java, *args)
}