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

        /**
         * @return A list of segments whose points have been updated with metadata related to the
         * source point
         * @param source The source point
         * @param segments The list of segments to update
         */
        fun processSegments(source: Point, segments: List<Segment>): List<Segment>
        {
            for (segment in segments) {
                updatePointMeta(source, segment)
            }
            return segments
        }

        /**
         * Updates a segment's points with metadata related to a source point
         *
         * @param source The source point
         * @param segment The segment to update
         */
        fun updatePointMeta(source: Point, segment: Segment)
        {
            //Set the angle from source to point, relative to the positive x-axis
            segment.p1.angle = atan2(segment.p1.y - source.y, segment.p1.x - source.x)
            segment.p2.angle = atan2(segment.p2.y - source.y, segment.p2.x - source.x)

            //Get the angle delta betwen p2 and p1
            var dAngle = segment.p2.angle - segment.p1.angle

            //Normalize the angle delta between -PI and PI
            if (dAngle <= - PI) dAngle += 2 * PI
            if (dAngle > PI) dAngle -= 2 * PI

            //If the angle delta is greater than zero, we will consider P1 the "start" of the segment.
            //This is the direction (clockwise) in which the player "sweeps" segments.
            segment.p1.beginsSegment = dAngle > 0;
            segment.p2.beginsSegment = !segment.p1.beginsSegment;
        }

        fun rectangleToSegments(rectangle: Rectangle): List<Segment>
        {
            return getRectangleSegments(getCorners(rectangle.x, rectangle.y, rectangle.width, rectangle.height))
        }
    }
}

