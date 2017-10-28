
import java.util.LinkedList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.Comparator;

/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. Start by using Dijkstra's, and if your code isn't fast enough for your
 * satisfaction (or the autograder), upgrade your implementation by switching it to A*.
 * Your code will probably not be fast enough to pass the autograder unless you use A*.
 * The difference between A* and Dijkstra's is only a couple of lines of code, and boils
 * down to the priority you use to order your vertices.
 */
public class Router {
    /**
     * Return a LinkedList of <code>Long</code>s representing the shortest path from st to dest,
     * where the longs are node IDs.
     */
    private static class Pair {
        long key;
        double value;
        double traveled;

        Pair(long key, double value, double traveled) {
            this.key = key;
            this.value = value;
            this.traveled = traveled;
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof Pair) && ((Pair) o).key == key;
        }

        @Override
        public int hashCode() {
            return (int) key;
        }
    }

    private static class AStarComparator implements Comparator<Pair> {
        @Override
        public int compare(Pair a, Pair b) {
            if (a.value > b.value) {
                return 1;
            } else if (a.value == b.value) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    public static LinkedList<Long>
        shortestPath(GraphDB g, double stlon, double stlat, double destlon, double destlat) {
        LinkedList<Long> path = new LinkedList<>();
        long start = g.closest(stlon, stlat);
        long goal = g.closest(destlon, destlat);
        PriorityQueue<Pair> pq = new PriorityQueue<>(new AStarComparator());
        HashSet<Long> marked = new HashSet<>();
        HashMap<Long, Long> edgeTo = new HashMap<>();
        HashMap<Long, Double> distTo = new HashMap<>();
        pq.add(new Pair(start, g.distance(start, goal), 0));
        distTo.put(start, 0.0);
        edgeTo.put(start, start);
        Pair current = new Pair(0, 0, 0);
        while (!pq.isEmpty()) {
            current = pq.poll();
            if (current.key == goal) {
                break;
            }
            if (marked.contains(current.key)) {
                continue;
            }
            marked.add(current.key);
            for (long v : g.adjacent(current.key)) {
                double stepDis = g.distance(current.key, v);
                double upToV = current.traveled + stepDis;
                double goalDis = g.distance(v, goal);
                if (!distTo.containsKey(v)) {
                    distTo.put(v, upToV);
                    edgeTo.put(v, current.key);
                    pq.add(new Pair(v, upToV + goalDis, upToV));
                } else if (distTo.get(v) > upToV) {
                    distTo.put(v, upToV);
                    edgeTo.put(v, current.key);
                    Pair p = new Pair(v, upToV + goalDis, upToV);
                    pq.remove(p);
                    pq.add(p);
                }
            }
        }
        long p = current.key;
        if (p == goal) {
            do {
                path.addFirst(p);
                p = edgeTo.get(p);
            } while (p != start);
            path.addFirst(p);
        }
        return path;
    }

    public static void main(String[] args) {
        GraphDB g = new GraphDB("berkeley.osm");
        double stlon = -122.2410200526445;
        double stlat = 37.8757099036705;
        double edlon = -122.24154947437555;
        double edlat = 37.875803001382806;
        GraphDB.Node s1 = g.nodes.get((long) 35719135);
        GraphDB.Node s2 = g.nodes.get((long) 1294168686);
        double d1 = g.distance(s1, stlon, stlat);
        //double d2 = g.distance(s2,stlon,stlat);
        LinkedList<Long> path = shortestPath(g, stlon, stlat, edlon, edlat);
    }

}


