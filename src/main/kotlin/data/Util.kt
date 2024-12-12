package vision.data

import data.Point

fun getCorners(x: Double, y: Double, width: Double, height: Double): Array<Point>
{
    return arrayOf(Point(x, y), Point(x + width, y + height), Point(x, y + height))
}