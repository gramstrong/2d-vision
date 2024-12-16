package vision.data

import data.Point

class PointMeta (
    val point: Point,
    var beginsSegment: Boolean,
    val segment: Segment,
    var angle: Double
)