

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Queue;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    // Recommended: QuadTree instance variable. You'll need to make
    //              your own QuadTree since there is no built-in quadtree in Java.

    /**
     * imgRoot is the name of the directory containing the images.
     * You may not actually need this for your class.
     */
    public Rasterer(String imgRoot) {
        // YOUR CODE HERE
        imgDir = imgRoot;
        layers = new ArrayList<>(MAXDEPTH + 1);
        for (int i = 0; i < MAXDEPTH + 1; i++) {
            layers.add(new Tile[POW2[i]][POW2[i]]);
        }
        buffer = new LinkedList<>();

        root = buildQuadTreeHelper("", MAXDEPTH,
                MapServer.ROOT_ULLON, MapServer.ROOT_ULLAT,
                MapServer.ROOT_LRLON, MapServer.ROOT_LRLAT,
                0, 0);
        lonDPP = new double[MAXDEPTH + 1];
        double firstDPP = (MapServer.ROOT_LRLON - MapServer.ROOT_ULLON) / 512;
        lonDPP[0] = 999999;
        lonDPP[1] = firstDPP;
        for (int i = 2; i < lonDPP.length; i++) {
            lonDPP[i] = lonDPP[i - 1] / 2;
        }
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     * <p>
     * <<<<<<< HEAD
     * The grid of images must obey the following properties, where image in the
     * grid is referred to as a "tile".
     * <ul>
     * <li>Has dimensions of at least w by h, where w and h are the user viewport width
     * and height.</li>
     * <li>The tiles collected must cover the most longitudinal distance per pixel
     * (LonDPP) possible, while still covering less than or equal to the amount of
     * longitudinal distance per pixel in the query box for the user viewport size. </li>
     * <li>Contains all tiles that intersect the query bounding box that fulfill the
     * above condition.</li>
     * <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     * </ul>
     * =======
     * The grid of images must obey the following properties, where image in the
     * grid is referred to as a "tile".
     * <ul>
     * <li>The tiles collected must cover the most longitudinal distance per pixel
     * (LonDPP) possible, while still covering less than or equal to the amount of
     * longitudinal distance per pixel in the query box for the user viewport size. </li>
     * <li>Contains all tiles that intersect the query bounding box that fulfill the
     * above condition.</li>
     * <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     * </ul>
     * >>>>>>> e5eb1deae982c391e100a8fe3837e97db9d60f20
     * </p>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     * @return A map of results for the front end as specified:
     * "render_grid"   -> String[][], the files to display
     * "raster_ul_lon" -> Number, the bounding upper left longitude of the rastered image <br>
     * "raster_ul_lat" -> Number, the bounding upper left latitude of the rastered image <br>
     * "raster_lr_lon" -> Number, the bounding lower right longitude of the rastered image <br>
     * "raster_lr_lat" -> Number, the bounding lower right latitude of the rastered image <br>
     * "depth"         -> Number, the 1-indexed quadtree depth of the nodes of the rastered image.
     * Can also be interpreted as the length of the numbers in the image
     * string. <br>
     * "query_success" -> Boolean, whether the query was able to successfully complete. Don't
     * forget to set this to true! <br>
     * @see REQUIRED_RASTER_REQUEST_PARAMS
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        double leftX = params.get("ullon");
        double upperY = params.get("ullat");
        double rightX = params.get("lrlon");
        double lowerY = params.get("lrlat");
        double w = params.get("w");
        double requiredDDP = (rightX - leftX) / w;
        int depth = properLevel(requiredDDP);
        Tile t1 = find(root, leftX, upperY, depth), t4 = find(root, rightX, lowerY, depth);
        for (Map<String, Object> r : buffer) {
            if (t1 == r.get("t1") && t4 == r.get("t4")) {
                return r;
            }
        }
        Map<String, Object> results = new HashMap<>();
        results.put("depth", depth);
        String[][] pic = new String[t4.n - t1.n + 1][t4.m - t1.m + 1];
        Tile[][] layer = layers.get(depth);
        for (int i = 0; i < pic.length; i++) {
            for (int j = 0; j < pic[0].length; j++) {
                pic[i][j] = layer[t1.n + i][t1.m + j].file;
            }
        }
        results.put("render_grid", pic);
        results.put("raster_ul_lon", t1.ulx);
        results.put("raster_ul_lat", t1.uly);
        results.put("raster_lr_lon", t4.lrx);
        results.put("raster_lr_lat", t4.lry);
        results.put("t1", t1);
        results.put("t4", t4);
        results.put("query_success", true);

        buffer.add(results);
        if (buffer.size() > 30) {
            buffer.remove();
        }

        return results;
    }

    private Queue<Map<String, Object>> buffer;


    private Tile find(Tile t, double x, double y, int depth) {
        if (depth == 0) {
            return t;
        }
        double midx = (t.lrx + t.ulx) / 2, midy = (t.lry + t.uly) / 2;
        if (x < midx && y >= midy) {
            return find(t.one, x, y, depth - 1);
        } else if (x >= midx && y >= midy) {
            return find(t.two, x, y, depth - 1);
        } else if (x < midx && y < midy) {
            return find(t.three, x, y, depth - 1);
        } else if (x >= midx && y < midy) {
            return find(t.four, x, y, depth - 1);
        }
        throw new RuntimeException("out of boundary :x =" + x + " , y =" + y);
    }

    private String imgDir;
    private Tile root;
    private double[] lonDPP;
    private ArrayList<Tile[][]> layers;
    private static final int[] POW2 = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024};
    private static final int MAXDEPTH = 7;

    private class Tile {
        private String file;
        private Tile one, two, three, four;
        private double ulx, uly, lrx, lry;
        private int n, m;

        Tile(String fileNum, double ulx, double uly, double lrx, double lry,
             int n, int m) {
            this.n = n;
            this.m = m;
            layers.get(fileNum.length())[n][m] = this;
            file = imgDir + fileNum + ".png";
            this.ulx = ulx;
            this.uly = uly;
            this.lrx = lrx;
            this.lry = lry;
        }
    }

    private Tile buildQuadTreeHelper(String fileName, int count,
                                     double ulx, double uly, double lrx, double lry,
                                     int n, int m) {
        Tile t = new Tile(fileName, ulx, uly, lrx, lry, n, m);
        if (count > 0) {
            double midx = (ulx + lrx) / 2, midy = (uly + lry) / 2;
            t.one = buildQuadTreeHelper(fileName + "1", count - 1,
                    ulx, uly, midx, midy, 2 * n, 2 * m);
            t.two = buildQuadTreeHelper(fileName + "2", count - 1,
                    midx, uly, lrx, midy, 2 * n, 2 * m + 1);
            t.three = buildQuadTreeHelper(fileName + "3", count - 1,
                    ulx, midy, midx, lry, 2 * n + 1, 2 * m);
            t.four = buildQuadTreeHelper(fileName + "4", count - 1,
                    midx, midy, lrx, lry, 2 * n + 1, 2 * m + 1);
        }
        return t;
    }

    private int properLevel(double requiredDPP) {
        for (int i = 1; i < lonDPP.length; i++) {
            if (lonDPP[i] <= requiredDPP) {
                return i;
            }
        }
        return lonDPP.length - 1;
    }


}
