package edu.uci.ics.asterix.api.java;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import edu.uci.ics.hyracks.api.util.StopWatch;

public class SpatialIndexEvaluatorForSyntheticData10CopyQueryOnly {

    private static double circleRadius = 0.00000025; // around 25mm
    private static int numQuery = 200;
    private static StopWatch sw = new StopWatch();
    private static Random rand;
    private static String datasetName;
    private static int coordCircleIdx = 0;
    private static String ipAddress = "127.0.0.1";
    private static String portNum = "19002";
    private static String indexType;
    private static String workType;
    private static int numDatasetCopy = 10;
    private static int targetDatasetNum = 5;
    private static int joinIdx = 0;
    private static boolean isJoin = false;

    /* 200 points returned from the following AQL query:
    use dataverse STBench;
    for $x in dataset FsqCheckinTweet
    where $x.id % int64("400000") = 0
    return $x.coordinates
     */
    public static final double[][] coordinates = new double[][] { { 177.188238, -90.0 }, { 146.3451281, -38.3775451 },
            { 143.754741, -36.867605 }, { 138.62736, -34.67428 }, { 150.09351, -33.62857 },
            { 116.8588489, -29.9427749 }, { 152.965699, -26.556816 }, { 106.8199301, -6.4826538 },
            { 31.894557, -26.430565 }, { 55.414458, -21.090953 }, { -72.5746154, -38.7229843 },
            { -65.193434, -26.834847 }, { -79.270906, -3.699129 }, { -58.178857, 6.761247 }, { -71.849508, 18.495198 },
            { -16.8421519, 28.2450293 }, { -106.5599551, 32.3702523 }, { -118.274601, 34.126714 },
            { -3.557378, 36.717228 }, { -122.4443988, 37.7742776 }, { -77.198812, 38.926179 },
            { -83.1885639, 39.8125611 }, { -73.9705279, 40.6143147 }, { -8.1792249, 41.6551799 },
            { -6.639371, 42.40465 }, { -72.91526, 43.09901 }, { -77.167457, 43.957652 }, { -122.840738, 45.480481 },
            { -0.111058, 47.2641599 }, { -123.494461, 48.46485 }, { -97.0788965, 49.8540529 }, { -3.154046, 50.82219 },
            { -0.3371483, 51.07903 }, { -2.962055, 51.337588 }, { -2.4966316, 51.48775 }, { -0.597554, 51.675219 },
            { -1.545809, 51.940483 }, { -0.016727, 52.204258 }, { -1.817691, 52.443448 }, { -1.2204217, 52.6631383 },
            { -1.8194642, 52.9915831 }, { -4.172312, 53.258599 }, { -6.2239817, 53.4148267 }, { -9.304934, 53.76419 },
            { -1.820626, 54.307506 }, { -1.368436, 54.933407 }, { -4.7667933, 55.944305 }, { -4.1799333, 57.4815667 },
            { -20.1484966, 65.5951112 }, { 0.9932817, 48.39076 }, { 0.6110659, 51.9696731 }, { 4.27507, 39.812857 },
            { 1.634857, 42.519532 }, { 3.4580682, 43.6188815 }, { 9.9593547, 44.1142333 }, { 5.59337, 44.843991 },
            { 6.2484939, 45.2373729 }, { 9.9014333, 45.606615 }, { 6.711653, 45.986402 }, { 9.763358, 46.42901 },
            { 8.4239499, 46.8409866 }, { 9.5232238, 47.1633834 }, { 8.3417383, 47.396725 }, { 7.7642387, 47.5925159 },
            { 8.963046, 47.823296 }, { 8.377629, 48.094109 }, { 8.623129, 48.421813 }, { 2.344158, 48.624352 },
            { 2.345908, 48.776644 }, { 8.940035, 48.895147 }, { 9.47618, 49.044323 }, { 8.0559942, 49.2390508 },
            { 8.288106, 49.416147 }, { 6.0819413, 49.5921298 }, { 6.563402, 49.805341 }, { 7.946738, 49.9853959 },
            { 8.082097, 50.112984 }, { 9.381281, 50.230869 }, { 3.865922, 50.418768 }, { 3.915114, 50.581843 },
            { 7.265265, 50.695606 }, { 5.4721583, 50.79961 }, { 6.43517, 50.908728 }, { 9.8382267, 51.0284045 },
            { 7.0445166, 51.1573216 }, { 7.112647, 51.243227 }, { 6.834405, 51.3364317 }, { 6.773622, 51.450241 },
            { 7.659137, 51.585497 }, { 5.843748, 51.785684 }, { 9.24334, 51.951673 }, { 4.354537, 52.079375 },
            { 6.904968, 52.228667 }, { 4.802059, 52.348662 }, { 6.975319, 52.575857 }, { 6.561705, 53.010305 },
            { 9.618892, 53.310066 }, { 9.7290416, 53.7278066 }, { 8.319517, 54.926186 }, { 9.921694, 57.075117 },
            { 9.6146366, 62.4318999 }, { 77.9310645, 13.2436603 }, { 97.001807, 20.7888709 }, { 101.684218, 2.9147 },
            { 34.84679, 31.9997633 }, { 24.8818884, 35.0495395 }, { 20.895335, 37.742155 }, { 19.754637, 40.575653 },
            { 12.485857, 41.850764 }, { 22.9516656, 42.5164224 }, { 76.8909019, 43.2274058 },
            { 12.9379754, 43.6318626 }, { 16.470022, 44.001886 }, { 11.2895402, 44.3544244 }, { 34.321804, 44.565136 },
            { 38.807956, 44.886232 }, { 38.990057, 45.072342 }, { 21.4306983, 45.325655 }, { 27.7196512, 45.5059104 },
            { 21.200135, 45.7223467 }, { 22.1882367, 45.8571817 }, { 18.1224016, 46.1375283 },
            { 18.7551544, 46.3889142 }, { 24.5545528, 46.5411006 }, { 14.4960233, 46.7024833 },
            { 18.8937729, 46.9401072 }, { 15.45296, 47.1033767 }, { 11.83767, 47.256004 }, { 13.680321, 47.404 },
            { 19.051639, 47.512231 }, { 14.498095, 47.623722 }, { 10.317685, 47.728124 }, { 10.2450618, 47.8585624 },
            { 16.2625265, 48.0314028 }, { 10.5690666, 48.1399549 }, { 16.3243105, 48.2137516 }, { 12.71273, 48.34063 },
            { 35.354469, 48.511345 }, { 15.900162, 48.707651 }, { 12.5808994, 48.8990909 }, { 20.1394699, 49.1173133 },
            { 11.0941357, 49.3478674 }, { 11.017762, 49.594143 }, { 73.067153, 49.804336 }, { 14.588408, 49.992746 },
            { 18.6339639, 50.1524511 }, { 30.283251, 50.336639 }, { 19.2300533, 50.466645 }, { 39.7539333, 50.68527 },
            { 15.3302859, 50.8287277 }, { 13.643513, 50.945204 }, { 14.6492547, 51.0833127 }, { 51.39056, 51.218143 },
            { 11.603275, 51.444054 }, { 10.1331637, 51.6537622 }, { 10.2754849, 51.8099849 },
            { 10.1488236, 52.0194552 }, { 11.463673, 52.181176 }, { 76.954291, 52.308012 }, { 10.8303172, 52.4509929 },
            { 41.493736, 52.589905 }, { 15.2561848, 52.7401485 }, { 14.276522, 52.995592 }, { 10.288701, 53.180739 },
            { 22.8965783, 53.324375 }, { 12.425403, 53.474239 }, { 12.2414179, 53.6434476 }, { 52.502521, 53.824543 },
            { 59.699215, 53.956882 }, { 13.379252, 54.098873 }, { 27.54138, 54.260798 }, { 39.291737, 54.40374 },
            { 39.711772, 54.596496 }, { 20.49575, 54.68375 }, { 20.497446, 54.737114 }, { 83.098392, 54.856575 },
            { 73.289932, 54.980991 }, { 32.840589, 55.092611 }, { 61.332588, 55.194469 }, { 62.666088, 55.350712 },
            { 37.6083641, 55.5160942 }, { 40.695033, 55.617124 }, { 37.509235, 55.6631566 },
            { 37.3968833, 55.7004632 }, { 37.4704567, 55.727725 }, { 37.594135, 55.752665 },
            { 37.4523299, 55.7813399 }, { 49.098365, 55.817817 }, { 41.775742, 55.873875 }, { 37.377223, 55.955284 },
            { 46.7868, 56.036872 }, { 89.367966, 56.144703 }, { 41.32653, 56.238754 }, { 44.015395, 56.318263 },
            { 84.9818882, 56.4601698 }, { 84.740837, 56.671787 }, { 26.2121666, 56.8558 }, { 14.703227, 57.061233 },
            { 67.117241, 57.519295 }, { 68.106361, 57.884609 }, { 172.556905, -43.536002 }, { 145.305921, -38.032783 },
            { 148.19314, -36.32112 }, { 150.498362, -34.332169 }, { 116.074741, -33.379904 },
            { 153.4040833, -28.0460167 }, { 147.5915866, -23.797925 }, { 18.468112, -33.945613 },
            { 31.78369, -26.249016 }, { 16.470961, -19.035172 }, { -72.006025, -34.39262 }, { -50.623149, -23.177613 },
            { -46.1852465, -0.9263157 }, { -74.1717959, 8.8185775 }, { -72.6999919, 19.1067818 },
            { -17.8311101, 28.6407431 }, { -17.2351617, 32.7749033 }, { -118.70334, 34.58015 },
            { -86.3511667, 37.0097333 }, { -122.766149, 37.952703 }, { -109.1605333, 39.1322667 },
            { -86.156754, 39.970188 }, { -111.89807, 40.7566233 }, { -90.942539, 41.861748 },
            { -71.8378307, 42.5262849 }, { -2.165262, 43.285274 }, { -121.829391, 44.423414 },
            { -122.6450214, 45.5350311 }, { -1.075268, 47.57608 }, { -0.25524, 48.605993 }, { -4.9539341, 50.3059828 },
            { -0.547149, 50.860282 }, { -1.454695, 51.148667 }, { -2.878135, 51.3705117 }, { -3.496807, 51.513948 },
            { -1.277461, 51.739919 }, { -4.833693, 52.017576 }, { -113.8119331, 52.2609695 }, { -0.936367, 52.485841 },
            { -1.419335, 52.7301433 }, { -1.3996997, 53.0546907 }, { -2.254209, 53.299271 },
            { -113.584699, 53.461557 }, { -1.765734, 53.806554 }, { -3.0129093, 54.4336913 },
            { -1.5350246, 55.0758307 }, { -5.211823, 56.033703 }, { -3.9097373, 57.7818637 }, { 0.054976, 50.79543 },
            { 0.951093, 51.159322 }, { 0.1237034, 52.2288751 }, { 9.3087888, 40.8217621 }, { 2.0396615, 42.8858265 },
            { 5.36951, 43.71542 }, { 3.460454, 44.238065 }, { 6.223878, 44.997226 }, { 6.579345, 45.318615 },
            { 4.8463, 45.68804 }, { 7.168354, 46.0790557 }, { 6.6881819, 46.5056994 }, { 7.52381, 46.936498 },
            { 8.452585, 47.217748 }, { 8.3477983, 47.4299067 }, { 6.1670198, 47.6360321 }, { 8.029466, 47.887021 },
            { 9.739629, 48.153822 }, { 8.864007, 48.478294 }, { 8.587363, 48.661057 }, { 8.934858, 48.800963 },
            { 8.752885, 48.912002 }, { 7.676863, 49.091601 }, { 7.638486, 49.277341 }, { 8.3615446, 49.448082 },
            { 7.898291, 49.645391 }, { 6.265683, 49.841087 }, { 9.029379, 50.010539 }, { 8.5605333, 50.133495 },
            { 9.881013, 50.267718 }, { 9.125725, 50.459192 }, { 6.942688, 50.607714 }, { 5.776121, 50.719242 },
            { 2.66375, 50.818802 }, { 5.8847693, 50.929552 }, { 4.664252, 51.058733 }, { 6.883081, 51.177411 },
            { 7.049997, 51.262367 }, { 6.0008441, 51.3587038 }, { 6.28791, 51.473965 }, { 4.3690825, 51.6323733 },
            { 1.1526119, 51.8263333 }, { 8.397778, 51.9789701 }, { 5.1174544, 52.1051479 }, { 7.8483866, 52.2604233 },
            { 8.2203533, 52.3696433 }, { 9.883946, 52.640106 }, { 6.588738, 53.055093 }, { 9.8961715, 53.410305 },
            { 9.484021, 53.863846 }, { 9.9709383, 55.4411483 }, { 9.400484, 58.8316001 }, { 80.638431, 7.29346 },
            { 80.669966, 16.515157 }, { 58.6723009, 23.5389532 }, { 35.290442, 30.470036 }, { 34.801502, 32.140957 },
            { 23.81028, 35.52342 }, { 13.53423, 38.056825 }, { 14.349148, 40.844062 }, { 14.66141, 41.9182116 },
            { 21.1507792, 42.6537472 }, { 11.355085, 43.300718 }, { 16.672365, 43.699518 }, { 10.14665, 44.04874 },
            { 22.490313, 44.414632 }, { 11.3459246, 44.6373637 }, { 16.764463, 44.9417281 }, { 14.483394, 45.120115 },
            { 27.9280768, 45.3663168 }, { 18.715166, 45.553866 }, { 21.21594, 45.75376 }, { 13.047007, 45.904699 },
            { 21.37329, 46.184185 }, { 26.9218449, 46.4373016 }, { 15.360674, 46.5669226 }, { 23.4294716, 46.75082 },
            { 14.9754333, 46.9783767 }, { 18.0216509, 47.1350409 }, { 39.716614, 47.281513 },
            { 14.7161092, 47.4279556 }, { 19.0807132, 47.5300949 }, { 23.554367, 47.648341 }, { 12.432753, 47.753052 },
            { 19.9416119, 47.8917359 }, { 14.3600333, 48.0571783 }, { 11.588649, 48.156758 },
            { 16.3576769, 48.2331562 }, { 14.5088579, 48.3665459 }, { 12.1080636, 48.5489082 },
            { 21.2818266, 48.7387866 }, { 39.002278, 48.937012 }, { 20.219525, 49.170572 }, { 11.1364212, 49.3985109 },
            { 34.9815066, 49.6418383 }, { 19.28183, 49.844958 }, { 11.675344, 50.025517 }, { 18.7397217, 50.2087683 },
            { 15.545257, 50.369152 }, { 12.685723, 50.514557 }, { 34.867099, 50.71651 }, { 13.320189, 50.854225 },
            { 39.5579642, 50.9720115 }, { 10.440803, 51.108291 }, { 11.95317, 51.2639 }, { 40.3939819, 51.4900589 },
            { 39.4919471, 51.688118 }, { 16.5921766, 51.8468216 }, { 23.767037, 52.067057 }, { 11.787898, 52.210173 },
            { 10.830712, 52.335466 }, { 13.14576, 52.4781525 }, { 13.316067, 52.626145 }, { 41.363186, 52.760239 },
            { 87.672676, 53.0365905 }, { 45.15814, 53.206564 }, { 83.762583, 53.35036 }, { 49.933097, 53.508785 },
            { 12.0977027, 53.6808303 }, { 29.058341, 53.846824 }, { 58.445698, 53.980526 }, { 37.587284, 54.137676 },
            { 47.550941, 54.291511 }, { 53.21936, 54.44384 }, { 39.798526, 54.6193 }, { 20.502783, 54.698528 },
            { 10.6748184, 54.7512201 }, { 38.0885965, 54.8871949 }, { 73.191002, 54.999523 }, { 36.647716, 55.119083 },
            { 36.467336, 55.217543 }, { 43.36478, 55.384968 }, { 28.627537, 55.542338 }, { 37.4398133, 55.6315599 },
            { 52.3005766, 55.6686399 }, { 37.8183816, 55.7067266 }, { 37.5434541, 55.7322031 },
            { 37.8420316, 55.7582716 }, { 37.57077, 55.7887783 }, { 37.4520986, 55.8255555 },
            { 91.8883666, 55.8864669 }, { 37.5209069, 55.9742045 }, { 12.5721244, 56.0603446 },
            { 44.153954, 56.160107 }, { 91.081772, 56.254532 }, { 50.821495, 56.335728 }, { 60.299007, 56.490284 },
            { 54.048519, 56.736591 }, { 14.86361, 56.89197 }, { 12.7189016, 57.135715 }, { 27.92392, 57.621763 },
            { 26.78816, 57.967942 }, { 148.260231, -42.10851 }, { 144.985632, -37.813441 }, { 174.396843, -35.571013 },
            { 151.2430016, -33.9561266 }, { 116.568065, -33.053318 }, { 152.7601683, -27.6071416 },
            { 115.855772, -21.683341 }, { 18.660702, -30.475975 }, { 28.052362, -26.041512 }, { 39.254048, -6.761975 },
            { -58.207963, -31.613347 }, { -49.7618145, -21.6861586 }, { -78.1093547, 0.3847367 },
            { -78.1336832, 10.2224776 }, { -100.416623, 20.60098 }, { -94.9652169, 29.8082607 },
            { -105.796828, 33.3910161 }, { -92.4142708, 35.0840835 }, { -76.5157458, 37.181661 },
            { -85.80333, 38.330115 }, { -77.55309, 39.32678 }, { -74.627853, 40.178225 }, { -7.8869515, 40.9756515 },
            { -1.79757, 42.07725 }, { -73.617137, 42.661075 }, { -4.7351283, 43.4094183 }, { -1.2377911, 44.6974629 },
            { -95.243038, 45.708101 }, { -122.10894, 47.833553 }, { -0.8751546, 48.8386383 },
            { -3.5076739, 50.4667701 }, { -1.403765, 50.918708 }, { -0.787115, 51.2166089 },
            { -0.5453681, 51.3996863 }, { -3.108282, 51.543483 }, { -0.201346, 51.789341 }, { -0.877045, 52.069875 },
            { -2.0711835, 52.3137443 }, { -1.778965, 52.531248 }, { -1.549437, 52.8401184 }, { -3.948021, 53.104644 },
            { -2.404678, 53.346969 }, { -1.871973, 53.532677 }, { -1.684629, 53.895671 }, { -3.279653, 54.53264 },
            { -3.6928833, 55.5068833 }, { -3.83686, 56.37636 }, { -1.04996, 60.1503 }, { 32.571551, 0.351775 },
            { 0.81498, 51.310653 }, { 0.935179, 52.777733 }, { 2.146002, 41.476478 }, { 5.981337, 43.162238 },
            { 7.157896, 43.824324 }, { 2.5741589, 44.351035 }, { 7.68224, 45.052302 }, { 9.25329, 45.41377 },
            { 4.828938, 45.758675 }, { 7.8877, 46.173743 }, { 7.659757, 46.558913 }, { 7.4828662, 46.9909988 },
            { 6.09014, 47.2705 }, { 8.899455, 47.472738 }, { 8.7922918, 47.6820534 }, { 1.890157, 47.946657 },
            { 7.4040624, 48.2320994 }, { 1.854147, 48.52394 }, { 3.39478, 48.6923516 }, { 8.206773, 48.823729 },
            { 9.632988, 48.940665 }, { 9.8829804, 49.1322798 }, { 7.3427778, 49.3204167 }, { 7.838829, 49.4757356 },
            { 7.109198, 49.7015274 }, { 5.812668, 49.8739469 }, { 6.9163573, 50.0419623 }, { 9.9511, 50.1461 },
            { 9.289031, 50.311952 }, { 7.7624588, 50.4951553 }, { 7.1994028, 50.6296043 }, { 7.2384955, 50.7401477 },
            { 4.0217242, 50.8410908 }, { 7.06721, 50.9498183 }, { 9.403469, 51.089003 }, { 8.499645, 51.194818 },
            { 9.397121, 51.280806 }, { 8.261033, 51.380399 }, { 7.305432, 51.496458 }, { 5.337574, 51.676147 },
            { 1.064161, 51.858707 }, { 1.049763, 52.003645 }, { 8.557772, 52.137907 }, { 4.8214199, 52.2873749 },
            { 4.9087089, 52.3913502 }, { 8.282906, 52.713412 }, { 8.876422, 53.106141 }, { 8.0105211, 53.4956496 },
            { 9.0821016, 54.2040092 }, { 9.9432365, 55.7178712 }, { 9.641823, 59.643057 }, { 98.526943, 8.909037 },
            { 76.7295183, 18.175795 }, { 55.2962, 25.256981 }, { 35.2923397, 31.225873 }, { 34.9431986, 32.5586005 },
            { 28.208395, 36.321404 }, { 22.2738576, 38.8206196 }, { 16.546816, 41.131194 }, { 12.1752527, 42.1046932 },
            { 13.606543, 42.740128 }, { 24.620431, 43.415388 }, { 13.1238682, 43.7666482 }, { 10.104151, 44.12157 },
            { 26.1273061, 44.438915 }, { 10.637082, 44.714918 }, { 41.910641, 45.000759 }, { 18.15975, 45.1712433 },
            { 11.2647916, 45.4088099 }, { 24.6081411, 45.6018179 }, { 21.242985, 45.7745017 },
            { 11.112064, 45.965261 }, { 20.1294595, 46.241573 }, { 30.731378, 46.475362 }, { 12.945704, 46.59728 },
            { 23.6180488, 46.7831965 }, { 18.0078716, 47.0207483 }, { 13.082816, 47.164936 }, { 39.129478, 47.308628 },
            { 12.457168, 47.4505409 }, { 11.1911783, 47.5528069 }, { 13.45718, 47.6676216 }, { 25.7116772, 47.778211 },
            { 13.0825209, 47.9278119 }, { 10.9281864, 48.0811232 }, { 11.5680198, 48.1720581 },
            { 17.943548, 48.257825 }, { 16.7448036, 48.3949563 }, { 18.9850407, 48.5779735 }, { 44.48917, 48.775219 },
            { 11.6218614, 48.9868998 }, { 16.5988502, 49.2098388 }, { 12.432652, 49.450514 }, { 11.183062, 49.704957 },
            { 18.2561291, 49.8778241 }, { 14.457481, 50.051504 }, { 12.03976, 50.2538433 }, { 11.5538396, 50.3998222 },
            { 10.4269398, 50.5688387 }, { 11.999153, 50.742894 }, { 12.79725, 50.87132 }, { 13.661373, 50.995907 },
            { 18.8533361, 51.1332276 }, { 12.2375894, 51.3057876 }, { 10.79784, 51.52179 }, { 77.5595776, 51.7211328 },
            { 12.271957, 51.883613 }, { 12.6828658, 52.1007332 }, { 49.108001, 52.235801 }, { 21.145109, 52.371127 },
            { 32.006828, 52.50515 }, { 39.589863, 52.659409 }, { 54.783721, 52.824253 }, { 25.315359, 53.090694 },
            { 38.139894, 53.226453 }, { 83.742261, 53.370371 }, { 49.3313293, 53.5339279 }, { 91.479152, 53.717662 },
            { 27.535664, 53.8783649 }, { 13.87235, 54.0098449 }, { 37.462008, 54.16674 }, { 10.1253048, 54.3101984 },
            { 83.354454, 54.493244 }, { 39.763748, 54.634571 }, { 55.963684, 54.710674 }, { 56.02924, 54.771412 },
            { 23.9413833, 54.9112583 }, { 83.582298, 55.019138 }, { 61.370953, 55.141628 }, { 61.361, 55.240528 },
            { 58.679824, 55.418655 }, { 39.555061, 55.569405 }, { 12.63842, 55.640686 }, { 38.00634, 55.67457 },
            { 37.67712, 55.7131299 }, { 37.235875, 55.737355 }, { 37.5888417, 55.764175 }, { 49.1723871, 55.7934207 },
            { 37.39579, 55.83581 }, { 37.522037, 55.901393 }, { 40.01968, 55.990055 }, { 40.222664, 56.089703 },
            { 46.243126, 56.185856 }, { 10.2077586, 56.266752 }, { 41.338596, 56.352173 }, { 68.468116, 56.51873 },
            { 61.8987799, 56.7720398 }, { 63.159199, 56.92944 }, { 10.144822, 57.208427 }, { 11.94883, 57.688256 },
            { 55.940528, 58.003993 }, { 174.7765183, -41.274955 }, { 145.1056924, -37.6592534 },
            { 150.553038, -35.207509 }, { 121.877828, -33.859531 }, { 152.4991266, -32.174735 },
            { 153.014555, -27.46634 }, { 116.434, -17.216 }, { 32.134347, -27.904693 }, { 28.3057833, -25.7857833 },
            { 36.783356, -1.313245 }, { -66.725391, -29.2637278 }, { -149.6089821, -17.6140008 },
            { -75.1916089, 4.3832641 }, { -69.1111716, 12.27648 }, { -107.4013673, 24.800808 },
            { -86.510925, 30.39554 }, { -117.966608, 33.807764 }, { -119.331021, 35.670429 },
            { -121.940892, 37.378326 }, { -122.4255065, 38.6171073 }, { -5.02186, 39.49702 }, { -8.366091, 40.371651 },
            { -6.58509, 41.204398 }, { -2.09324, 42.22843 }, { -7.1076167, 42.80245 }, { -6.6221702, 43.5476042 },
            { -76.541904, 44.93359 }, { -0.157982, 46.227265 }, { -2.0988498, 48.0929246 },
            { -121.9366121, 49.1831517 }, { -120.337026, 50.675778 }, { -1.422901, 50.962345 },
            { -0.013586, 51.253158 }, { -0.45266, 51.429405 }, { -2.535974, 51.580939 }, { -3.6847055, 51.8312951 },
            { -1.370384, 52.115457 }, { -2.038683, 52.355483 }, { -6.2694617, 52.5656287 }, { -1.4863833, 52.9099833 },
            { -2.406352, 53.153605 }, { -2.8532216, 53.3708966 }, { -3.0006817, 53.6248567 },
            { -2.7874167, 54.0082222 }, { -2.173502, 54.649774 }, { -3.0245254, 55.8328781 }, { -3.731385, 56.707956 },
            { -19.158844, 63.88595 }, { 0.9376586, 42.570807 }, { 0.17367, 51.40573 }, { 98.124188, 3.5489982 },
            { 9.275445, 41.831882 }, { 6.6826683, 43.3465199 }, { 5.2847183, 43.9319552 }, { 1.816413, 44.474934 },
            { 5.253763, 45.107568 }, { 6.8902301, 45.4835379 }, { 4.8626047, 45.8349627 }, { 8.8434144, 46.2537346 },
            { 5.42184, 46.667476 }, { 7.1062409, 47.0487132 }, { 8.3834749, 47.3181483 }, { 6.7065172, 47.5168304 },
            { 7.9835566, 47.7248999 }, { 7.962068, 47.992865 }, { 8.7109091, 48.291834 }, { 8.4669479, 48.5488999 },
            { 8.4245584, 48.7179678 }, { 9.135767, 48.850853 }, { 8.9085116, 48.9704449 }, { 9.250701, 49.170611 },
            { 8.413879, 49.353371 }, { 5.7649329, 49.5054533 }, { 9.51632, 49.742538 }, { 9.2179316, 49.9138866 },
            { 8.8017595, 50.0611775 }, { 9.394945, 50.17307 }, { 7.6322551, 50.3433662 }, { 4.242843, 50.527288 },
            { 9.395625, 50.65155 }, { 7.0581933, 50.76162 }, { 9.344326, 50.8653189 }, { 9.475897, 50.973246 },
            { 4.6556076, 51.1166906 }, { 6.7968, 51.210852 }, { 6.761611, 51.297712 }, { 6.805647, 51.403901 },
            { 7.6958033, 51.5236583 }, { 8.737087, 51.71442 }, { 9.088578, 51.89615 }, { 8.5326486, 52.0285087 },
            { 7.952089, 52.168599 }, { 4.8563599, 52.2986016 }, { 9.931615, 52.422968 }, { 6.791011, 52.841355 },
            { 7.456626, 53.168108 }, { 8.586147, 53.564393 }, { 9.8457411, 54.4579513 }, { 8.5029233, 55.95379 },
            { 5.2775083, 60.3747783 }, { 79.137337, 10.787313 }, { 99.174858, 18.7625579 }, { 80.973172, 26.851514 },
            { 35.3136966, 31.7318566 }, { 35.069401, 32.854091 }, { 31.17877, 36.8466835 }, { 21.629577, 39.702196 },
            { 13.4948853, 41.386892 }, { 25.56016, 42.2882 }, { 11.588755, 42.911335 }, { 43.6345558, 43.5002632 },
            { 12.413943, 43.838916 }, { 28.6232205, 44.1932566 }, { 11.2717294, 44.4704589 }, { 10.330273, 44.786997 },
            { 41.946541, 45.032597 }, { 11.5331878, 45.2260322 }, { 12.356737, 45.431043 }, { 18.4618163, 45.6414556 },
            { 15.009751, 45.781912 }, { 13.142477, 46.034629 }, { 39.82975, 46.284386 }, { 15.5928142, 46.4999538 },
            { 13.8137989, 46.625103 }, { 15.564951, 46.828178 }, { 25.933502, 47.051908 }, { 17.663553, 47.197538 },
            { 17.6102748, 47.3427268 }, { 16.115206, 47.472461 }, { 14.20165, 47.572408 }, { 17.633111, 47.687355 },
            { 12.275491, 47.803712 }, { 10.21301, 47.966018 }, { 11.52588, 48.102445 }, { 16.3023716, 48.1858751 },
            { 16.528258, 48.283607 }, { 11.5527096, 48.4270656 }, { 17.8814033, 48.6193016 },
            { 17.1628036, 48.8171653 }, { 14.7527617, 49.0244961 }, { 10.632377, 49.24588 }, { 11.12285, 49.495483 },
            { 13.3295661, 49.7436785 }, { 10.751812, 49.913083 }, { 19.9591513, 50.0738906 }, { 11.92714, 50.2861166 },
            { 12.921626, 50.423176 }, { 11.170982, 50.612442 }, { 12.5367767, 50.7742383 }, { 15.277504, 50.893634 },
            { 39.440678, 51.026649 }, { 12.9457237, 51.1602725 }, { 10.164361, 51.347541 }, { 81.201396, 51.565345 },
            { 39.187198, 51.753593 }, { 19.1048594, 51.9191237 }, { 26.107473, 52.120168 }, { 21.0353575, 52.2598701 },
            { 10.727613, 52.3991203 }, { 13.110365, 52.5263016 }, { 23.843297, 52.703707 }, { 40.694057, 52.892311 },
            { 10.179822, 53.127646 }, { 50.745246, 53.252727 }, { 61.020824, 53.391781 }, { 23.1039816, 53.5699133 },
            { 28.003706, 53.7548027 }, { 25.4667013, 53.9047538 }, { 41.664062, 54.03852 }, { 61.4333801, 54.1913566 },
            { 10.223477, 54.335935 }, { 82.209976, 54.534328 }, { 39.6371, 54.656956 }, { 20.496167, 54.720017 },
            { 37.486328, 54.794392 }, { 23.9206111, 54.9361916 }, { 82.35453, 55.041485 }, { 82.979965, 55.162476 },
            { 58.132416, 55.276119 }, { 21.3155388, 55.4507611 }, { 44.596305, 55.587413 }, { 12.613292, 55.648777 },
            { 37.5171083, 55.682365 }, { 37.568607, 55.717798 }, { 37.6051229, 55.7432127 }, { 37.594324, 55.771032 },
            { 37.4575316, 55.8010299 }, { 37.56774, 55.8474666 }, { 39.155052, 55.914833 }, { 92.922569, 56.00045 },
            { 47.246002, 56.111565 }, { 87.057665, 56.207015 }, { 39.411304, 56.284138 }, { 36.868136, 56.384289 },
            { 43.479252, 56.562634 }, { 60.7885742, 56.8037109 }, { 23.9838533, 56.95898 }, { 56.5359839, 57.2860412 },
            { 13.0151016, 57.7401333 }, { 56.3146362, 58.0366936 }, { 175.375555, -40.4583766 },
            { 144.2440059, -37.1274899 }, { 138.6132507, -34.9128837 }, { 150.629228, -33.767445 },
            { 115.81242, -31.69541 }, { 153.0372699, -27.3592899 }, { 115.1305443, -8.7908005 },
            { 31.921699, -26.866014 }, { 31.584708, -25.04874 }, { -72.867977, -50.967073 },
            { -65.7379399, -28.4554349 }, { -38.138732, -12.707137 }, { -74.793613, 5.071472 },
            { -61.300198, 15.47369 }, { -15.446821, 27.906351 }, { -7.665402, 31.242473 },
            { -118.2567424, 34.0515072 }, { -96.092709, 36.138792 }, { -122.3114749, 37.5798808 },
            { -77.529464, 38.810441 }, { -86.082542, 39.677837 }, { -75.46007, 40.479415 }, { -4.7025916, 41.4828183 },
            { -1.81441, 42.32561 }, { -78.736434, 42.931557 }, { -69.836531, 43.743771 }, { -75.89448, 45.2943499 },
            { -0.9824284, 46.889709 }, { -4.5944499, 48.2893236 }, { -124.563378, 49.344476 },
            { -1.322777, 50.763672 }, { -3.3256833, 51.0191566 }, { -1.184415, 51.291795 }, { -0.958771, 51.458956 },
            { -3.928182, 51.62161 }, { -1.274603, 51.884276 }, { -0.4694172, 52.1491158 }, { -8.168548, 52.396892 },
            { -0.740209, 52.614724 }, { -7.82345, 52.940456 }, { -1.063292, 53.20565 }, { -2.873548, 53.385633 },
            { -1.728563, 53.702215 }, { -1.625747, 54.167493 }, { -1.6514682, 54.7656011 }, { -3.306732, 55.896702 },
            { -2.1891833, 57.1335833 }, { -21.6937575, 64.1665706 }, { 0.8982467, 45.6351 }, { 0.677565, 51.7313466 },
            { 3.212619, 39.5746 }, { 1.243133, 42.169 }, { 5.166367, 43.530281 }, { 3.8522917, 44.0122517 },
            { 6.6628349, 44.642226 }, { 5.774, 45.1736 }, { 6.164244, 45.550827 }, { 5.2966386, 45.9051918 },
            { 7.1642646, 46.342308 }, { 6.6461095, 46.767044 }, { 8.417157, 47.105252 }, { 8.061819, 47.3619747 },
            { 8.073025, 47.5650604 }, { 7.3607, 47.77161 }, { 7.628616, 48.043237 }, { 2.8252726, 48.3666014 },
            { 2.439777, 48.590958 }, { 8.350088, 48.74667 }, { 2.0477175, 48.8752087 }, { 8.2593851, 49.0044174 },
            { 8.594982, 49.203931 }, { 9.077288, 49.389465 }, { 8.1343333, 49.5438333 }, { 6.626531, 49.775842 },
            { 7.45487, 49.959195 }, { 8.286755, 50.089358 }, { 8.92118, 50.202084 }, { 6.7919749, 50.3794033 },
            { 8.668708, 50.555537 }, { 6.315756, 50.671478 }, { 8.8382853, 50.7799584 }, { 4.710561, 50.885447 },
            { 3.71869, 51.0005958 }, { 8.2604873, 51.1378401 }, { 9.4814816, 51.2260799 }, { 3.855473, 51.319189 },
            { 7.2989433, 51.4288466 }, { 5.162124, 51.552345 }, { 8.70287, 51.744889 }, { 4.3791066, 51.9259733 },
            { 8.6134691, 52.0548628 }, { 5.999232, 52.198951 }, { 9.389799, 52.322833 }, { 9.721588, 52.48334 },
            { 5.6303956, 52.936293 }, { 8.788624, 53.23579 }, { 9.9411141, 53.6166179 }, { 9.492678, 54.696567 },
            { 9.48778, 56.244579 }, { 7.500505, 61.0637433 }, { 35.573329, 12.726842 }, { 99.086235, 18.845543 },
            { 85.4691009, 28.2049102 }, { 75.7828077, 31.8262336 }, { 10.9940961, 33.8093022 },
            { 99.003095, 37.226029 }, { 44.6334761, 40.1748132 }, { 14.85781, 41.6973083 }, { 69.605087, 42.347892 },
            { 11.6778716, 43.0854766 }, { 11.6489624, 43.57903 }, { 12.900661, 43.910713 }, { 15.63746, 44.250683 },
            { 10.745088, 44.5103269 }, { 13.8335696, 44.8446606 }, { 42.00425, 45.05191 }, { 34.136307, 45.274658 },
            { 10.955275, 45.459545 }, { 27.1853341, 45.6790338 }, { 15.8813316, 45.8131116 },
            { 11.1131264, 46.082652 }, { 13.5077552, 46.3383865 }, { 15.8860449, 46.5206209 },
            { 11.041832, 46.656371 }, { 13.511, 46.8802 }, { 15.089806, 47.074561 }, { 12.159717, 47.228225 },
            { 13.861073, 47.378183 }, { 12.780143, 47.493825 }, { 18.966248, 47.596174 }, { 12.2465362, 47.708599 },
            { 13.0555166, 47.8281983 }, { 11.4929905, 47.9997883 }, { 13.1650037, 48.1221923 },
            { 11.7981645, 48.1987386 }, { 14.2580575, 48.3121528 }, { 17.7405557, 48.4641624 },
            { 13.829398, 48.663958 }, { 72.140007, 48.85807 }, { 17.4054766, 49.067173 }, { 33.059586, 49.289848 },
            { 17.1812, 49.550446 }, { 73.093683, 49.781363 }, { 18.37483, 49.95578 }, { 12.0969133, 50.1023333 },
            { 37.945163, 50.311213 }, { 80.238464, 50.438601 }, { 10.7509702, 50.6479598 }, { 13.5473681, 50.8013976 },
            { 13.891848, 50.91734 }, { 11.5058483, 51.05554 }, { 16.1768183, 51.1909083 }, { 10.314315, 51.392038 },
            { 19.2150364, 51.6160574 }, { 39.294887, 51.780723 }, { 86.016724, 51.964207 }, { 55.28519, 52.146873 },
            { 10.9877812, 52.2850962 }, { 85.656183, 52.42464 }, { 12.967243, 52.555719 }, { 11.5007067, 52.7241 },
            { 42.073345, 52.953583 }, { 23.157265, 53.14592 }, { 17.964718, 53.283912 }, { 14.548607, 53.433165 },
            { 10.1286166, 53.6075788 }, { 13.285573, 53.785484 }, { 39.514164, 53.928272 }, { 50.663376, 54.073883 },
            { 12.1880405, 54.2237514 }, { 81.0778053, 54.3686605 }, { 86.32484, 54.57285 }, { 20.464183, 54.672133 },
            { 55.944153, 54.726486 }, { 55.6867256, 54.8217392 }, { 82.928513, 54.961334 }, { 74.60527, 55.064991 },
            { 61.3576736, 55.1787681 }, { 77.782501, 55.316105 }, { 65.321075, 55.481041 }, { 37.4254682, 55.604387 },
            { 37.7644583, 55.65693 }, { 37.7556266, 55.6905933 }, { 37.695236, 55.722672 }, { 37.6977115, 55.7482027 },
            { 37.7008083, 55.7757817 }, { 37.4617383, 55.8094166 }, { 37.992037, 55.859842 },
            { 37.3768766, 55.9320516 }, { 92.953025, 56.01762 }, { 40.884129, 56.13261 }, { 43.941162, 56.225787 },
            { 10.46855, 56.302296 }, { 41.879147, 56.420464 }, { 61.291072, 56.624737 }, { 53.213531, 56.832351 },
            { 40.992214, 56.998196 }, { 21.5930716, 57.3972466 }, { 67.9317935, 57.8048186 }, { 38.793808, 58.056705 } };

    public static void main(String[] args) throws URISyntaxException, IOException {
        if (args.length < 7) {
            System.out
                    .println("Example Usage: java -jar SpatialIndexEvaluatorForSyntheticData10CopyQueryOnly.jar <index type> <workload type> <dataset name> <iteration count> <circle radius> <cc ipAddress> <cc portnum>");
            System.out.println("\targ0: index type - shbtree, dhbtree, sif, or rtree");
            System.out.println("\targ1: workload type - load or query");
            System.out.println("\targ2: dataset name to use");
            System.out.println("\targ3: iteration count");
            System.out.println("\targ4: circle radius");
            System.out.println("\targ5: asterix cc ip address");
            System.out.println("\targ6: asterix api port number");
            System.exit(-1);
        }

        indexType = args[0];
        workType = args[1]; //either select or join.
        if (workType.contains("join")) isJoin = true;
        datasetName = args[2];
        int runCount = Integer.parseInt(args[3]);
        circleRadius = Double.parseDouble(args[4]);
        ipAddress = args[5];
        portNum = args[6];

        //datasets and indexes are already populated.
        for (int j = 0; j < runCount; j++) {
            if (j != 0) {
                circleRadius *= 10;
            }
            System.out.println("running " + indexType + " " + workType + " " + datasetName + " " + runCount + "(" + j
                    + ") " + circleRadius + " " + ipAddress + " " + portNum);

            if (indexType.contains("shbtree")) {
                runQuery("SHBTree", j);
            } else if (indexType.contains("dhbtree")) {
                runQuery("DHBTree", j);
            } else if (indexType.contains("rtree")) {
                runQuery("RTree", j);
            } else if (indexType.contains("sif")) {
                runQuery("SIF", j);
            }
        }
    }

    private static void runQuery(String indexType, int runCount) throws URISyntaxException, IOException {
        rand = new Random();
        rand.setSeed(1);
        HttpResponse response;
        AsterixHttpClient ahc = new AsterixHttpClient(ipAddress, portNum);
        StringBuilder sb = new StringBuilder();
        FileOutputStream fosQuery = null;
        if (isJoin) {
            fosQuery = ahc.openOutputFile("./" + indexType + "JoinQueryResult" + runCount + ".txt");
        } else {
            fosQuery = ahc.openOutputFile("./" + indexType + "SelectQueryResult" + runCount + ".txt");
        }

        try {
            for (int i = 0; i < numQuery; i++) {

                for (int j = 0; j < numDatasetCopy; j++) {
                    if (j == targetDatasetNum) {
                        if (isJoin) {
                            ahc.prepareQuery(getJoinQueryAQL(j));
                        } else {
                            ahc.prepareQuery(getSelectQueryAQL(j));
                        }
                        sw.start();
                        response = ahc.execute();
                        sw.stop();
                        if (isJoin) {
                            sb.append("JoinQuery\t" + sw.getElapsedTime() + "\n");
                        } else {
                            sb.append("SelectQuery\t" + sw.getElapsedTime() + "\n");
                        }
                        ahc.printResult(response, fosQuery);
                    } else {
                        if (isJoin) {
                            ahc.prepareQuery(getJoinQueryAQL(j));
                        } else {
                            ahc.prepareQuery(getSelectQueryAQL(j));
                        }
                        response = ahc.execute();
                        //consume result
                        EntityUtils.consume(response.getEntity());
                    }

                }
                ++joinIdx; //joinIdx can be more than 5M.
                ++coordCircleIdx;
                if (coordCircleIdx == coordinates.length) {
                    coordCircleIdx = 0;
                }
            }
        } finally {
            ahc.closeOutputFile(fosQuery);
            System.out.println(sb.toString());
        }
    }

    private static String getSelectQueryAQL(int datasetNumber) {
        double x1, y1; // center point
        x1 = coordinates[coordCircleIdx][0];
        y1 = coordinates[coordCircleIdx][1];
        Double radius = circleRadius;

        StringBuilder sb = new StringBuilder();
        sb.append("use dataverse STBench; ");
        sb.append("count( ");
        sb.append("for $x in dataset " + datasetName).append(datasetNumber).append(" ");
        sb.append("let $n :=  create-circle( ");
        sb.append("point(\"").append(x1).append(", ").append(y1).append("\") ");
        sb.append(", ");
        sb.append(String.format("%f", radius));
        sb.append(" )");
        sb.append("where spatial-intersect($x.coordinates, $n)");
        sb.append("return $x ");
        sb.append(");");
        return sb.toString();
    }

    private static String getJoinQueryAQL(int datasetNumber) {
        Double radius = circleRadius;
        int lowId = joinIdx * 1000; 
        int highId = lowId + 100; //for every 1000 records, use the first 100 records for probe side.

        /* example query
        use dataverse STBench;
        count(
        for $x in dataset FsqVenue0
        let $area := create-circle($x.coordinates, 0.00001)
        for $y in dataset FsqCheckinTweet0
        where $x.id >= int64("0") and $x.id < int64("200") and spatial-intersect($y.coordinates, $area)
        return $y
        )
        */
        StringBuilder sb = new StringBuilder();
        sb.append(" use dataverse STBench; \n");
        sb.append(" count( \n");
        sb.append(" for $x in dataset FsqVenue").append(datasetNumber).append(" \n");
        sb.append(" let $area := create-circle($x.coordinates, ").append(String.format("%f", radius)).append(" ) \n");
        sb.append(" for $y in dataset " + datasetName).append(datasetNumber).append(" \n");
        sb.append(" where $x.id >= int64(\"" + lowId + "\") ").append("and $x.id < int64(\"" + highId + "\") and ");
        sb.append(" spatial-intersect($y.coordinates, $area) \n");
        sb.append(" return $y \n");
        sb.append(" );\n");

        return sb.toString();
    }
    
    

}
