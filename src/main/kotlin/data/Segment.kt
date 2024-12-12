package vision.data

import data.Point

class Segment(val x1: Double, val y1: Double, val x2: Double, val y2: Double) {
    var p1 = Point(x1, y1);
    var p2 = Point(x2, y2);
    var d = 0;

    init {
        p1.segment = this;
        p2.segment = this;
    }
}