package vision

import data.Point
import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.stage.Stage
import vision.data.SegmentPoint
import vision.data.Segment
import vision.data.Util
import kotlin.math.cos
import kotlin.math.sin


class Vision : Application() {
    val canvas = Canvas(500.0, 500.0)
    var room = Rectangle(100.0, 100.0, 300.0, 300.0)
    var boxes = arrayOf(
        Rectangle(150.0, 150.0, 75.0, 75.0),
        Rectangle(290.0, 260.0, 30.0, 75.0),
    )

    val radius = 10.0

    override fun start(primaryStage: Stage) {
        canvas.onMouseMoved = EventHandler { event: MouseEvent ->
            render(Point(event.x, event.y), canvas)
        }

        val root = Pane(canvas);
        val scene = Scene(root, 500.0, 500.0)
        primaryStage.scene = scene
        primaryStage.show()
    }

    fun render(mousePosition: Point, canvas: Canvas)
    {
        val diameter = radius * 2.0;
        val gc = canvas.graphicsContext2D

        gc.clearRect(0.0, 0.0, canvas.getWidth(), canvas.getHeight());

        //Segments for the room
        var totalSegments = Util.processSegments(mousePosition, Util.rectangleToSegments(room))

        //Segments for each box
        for(box in boxes) {
            totalSegments = totalSegments + Util.processSegments(mousePosition, Util.rectangleToSegments(box))
        }

        gc.lineWidth = 2.0

        //Draw vision tris
        drawTriPoints(mousePosition, calculateVisibility(mousePosition, totalSegments), gc)

        //Draw the room
        gc.fill = Color.TRANSPARENT
        gc.stroke = Color.BLACK;
        gc.strokeRect(room.x, room.y, room.width, room.height);

        //Draw each box
        for(box in boxes)
        {
            gc.strokeRect(box.x, box.y, box.width, box.height);
        }

        // Drawing the "player". The edge of the drawing starts at the provided Point,
        // so we subtract the radius in order to position the circle over the mouse position.
        gc.fill = Color.BLACK
        gc.fillOval(mousePosition.x-radius, mousePosition.y-radius, diameter, diameter)
    }

    fun drawTriPoints(animPos: Point, triPoints: List<List<Point>>, gc: GraphicsContext)
    {
        for(tri in triPoints)
        {
            val p1 = tri.get(0);
            val p2 =  tri.get(1);
            val p3 =  animPos;
            gc.fill = Color.LIGHTGOLDENRODYELLOW
            gc.stroke = Color.BLACK
            gc.fillPolygon(doubleArrayOf(p1.x, p2.x, p3.x), doubleArrayOf(p1.y, p2.y, p3.y), 3)
            gc.strokePolygon(doubleArrayOf(p1.x, p2.x, p3.x), doubleArrayOf(p1.y, p2.y, p3.y), 3)
        }
    }

    fun pointCompare(pointA: SegmentPoint, pointB: SegmentPoint): Int {
        return when {
            pointA.angle > pointB.angle -> 1
            pointA.angle < pointB.angle -> -1
            !pointA.beginsSegment && pointB.beginsSegment -> 1
            pointA.beginsSegment && !pointB.beginsSegment -> -1
            else -> 0
        }
    }

    fun getSortedSegPoints(segments: List<Segment>): List<SegmentPoint>
    {
        val points = segments.flatMap{ listOf(it.p1, it.p2) }
        return points.sortedWith(this::pointCompare)
    }

    fun segmentInFrontOf(segmentA: Segment, segmentB: Segment, origin: Point): Boolean {

        val leftOf: (Segment, Point) -> Boolean = { segment, point ->
            val crossProduct = (segment.p2.x - segment.p1.x) * (point.y - segment.p1.y) -
                    (segment.p2.y - segment.p1.y) * (point.x - segment.p1.x)
            crossProduct < 0
        }

        val interpolate: (SegmentPoint, SegmentPoint, Double) -> Point = { pointA, pointB, f ->
            Point(
                pointA.x * (1 - f) + pointB.x * f,
                pointA.y * (1 - f) + pointB.y * f
            )
        }

        val A1 = leftOf(segmentA, interpolate(segmentB.p1, segmentB.p2, 0.01))
        val A2 = leftOf(segmentA, interpolate(segmentB.p2, segmentB.p1, 0.01))
        val A3 = leftOf(segmentA, origin)
        val B1 = leftOf(segmentB, interpolate(segmentA.p1, segmentA.p2, 0.01))
        val B2 = leftOf(segmentB, interpolate(segmentA.p2, segmentA.p1, 0.01))
        val B3 = leftOf(segmentB, origin)

        return when {
            B1 == B2 && B2 != B3 -> true
            A1 == A2 && A2 == A3 -> true
            A1 == A2 && A2 != A3 -> false
            B1 == B2 && B2 == B3 -> false
            else -> false
        }
    }

    fun calculateVisibility(origin: Point, endpoints: List<Segment>): List<List<Point>> {
        val openSegments = mutableListOf<Segment>()
        val output = mutableListOf<List<Point>>()
        var beginAngle = 0.0

        val sortedSegPoints = getSortedSegPoints(endpoints);

        for (pass in 0..1) {
            for (segPoint in sortedSegPoints) {
                val openSegment = openSegments.firstOrNull()

                if (segPoint.beginsSegment) {
                    var index = 0
                    var segment = openSegments.getOrNull(index)
                    while (segment != null && segmentInFrontOf(segPoint.segment, segment, origin)) {
                        index++
                        segment = openSegments.getOrNull(index)
                    }

                    if (segment == null) {
                        openSegments.add(segPoint.segment)
                    } else {
                        openSegments.add(index, segPoint.segment)
                    }
                } else {
                    val index = openSegments.indexOf(segPoint.segment)
                    if (index >= 0) {
                        openSegments.removeAt(index)
                    }
                }

                if (openSegment != openSegments.firstOrNull()) {
                    if (pass == 1) {
                        val trianglePoints = getTrianglePoints(origin, beginAngle, segPoint.angle, openSegment)
                        output.add(trianglePoints)
                    }
                    beginAngle = segPoint.angle
                }
            }
        }

        return output
    }

    fun getTrianglePoints(origin: Point, angle1: Double, angle2: Double, segment: Segment?): List<Point> {
        var p1 = origin
        var p2 = Point(origin.x + cos(angle1), origin.y + sin(angle1))
        var p3 = Point(0.0, 0.0)
        var p4 = Point(0.0, 0.0)

        if (segment != null) {
            p3.x = segment.p1.x
            p3.y = segment.p1.y
            p4.x = segment.p2.x
            p4.y = segment.p2.y
        } else {
            p3.x = origin.x + cos(angle1) * 200
            p3.y = origin.y + sin(angle1) * 200
            p4.x = origin.x + cos(angle2) * 200
            p4.y = origin.y + sin(angle2) * 200
        }

        val pBegin = lineIntersection(p3, p4, p1, p2)
        p2.x = origin.x + cos(angle2)
        p2.y = origin.y + sin(angle2)
        val pEnd = lineIntersection(p3, p4, p1, p2)

        return listOf(pBegin, pEnd)
    }

    fun lineIntersection(point1: Point, point2: Point, point3: Point, point4: Point): Point {
        val s = (
                (point4.x - point3.x) * (point1.y - point3.y) -
                        (point4.y - point3.y) * (point1.x - point3.x)
                ) / (
                (point4.y - point3.y) * (point2.x - point1.x) -
                        (point4.x - point3.x) * (point2.y - point1.y)
                )

        val x = point1.x + s * (point2.x - point1.x)
        val y = point1.y + s * (point2.y - point1.y)

        return Point(x, y)
    }
}

fun main(args: Array<String>) {
    Application.launch(Vision::class.java, *args)
}