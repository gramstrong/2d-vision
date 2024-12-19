package vision.data

import data.Point

class SegmentPoint (
    val x: Double,
    val y: Double,
    var beginsSegment: Boolean,
    val segment: Segment,
    var angle: Double
)