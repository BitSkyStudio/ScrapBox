package com.github.industrialcraft.scrapbox.server;

import clipper2.Clipper;
import clipper2.core.FillRule;
import clipper2.core.PathD;
import clipper2.core.PathsD;
import clipper2.core.PointD;
import clipper2.offset.EndType;
import clipper2.offset.JoinType;
import com.badlogic.gdx.math.Vector2;
import micycle.jsimplex.generator.NoiseSurface;

import java.util.*;

public class WorldGenerator {
    public static PathsD generateMap(){
        float frequency = 0.07f;
        int width = 256;
        int height = 192;
        float scale = 4;
        float[][] densityMap = unPack2D(NoiseSurface.generate2dRaw(0, 0, width, height, frequency, true), width, height);
        for(int x = 1;x < width - 1;x++){
            for(int y = 1;y < height - 1;y++){
                float squircleVal = (float) (Math.pow(((x-width/2f)/width)*2f, 4) + Math.pow(((y-height/2f)/height)*2f, 4));
                densityMap[y][x] = densityMap[y][x] - ((y/(float)height)*1.2f-0.6f) - Math.max(0f, squircleVal-0.5f)*4;
            }
        }
        for(int i = 0;i < height;i++){
            densityMap[i][0] = 0f;
            densityMap[i][width-1] = 0f;
        }
        for(int i = 0;i < width;i++){
            densityMap[0][i] = 0f;
            densityMap[height-1][i] = 0f;
        }
        ArrayList<ArrayList<MarchPoint>> paths = new ArrayList<>();
        //PathsD pathsD = new PathsD();
        for(int x = 0;x < width-1; x++){
            for(int y = 0;y < height-1; y++){
                float v0 = densityMap[y][x];
                float v1 = densityMap[y][x + 1];
                float v2 = densityMap[y + 1][x + 1];
                float v3 = densityMap[y + 1][x];

                float ta = findLerpFactor(v0, v1);
                float tb = findLerpFactor(v1, v2);
                float tc = findLerpFactor(v3, v2);
                float td = findLerpFactor(v0, v3);

                MarchPoint pa = new MarchPoint(x*2+1, y*2, new Vector2(x + ta, y));
                MarchPoint pb = new MarchPoint(x*2+2, y*2+1, new Vector2(x + 1, y + tb));
                MarchPoint pc = new MarchPoint(x*2+1, y*2+2, new Vector2(x + tc, y + 1));
                MarchPoint pd = new MarchPoint(x*2, y*2+1, new Vector2(x, y + td));
                MarchPoint[] points = new MarchPoint[]{pa, pb, pc, pd};

                int state = getState(v0, v1, v2, v3);
                for(EdgePair edge : STATES[state]){
                    MarchPoint a = points[edge.a];
                    MarchPoint b = points[edge.b];
                    /*for(int i = 0;i < 8;i++) {
                        Vector2 point = a.interpolated.cpy().lerp(b.interpolated, i/5f);
                        pathsD.add(Clipper.Ellipse(new PointD(point.x, point.y), 0.5, 0.5, 4));
                    }*/
                    ArrayList<MarchPoint> pathA = paths.stream().filter(path -> path.contains(a)).findAny().orElse(null);
                    ArrayList<MarchPoint> pathB = paths.stream().filter(path -> path.contains(b)).findAny().orElse(null);
                    if(pathA == null && pathB == null){
                        paths.add(new ArrayList<>(Arrays.asList(a, b)));
                    } else if(pathA != null && pathB != null){
                        if(pathA == pathB){
                            ArrayList<MarchPoint> activePath = pathA;
                            if(activePath.get(0).equals(a)){
                                activePath.add(0, b);
                            } else if(activePath.get(0).equals(b)){
                                activePath.add(0, a);
                            } else if(activePath.get(activePath.size()-1).equals(a)){
                                activePath.add(b);
                            } else if(activePath.get(activePath.size()-1).equals(b)){
                                activePath.add(a);
                            } else {
                                throw new RuntimeException();
                            }
                        } else {
                            if(pathA.get(0).equals(a)){
                                Collections.reverse(pathA);
                            }
                            if(!pathB.get(0).equals(b)){
                                Collections.reverse(pathB);
                            }
                            pathA.addAll(pathB);
                            paths.remove(pathB);
                        }
                    } else {
                        ArrayList<MarchPoint> activePath = pathA == null ? pathB : pathA;
                        if(activePath.get(0).equals(a)){
                            activePath.add(0, b);
                        } else if(activePath.get(0).equals(b)){
                            activePath.add(0, a);
                        } else if(activePath.get(activePath.size()-1).equals(a)){
                            activePath.add(b);
                        } else if(activePath.get(activePath.size()-1).equals(b)){
                            activePath.add(a);
                        } else {
                            throw new RuntimeException();
                        }
                    }
                }
            }
        }
        PathsD pathsD = new PathsD();
        for (ArrayList<MarchPoint> path : paths) {
            PathD pathD = new PathD();
            for (MarchPoint marchPoint : path) {
                Vector2 interpolated = marchPoint.interpolated;
                pathD.add(new PointD((interpolated.x - width/2f) * scale, interpolated.y * scale - 200));
            }
            pathsD.add(pathD);
        }
        //System.out.println(pathsD.size());
        return Clipper.SimplifyPaths(Clipper.Union(pathsD, FillRule.EvenOdd), Terrain.simplifyEpsilon);
    }
    private static float[][] unPack2D(float[] noise, int width, int height) {
        float[][] unpacked = new float[height][width];

        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                unpacked[y][x] = noise[y * width + x];
            }
        }

        return unpacked;
    }
    private static float findLerpFactor(float v0, float v1){
        float t = -v0/(v1-v0);
        return Math.max(Math.min(t, 0.9f), 0.1f);
    }
    private static int getState(float a, float b, float c, float d){
        int aVal = a > 0 ? 1:0;
        int bVal = b > 0 ? 1:0;
        int cVal = c > 0 ? 1:0;
        int dVal = d > 0 ? 1:0;
        return aVal*8 + bVal*4 + cVal*2 + dVal;
    }
    private static EdgePair[][] STATES = new EdgePair[][]{
        new EdgePair[]{},
        new EdgePair[]{new EdgePair(2, 3)},
        new EdgePair[]{new EdgePair(1,2)},
        new EdgePair[]{new EdgePair(1,3)},
        new EdgePair[]{new EdgePair(0, 1)},
        new EdgePair[]{new EdgePair(0, 3), new EdgePair(1, 2)},
        new EdgePair[]{new EdgePair(0, 2)},
        new EdgePair[]{new EdgePair(0, 3)},
        new EdgePair[]{new EdgePair(0, 3)},
        new EdgePair[]{new EdgePair(0, 2)},
        new EdgePair[]{new EdgePair(0, 1), new EdgePair(2, 3)},
        new EdgePair[]{new EdgePair(0, 1)},
        new EdgePair[]{new EdgePair(1, 3)},
        new EdgePair[]{new EdgePair(1, 2)},
        new EdgePair[]{new EdgePair(2, 3)},
        new EdgePair[]{},
    };
    public static class EdgePair{
        public final int a;
        public final int b;
        public EdgePair(int a, int b) {
            this.a = a;
            this.b = b;
        }
    }
    public static class MarchPoint{
        public final int gridX;
        public final int gridY;
        public final Vector2 interpolated;
        public MarchPoint(int gridX, int gridY, Vector2 interpolated) {
            this.gridX = gridX;
            this.gridY = gridY;
            this.interpolated = interpolated;
        }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof MarchPoint)) return false;
            MarchPoint that = (MarchPoint) o;
            return gridX == that.gridX && gridY == that.gridY;
        }
        @Override
        public int hashCode() {
            return Objects.hash(gridX, gridY);
        }
    }
}
