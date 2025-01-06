package vision.data

import data.Point
import javafx.scene.shape.Rectangle
import kotlin.math.PI
import kotlin.math.atan2

private class Corners(val NW: Point, val SW: Point, val NE: Point, val SE: Point);

class Util
{
    companion object
    {
        /**
         * @return A [Corners] object given the parameters of a rectangle.
         */
        private fun getCorners(x: Double, y: Double, width: Double, height: Double): Corners
        {
            return Corners(
                Point(x, y),
                Point(x, y + height),
                Point(x + width, y),
                Point(x + width, y + height))
        }

        /**
         * @return a list of [Segment] given a [Corners] object representing a rectangle.
         */
        private fun getRectangleSegments(corners: Corners): List<Segment>
        {
            val cornersToSegments = {NW: Point, SW: Point, NE: Point, SE: Point->
                listOf(
                    Segment(NW.x, NW.y, NE.x, NE.y),
                    Segment(NW.x, NW.y, SW.x, SW.y),
                    Segment(NE.x, NE.y, SE.x, SE.y),
                    Segment(SW.x, SW.y, SE.x, SE.y))
            }

            return cornersToSegments(corners.NW, corners.SW, corners.NE, corners.SE);
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
         * Updates a segment's points with metadata related to a source point.
         *
         * @param source The source point.
         * @param segment The segment to update.
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

        /**
         * @return A list of [Segment]
         * @param rectangle from which the segments should be derived.
         */
        fun rectangleToSegments(rectangle: Rectangle): List<Segment>
        {
            return getRectangleSegments(getCorners(rectangle.x, rectangle.y, rectangle.width, rectangle.height))
        }

        /**
         * Comparison function given two [SegmentPoint].
         * Sorts based on metadata that is set by [Util.updatePointMeta]. This gives us a list of [SegmentPoint]s
         * in clockwise order.
         */
        fun pointCompare(pointA: SegmentPoint, pointB: SegmentPoint): Int {
            return when {
                pointA.angle > pointB.angle -> 1
                pointA.angle < pointB.angle -> -1
                !pointA.beginsSegment && pointB.beginsSegment -> 1
                pointA.beginsSegment && !pointB.beginsSegment -> -1
                else -> 0
            }
        }

        /**
         * @return true if [segmentA] lies between [origin] and [segmentB], otherwise returns false.
         * @param segmentA
         * @param segmentB
         * @param origin
         */
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
    }
}

