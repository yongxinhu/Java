import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;


/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /** Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc. */

    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     *
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        nodes = new HashMap<>();
        nameMap = new HashMap<>();
        tire = new Trie();
        blocks = new Block[CUTS][CUTS];

        double y = MapServer.ROOT_ULLAT;
        for (int i = 0; i < CUTS; i++, y += DY) {
            double x = MapServer.ROOT_ULLON;
            for (int j = 0; j < CUTS; j++, x += DX) {
                blocks[i][j] = new Block(x, y);
            }
        }
        try {
            File inputFile = new File(dbPath);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputFile, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
        makeBlockSystem();
    }

    HashMap<Long, Node> nodes;
    HashMap<String, LinkedList<Node>> nameMap;
    Trie tire;
    private final int CUTS = 64;
    private final double MARGIN = 0.026;
    private final double LEFT = MapServer.ROOT_ULLON - MARGIN;
    private final double RIGHT = MapServer.ROOT_LRLON + MARGIN;
    private final double UP = MapServer.ROOT_ULLAT + MARGIN;
    private final double DOWN = MapServer.ROOT_LRLAT - MARGIN;
    private final double DX = (RIGHT - LEFT) / CUTS;
    private final double DY = (DOWN - UP) / CUTS;
    private final double R = Math.min(Math.abs(DX), Math.abs(DY));
    private Block[][] blocks;


    class Block {
        ArrayList<Node> nodes;
        double ulx, uly;

        Block(double ulx, double uly) {
            nodes = new ArrayList<>();
            this.ulx = ulx;
            this.uly = uly;
        }

        long findClosest(double x, double y, double r) {
            long id = 0;
            double minDis = 99999, t;
            for (Node n : nodes) {
                t = distance(n, x, y);
                if (t <= r && t < minDis) {
                    id = n.id;
                    minDis = t;
                }
            }
            return id;
        }
    }

    class Node {
        double lat, lon;
        long id;
        ArrayList<Long> adjIds;
        //ArrayList<Edge> adj;
        boolean connectedToGraph;
        String name;

        Node(long id, double lon, double lat) {
            this.id = id;
            this.lat = lat;
            this.lon = lon;
            adjIds = new ArrayList<>();
            //adj = new ArrayList<>();
        }

        @Override
        public int hashCode() {
            return (int) id;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Node) {
                return ((Node) o).id == id;
            }
            return false;
        }
    }

    /*
        class Edge {
            Node nodeTo;
            long from, to;
            double distance, maxSpeed;


            Edge(long from, long to, double maxSpeed) {
                this.from = from;
                this.to = to;
                nodeTo = nodes.get(to);
                distance = distance(from, to);
                this.maxSpeed = maxSpeed;
            }

            @Override
            public boolean equals(Object o) {
                if (o instanceof Edge) {
                    Edge e = (Edge) o;
                    return e.from == from && e.to == to;
                }
                return false;
            }
        }
    */
    private void makeBlockSystem() {
        for (Node n : nodes.values()) {
            double x = n.lon, y = n.lat;
            int blockX = (int) ((x - LEFT) / (RIGHT - LEFT) * CUTS);
            int blockY = (int) ((y - UP) / (DOWN - UP) * CUTS);
            if (blockX >= 0 && blockX < CUTS && blockY >= 0 && blockY < CUTS) {
                blocks[blockX][blockY].nodes.add(n);
            } else {
                System.out.println(blockX + " " + blockY);
            }
        }
    }


    void addNode(Long id, double lat, double lon) {
        if (!nodes.containsKey(id)) {
            Node n = new Node(id, lat, lon);
            nodes.put(id, n);
        }
    }

    void addEdge(long from, long to, boolean oneWay) {
        if (from != to && nodes.containsKey(from) && nodes.containsKey(to)) {
            //Edge e = new Edge(from, to, maxSpeed);
            nodes.get(from).adjIds.add(to);
            //nodes.get(from).adj.add(e);
            nodes.get(from).connectedToGraph = true;
            nodes.get(to).connectedToGraph = true;
            if (!oneWay) {
                //Edge e2 = new Edge(to, from, maxSpeed);
                nodes.get(to).adjIds.add(from);
                //nodes.get(to).adj.add(e2);
            }
        }
    }

    void addLocation(long id, String cleanName, String realName) {
        Node n = nodes.get(id);
        n.name = realName;
        if (!nameMap.containsKey(cleanName)) {
            nameMap.put(cleanName, new LinkedList<>());
        }
        nameMap.get(cleanName).addFirst(n);
        tire.put(cleanName, n);
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     *
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     * Remove nodes with no connections from the graph.
     * While this does not guarantee that any two nodes in the remaining graph are connected,
     * we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        // Your code here.

        ArrayList<Long> removeList = new ArrayList<>();
        for (Node n : nodes.values()) {
            if (!n.connectedToGraph) {
                removeList.add(n.id);
            }
        }
        for (long id : removeList) {
            nodes.remove(id);
        }
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     */
    Iterable<Long> vertices() {
        //YOUR CODE HERE, this currently returns only an empty list.
        return nodes.keySet();
    }

    /**
     * Returns ids of all vertices adjacent to v.
     */
    Iterable<Long> adjacent(long v) {
        return nodes.get(v).adjIds;
    }

    /**
     * Returns the distance in units of longitude between vertices v and w.
     */
    /**
     * Returns the Euclidean distance between vertices v and w, where Euclidean distance
     * is defined as sqrt( (lonV - lonV)^2 + (latV - latV)^2 ).
     */
    double distance(long v, long w) {
        double dx = nodes.get(v).lon - nodes.get(w).lon;
        double dy = nodes.get(v).lat - nodes.get(w).lat;
        return Math.sqrt(dx * dx + dy * dy);
    }

    double distance(Node n, double x, double y) {
        double dx = n.lon - x;
        double dy = n.lat - y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Returns the vertex id closest to the given longitude and latitude.
     */
    long closest(double lon, double lat) {
        lon = forceInRange(lon, LEFT, RIGHT);
        lat = forceInRange(lat, DOWN, UP);
        int blockX = (int) ((lon - LEFT) / (RIGHT - LEFT) * CUTS);
        int blockY = (int) ((UP - lat) / (UP - DOWN) * CUTS);
        blockX = forceInRange(blockX, 0, CUTS);
        blockY = forceInRange(blockY, 0, CUTS);
        int b = 1;
        double r = R;
        while (true) {
            int left = forceInRange(blockX - b, 0, CUTS);
            int right = forceInRange(blockX + b, 0, CUTS);
            int up = forceInRange(blockY - b, 0, CUTS);
            int down = forceInRange(blockY + b, 0, CUTS);
            long close = searchInBlocks(lon, lat, r, left, right, up, down);
            if (close != 0) {
                return close;
            }
            b *= 2;
            r = b * R;
        }
    }

    private long searchInBlocks(double x0, double y0, double r,
                                int left, int right, int up, int down) {
        long id = 0, tid = 0;
        double min = 999999, t;
        for (int i = left; i <= right; i++) {
            for (int j = up; j <= down; j++) {
                tid = blocks[i][j].findClosest(x0, y0, r);
                if (tid != 0) {
                    t = distance(nodes.get(tid), x0, y0);
                    if (t < min) {
                        min = t;
                        id = tid;
                    }
                }
            }
        }
        return id;
    }

    private int forceInRange(int n, int low, int upper) {
        if (n < low) {
            n = low;
        } else if (n >= upper) {
            n = upper - 1;
        }
        return n;
    }

    private double forceInRange(double n, double low, double upper) {
        if (n < low) {
            n = low;
        } else if (n > upper) {
            n = upper;
        }
        return n;
    }


    /**
     * Longitude of vertex v.
     */
    double lon(long v) {
        return nodes.get(v).lon;
    }

    /**
     * Latitude of vertex v.
     */
    double lat(long v) {
        return nodes.get(v).lat;
    }

    class Trie {
        TrieNode root;

        Trie() {
            root = new TrieNode();
        }

        boolean containsKey(String key) {
            TrieNode p = root;
            int i;
            for (i = 0; i < key.length() && p.childs.containsKey(key.charAt(i)); i++) {
                p = p.childs.get(key.charAt(i));
            }
            return i == key.length();
        }

        LinkedList<Node> getExact(String key) {
            TrieNode p = root;
            int i;
            for (i = 0; i < key.length() && p.childs.containsKey(key.charAt(i)); i++) {
                p = p.childs.get(key.charAt(i));
            }
            if (i < key.length() || p == null) {
                return new LinkedList<>();
            }
            LinkedList<Node> result = new LinkedList<>();
            if (p.stuff != null) {
                result.addAll(p.stuff);
            }
            return result;
        }

        LinkedList<Node> get(String key) {
            TrieNode p = root;
            int i;
            for (i = 0; i < key.length() && p.childs.containsKey(key.charAt(i)); i++) {
                p = p.childs.get(key.charAt(i));
            }
            if (i < key.length()) {
                return new LinkedList<>();
            }
            LinkedList<Node> result = new LinkedList<>();
            collectAll(p, result);
            return result;
        }

        private void collectAll(TrieNode node, LinkedList<Node> result) {
            if (node == null) {
                return;
            }
            if (node.stuff != null) {
                result.addAll(node.stuff);
            }
            for (TrieNode n : node.childs.values()) {
                collectAll(n, result);
            }
        }



        private TrieNode addKey(char[] key) {
            TrieNode p = root;
            int i;
            for (i = 0; i < key.length && p.childs.containsKey(key[i]); i++) {
                p = p.childs.get(key[i]);
            }
            for (; i < key.length; i++) {
                char k = key[i];
                p.childs.put(k, new TrieNode());
                p = p.childs.get(k);
            }
            return p;
        }

        void put(String key, Node n) {
            TrieNode p = addKey(key.toCharArray());
            if (p.stuff == null) {
                p.stuff = new LinkedList<>();
            }
            p.stuff.add(n);
        }

        private class TrieNode {
            HashMap<Character, TrieNode> childs;
            LinkedList<Node> stuff;

            TrieNode() {
                childs = new HashMap<>();
            }
        }
    }
}
