package com.github.industrialcraft.scrapbox.server;

/*
 * Convex Separator for libGDX Box2D
 *
 * Made by https://github.com/rakam
 * This class is a libGDX version of the Antoan Angelov's work.
 * It is designed to work with Erin Catto's Box2D physics library.
 *
 * Everybody can use this software for any purpose, under two restrictions:
 * 1. You cannot claim that you wrote this software.
 * 2. You can not remove or alter this notice.
 *
 */
//from: https://gist.github.com/rackaam/3180cb02a6579e263cae


import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ShortArray;

import java.util.ArrayList;

public class Box2DSeparator {

    /**
     * Separates a non-convex polygon into convex polygons and adds them as fixtures to the <code>body</code> parameter.<br/>
     * There are some rules you should follow (otherwise you might get unexpected results) :
     * <ul>
     * <li>This class is specifically for non-convex polygons. If you want to create a convex polygon, you don't need to use this class - Box2D's <code>b2PolygonShape</code> class allows you to create convex shapes with the <code>setAsArray()</code>/<code>setAsVector()</code> method.</li>
     * <li>The vertices must be in clockwise order.</li>
     * <li>No three neighbouring points should lie on the same line segment.</li>
     * <li>There must be no overlapping segments and no "holes".</li>
     * </ul> <p/>
     *
     * @param body        The b2Body, in which the new fixtures will be stored.
     * @param fixtureDef  A b2FixtureDef, containing all the properties (friction, density, etc.) which the new fixtures will inherit.
     * @param verticesVec The vertices of the non-convex polygon, in clockwise order.
     */
    public static void separate(Body body, FixtureDef fixtureDef, ArrayList<Vector2> verticesVec) {
        Array<Vector2> vec = new Array<Vector2>();
        Array<Array<Vector2>> figsVec;
        PolygonShape polyShape;

        for (Vector2 vector2 : verticesVec) vec.add(vector2.cpy());

        figsVec = calcShapes(vec);

        ArrayList<ArrayList<Vector2>> triangles = new ArrayList<>();

        for(Array<Vector2> convex : figsVec){
            float[] points = new float[convex.size * 2];
            for(int i = 0;i < convex.size;i++){
                points[i*2] = convex.get(i).x;
                points[(i*2)+1] = convex.get(i).y;
            }
            ShortArray array = new DelaunayTriangulator().computeTriangles(points, true);
            for(int i = 0;i < (array.size/3);i++){
                ArrayList<Vector2> vertices = new ArrayList<>();
                vertices.add(convex.get(array.get((i*3))));
                vertices.add(convex.get(array.get((i*3)+1)));
                vertices.add(convex.get(array.get((i*3)+2)));
                triangles.add(vertices);
            }
        }


        for (ArrayList<Vector2> vertices : triangles) {
            polyShape = new PolygonShape();
            polyShape.set(vertices.toArray(Vector2[]::new));
            fixtureDef.shape = polyShape;
            body.createFixture(fixtureDef);
        }
    }

    /**
     * Checks whether the vertices in <code>verticesVec</code> can be properly distributed into the new fixtures (more specifically, it makes sure there are no overlapping segments and the vertices are in clockwise order).
     * It is recommended that you use this method for debugging only, because it may cost more CPU usage.
     *
     * @param verticesVec The vertices to be validated.
     * @return An integer which can have the following values:
     * <ul>
     * <li>0 if the vertices can be properly processed.</li>
     * <li>1 If there are overlapping lines.</li>
     * <li>2 if the points are <b>not</b> in clockwise order.</li>
     * <li>3 if there are overlapping lines <b>and</b> the points are <b>not</b> in clockwise order.</li>
     * </ul>
     */
    public static int validate(Array<Vector2> verticesVec) {
        int i, n = verticesVec.size, j, j2, i2, i3;
        float d;
        int ret = 0;
        boolean fl, fl2 = false;

        for (i = 0; i < n; i++) {
            i2 = (i < n - 1 ? i + 1 : 0);
            i3 = (i > 0 ? i - 1 : n - 1);

            fl = false;
            for (j = 0; j < n; j++) {
                if (j != i && j != i2) {
                    if (!fl) {
                        d = det(verticesVec.get(i).x, verticesVec.get(i).y, verticesVec.get(i2).x, verticesVec.get(i2).y, verticesVec.get(j).x, verticesVec.get(j).y);
                        if (d > 0) fl = true;
                    }

                    if (j != i3) {
                        j2 = (j < n - 1 ? j + 1 : 0);
                        if (hitSegment(verticesVec.get(i).x, verticesVec.get(i).y, verticesVec.get(i2).x, verticesVec.get(i2).y, verticesVec.get(j).x, verticesVec.get(j).y, verticesVec.get(j2).x, verticesVec.get(j2).y) != null)
                            ret = 1;
                    }
                }
            }

            if (!fl) fl2 = true;
        }

        if (fl2) {
            if (ret == 1) ret = 3;
            else ret = 2;
        }

        return ret;
    }


    private static Array<Array<Vector2>> calcShapes(Array<Vector2> verticesVec) {
        Array<Vector2> vec;
        int i, n, j;
        float d, t, dx, dy, minLen;
        int i1, i2, i3;
        Vector2 p1, p2, p3;
        int j1, j2;
        Vector2 v1, v2;
        int k = 0, h = 0;
        Array<Vector2> vec1, vec2;
        Vector2 v, hitV = null;
        boolean isConvex;
        Array<Array<Vector2>> figsVec, queue;

        figsVec = new Array<Array<Vector2>>();
        queue = new Array<Array<Vector2>>();
        queue.add(verticesVec);

        while (queue.size > 0) {
            vec = queue.get(0);
            n = vec.size;
            isConvex = true;

            for (i = 0; i < n; i++) {
                i1 = i;
                i2 = (i < n - 1 ? i + 1 : i + 1 - n);
                i3 = (i < n - 2 ? i + 2 : i + 2 - n);

                p1 = vec.get(i1);
                p2 = vec.get(i2);
                p3 = vec.get(i3);

                d = det(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y);
                if (d < 0) {
                    isConvex = false;
                    minLen = Float.MAX_VALUE;

                    for (j = 0; j < n; j++) {
                        if (j != i1 && j != i2) {
                            j1 = j;
                            j2 = (j < n - 1 ? j + 1 : 0);

                            v1 = vec.get(j1);
                            v2 = vec.get(j2);

                            v = hitRay(p1.x, p1.y, p2.x, p2.y, v1.x, v1.y, v2.x, v2.y);

                            if (v != null) {
                                dx = p2.x - v.x;
                                dy = p2.y - v.y;
                                t = dx * dx + dy * dy;

                                if (t < minLen) {
                                    h = j1;
                                    k = j2;
                                    hitV = v;
                                    minLen = t;
                                }
                            }
                        }
                    }

                    if (minLen == Float.MAX_VALUE) err();

                    vec1 = new Array<Vector2>();
                    vec2 = new Array<Vector2>();

                    j1 = h;
                    j2 = k;
                    v1 = vec.get(j1);
                    v2 = vec.get(j2);

                    if (!pointsMatch(hitV.x, hitV.y, v2.x, v2.y)) vec1.add(hitV);
                    if (!pointsMatch(hitV.x, hitV.y, v1.x, v1.y)) vec2.add(hitV);

                    h = -1;
                    k = i1;
                    while (true) {
                        if (k != j2) vec1.add(vec.get(k));
                        else {
                            if (h < 0 || h >= n) err();
                            if (!isOnSegment(v2.x, v2.y, vec.get(h).x, vec.get(h).y, p1.x, p1.y))
                                vec1.add(vec.get(k));
                            break;
                        }

                        h = k;
                        if (k - 1 < 0) k = n - 1;
                        else k--;
                    }

                    vec1.reverse();

                    h = -1;
                    k = i2;
                    while (true) {
                        if (k != j1) {
                            vec2.add(vec.get(k));
                        } else {
                            if (h < 0 || h >= n) err();
                            if (k == j1 && !isOnSegment(v1.x, v1.y, vec.get(h).x, vec.get(h).y, p2.x, p2.y))
                                vec2.add(vec.get(k));
                            break;
                        }
                        h = k;
                        if (k + 1 > n - 1) k = 0;
                        else k++;
                    }

                    queue.add(vec1);
                    queue.add(vec2);
                    queue.removeIndex(0);

                    break;
                }
            }
            if (isConvex) {
                figsVec.add(queue.removeIndex(0));
            }
        }

        return figsVec;
    }


    private static Vector2 hitRay(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        float t1 = x3 - x1;
        float t2 = y3 - y1;
        float t3 = x2 - x1;
        float t4 = y2 - y1;
        float t5 = x4 - x3;
        float t6 = y4 - y3;
        float t7 = t4 * t5 - t3 * t6;
        float a = (t5 * t2 - t6 * t1) / t7;
        float px = x1 + a * t3;
        float py = y1 + a * t4;
        boolean b1 = isOnSegment(x2, y2, x1, y1, px, py);
        boolean b2 = isOnSegment(px, py, x3, y3, x4, y4);

        if (b1 && b2) return new Vector2(px, py);

        return null;
    }


    private static Vector2 hitSegment(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        float t1 = x3 - x1;
        float t2 = y3 - y1;
        float t3 = x2 - x1;
        float t4 = y2 - y1;
        float t5 = x4 - x3;
        float t6 = y4 - y3;
        float t7 = t4 * t5 - t3 * t6;
        float a = (t5 * t2 - t6 * t1) / t7;
        float px = x1 + a * t3;
        float py = y1 + a * t4;
        boolean b1 = isOnSegment(px, py, x1, y1, x2, y2);
        boolean b2 = isOnSegment(px, py, x3, y3, x4, y4);

        if (b1 && b2) return new Vector2(px, py);

        return null;
    }


    private static boolean isOnSegment(float px, float py, float x1, float y1, float x2, float y2) {
        boolean b1 = ((x1 + 0.1 >= px && px >= x2 - 0.1) || (x1 - 0.1 <= px && px <= x2 + 0.1));
        boolean b2 = ((y1 + 0.1 >= py && py >= y2 - 0.1) || (y1 - 0.1 <= py && py <= y2 + 0.1));
        return (b1 && b2 && isOnLine(px, py, x1, y1, x2, y2));
    }


    private static boolean pointsMatch(float x1, float y1, float x2, float y2) {
        float dx = (x2 >= x1 ? x2 - x1 : x1 - x2);
        float dy = (y2 >= y1 ? y2 - y1 : y1 - y2);
        return (dx < 0.1 && dy < 0.1);
    }


    private static boolean isOnLine(float px, float py, float x1, float y1, float x2, float y2) {
        if (x2 - x1 > 0.1 || x1 - x2 > 0.1) {
            float a = (y2 - y1) / (x2 - x1);
            float possibleY = a * (px - x1) + y1;
            float diff = (possibleY > py ? possibleY - py : py - possibleY);
            return diff < 0.1f;
        }

        return (px - x1 < 0.1 || x1 - px < 0.1);
    }


    private static float det(float x1, float y1, float x2, float y2, float x3, float y3) {
        return x1 * y2 + x2 * y3 + x3 * y1 - y1 * x2 - y2 * x3 - y3 * x1;
    }


    private static void err() {
        throw new Error("A problem has occurred. Use the Validate() method to see where the problem is.");
    }
}
