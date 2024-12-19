package vision.data

import data.Point

class Segment(val x1: Double, val y1: Double, val x2: Double, val y2: Double) {
    var p1 = SegmentPoint(x1, y1, false, this, 0.0);
    var p2 = SegmentPoint(x2, y2, false, this, 0.0)
}