package com.classming.cluster;

import com.classming.Vector.LevenshteinDistance;
import com.classming.util.OfflineClusterUtil;
import fj.P;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBScanCluster implements Cluster {

    private List<Point> points;
    private int[][] distance;

    private final int DBScan_radius;
    private final int DBScan_minPts;

    public DBScanCluster() {
        this(2, 2);
    }

    public DBScanCluster(int r, int m) {
        this.DBScan_minPts = m;
        this.DBScan_radius = r;
    }

    @Override
    public ClusterResult getClusterInfo(Object object) {
        ClusterResult result = new ClusterResult();

        if (points == null)
            loadPoints("./tmpRecord/");
        if (distance == null)
            loadDistance();
        List<Point> cores = new ArrayList<>();
        //find cores
        int len = points.size();
        for (int i = 0; i < len; i++) {
            Point point = points.get(i);
            int cnt = 0;
            for (int j = 0; j < len; j++) {
                Point other = points.get(j);
                if (point != other && distance[i][j] < DBScan_radius)
                    cnt++;
            }
            if (cnt >= DBScan_minPts)
                cores.add(point);
        }

        //set groups
        int id = 0;
        for (Point p : cores) {
            if (p.visited)
                continue;
            p.clusterId = ++id;
            densityConnected(p, id, points, cores);
        }

        Map<Integer, Integer> map = new HashMap<>();
        for (Point p : points) {
            int clusterId = p.clusterId;
            int cnt = map.getOrDefault(clusterId, 0);
            map.put(clusterId, cnt + 1);
        }

        result.clusterList = new ArrayList<>(map.values());
        return result;
    }

    private void loadDistance() {
        System.out.println("calculating distance");
        int len = points.size();
        int total = len * len;
        double cnt = 0;
        distance = new int[len][len];
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                Point a = points.get(i);
                Point b = points.get(j);
                distance[i][j] = LevenshteinDistance.computeLevenshteinDistance(a.instructions, b.instructions);
                cnt++;
            }
            System.out.printf("load distance %.3f%%\n", cnt * 100 / total);
        }
        System.out.println("calculating finished");
    }

    private void loadPoints(String path) {
        points = new ArrayList<>();
        List<List<String>> lists = OfflineClusterUtil.getPureInstructions(path);
        for (int i = 0; i < lists.size(); i++) {
            points.add(new Point(i, lists.get(i)));
        }
    }

    private void densityConnected(Point center, int id, List<Point> points, List<Point> cores) {
        center.visited = true;
        for (Point point : points) {
            if (point.visited)
                continue;
            if (point.distanceTo(center) < DBScan_radius) {
                point.clusterId = id;
                point.visited = true;
                if (cores.contains(point))
                    densityConnected(point, id, points, cores);
            }
        }
    }

    private static class Point {
        public List<String> instructions;
        public boolean visited;
        public int clusterId;
        public int pointId;

        public Point(int pointId, List<String> instructions) {
            this.instructions = instructions;
            this.visited = false;
            this.clusterId = 0;
            this.pointId = pointId;
        }

        public int distanceTo(Point other) {
            return LevenshteinDistance.computeLevenshteinDistance(this.instructions, other.instructions);
        }
    }
}
