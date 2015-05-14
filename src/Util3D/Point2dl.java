package Util3D;

/**
 * Created by Mikko on 14.5.2015.
 */

public class Point2dl {
    public final long x;
    public final long y;
    public Point2dl(final long xin, final long yin){
        x = xin; y = yin;
    }

    public Point2dl add(final long xin, final long yin){return new Point2dl(x + xin,  y+ yin);}
    public Point2dl add(final Point2dl p){return new Point2dl(x + p.x,  y+ p.y);}

    public Point2dl sub(final long xin, final long yin){return new Point2dl(x - xin,  y- yin);}
    public Point2dl sub(final Point2dl p){return new Point2dl(x - p.x,  y- p.y);}

    public boolean equals(final Point2dl p){ return x == p.x && y == p.y;}

    public String toString(){return "" + x + ", " + y;}
}
