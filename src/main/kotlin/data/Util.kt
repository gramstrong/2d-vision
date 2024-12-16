package vision.data

import data.Point
import javafx.scene.shape.Rectangle
import vision.dev.Logger
import kotlin.math.PI
import kotlin.math.atan2

class Util
{
    companion object
    {
        private fun getCorners(x: Double, y: Double, width: Double, height: Double): Array<Point>
        {
            return arrayOf(
                Point(x, y),
                Point(x, y + height),
                Point(x + width, y),
                Point(x + width, y + height))
        }

        private fun getRectangleSegments(corners: Array<Point>): List<Segment>
        {
            try {
                assert(corners.size == 4)
            } catch (e: Exception) {
                Logger.error("Expected array of Points size 4")
            }

            return listOf(
                Segment(corners[0].x, corners[0].y, corners[2].x, corners[2].y),
                Segment(corners[0].x, corners[0].y, corners[1].x, corners[1].y),
                Segment(corners[2].x, corners[2].y, corners[3].x, corners[3].y),
                Segment(corners[1].x, corners[1].y, corners[3].x, corners[3].y),
            )
        }

        fun processSegments(source: Point, segments: List<Segment>): List<Segment>
        {
            for (segment in segments) {
                updatePointMeta(source, segment)
            }
            return segments
        }

        fun updatePointMeta(source: Point, segment: Segment)
        {
            //dx from segment midpoint to source
            val dx = 0.5 * (segment.p1.point.x + segment.p2.point.x) - source.x
            //dy from segment midpoint to source
            val dy = 0.5 * (segment.p1.point.y + segment.p2.point.y) - source.y

            segment.d = (dx * dx) + (dy * dy)
            segment.p1.angle = atan2(segment.p1.point.y - source.y, segment.p1.point.x - source.x)
            segment.p2.angle = atan2(segment.p2.point.y - source.y, segment.p2.point.x - source.x)

            var dAngle = segment.p2.angle - segment.p1.angle
            if (dAngle <= - PI) dAngle += 2 * PI
            if (dAngle > PI) dAngle -= 2 * PI

            segment.p1.beginsSegment = dAngle > 0;
            segment.p2.beginsSegment = !segment.p1.beginsSegment;
        }

        fun rectangleToSegments(rectangle: Rectangle): List<Segment>
        {
            return getRectangleSegments(getCorners(rectangle.x, rectangle.y, rectangle.width, rectangle.height))
        }
    }
}

