import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        TaskC.run();
    }
}

class FastScanner {
    BufferedReader br;
    StringTokenizer st;

    FastScanner(InputStream stream) {
        try {
            br = new BufferedReader(new InputStreamReader(stream));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String next() {
        while (st == null || !st.hasMoreTokens()) {
            try {
                st = new StringTokenizer(br.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return st.nextToken();
    }

    int nextInt() {
        return Integer.parseInt(next());
    }
}

class TaskC {
    private static Distance distance;

    enum Distance {MANHATTAN, EUCLIDEAN, CHEBYSHEV}

    private static Kernel kernel;

    enum Kernel {UNIFORM, TRIANGULAR, EPANECHNIKOV, QUARTIC, TRIWEIGHT, TRICUBE, GAUSSIAN, COSINE, LOGISTIC, SIGMOID}

    private static WindowType windowType;

    enum WindowType {FIXED, VARIABLE}

    private static int paramAmount;

    private static int stringAmount;

    private static Integer[][] inpData;

    private static int[] query;

    private static double calculateMANHATTAN(int[] query, Integer[] vector) {
        double result = 0.0;
        for (int i = 0; i < paramAmount; i++) {
            result += Math.abs(query[i] - vector[i]);
        }
        return result;
    }

    private static double calculateEUCLIDEAN(int[] query, Integer[] vector) {
        double result = 0.0;
        for (int i = 0; i < paramAmount; i++) {
            result += Math.pow(query[i] - vector[i], 2);
        }
        return Math.sqrt(result);
    }

    private static double calculateCHEBYSHEV(int[] query, Integer[] vector) {
        double result = 0.0;
        for (int i = 0; i < paramAmount; i++) {
            int tmp = Math.abs(query[i] - vector[i]);
            result = result < tmp ? tmp : result;
        }
        return result;
    }

    private static double calculateDistance(int[] query, Integer[] vector) {
        switch (distance) {
            case MANHATTAN:
                return calculateMANHATTAN(query, vector);
            case EUCLIDEAN:
                return calculateEUCLIDEAN(query, vector);
            case CHEBYSHEV:
                return calculateCHEBYSHEV(query, vector);
        }
        return 0.0;
    }

    private static LinkedHashMap<Integer, Double> sortDistances(HashMap<Integer, Double> distances) {
        return distances.entrySet().stream().
                sorted(Map.Entry.comparingByValue()).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }

    private static double average;

    private static double calculateAVG(Integer[][] inpData) {
        double sum = 0;
        for (Integer[] str : inpData) {
            sum += str[paramAmount];
        }
        return sum / inpData.length;
    }

    private static Integer[][] returnSimilar() {
        List<Integer[]> resultList = new ArrayList<>(stringAmount);
        for (int i = 0; i < stringAmount; i++) {
            boolean isEqual = true;
            for (int j = 0; j < paramAmount; j++) {
                if (inpData[i][j] != query[j]) {
                    isEqual = false;
                    break;
                }
            }
            if (isEqual) {
                resultList.add(inpData[i]);
            }
        }
        int size = resultList.size();
        Integer[][] result = new Integer[size][paramAmount + 1];
        for (int i = 0; i < size; i++) {
            result[i] = resultList.get(i);
        }

        return result;
    }

    private static double getKernel(double distance) {
        double result = 0;
        if (Math.abs(distance) < 1)
            switch (kernel) {
                case UNIFORM:
                    return 0.5;
                case TRIANGULAR:
                    return 1 - Math.abs(distance);
                case EPANECHNIKOV:
                    return 0.75 * (1 - Math.pow(distance, 2));
                case QUARTIC:
                    return 15.0 / 16 * Math.pow(1 - Math.pow(distance, 2), 2);
                case TRIWEIGHT:
                    return 35.0 / 32 * Math.pow(1d - Math.pow(distance, 2), 3);
                case TRICUBE:
                    return 70.0 / 81 * Math.pow(1 - Math.pow(Math.abs(distance), 3), 3);
                case COSINE:
                    return Math.PI * Math.cos(Math.PI * distance / 2) / 4;
            }
        switch (kernel) {
            case GAUSSIAN:
                return Math.pow(Math.E, -Math.pow(distance, 2) / 2) / Math.sqrt(2 * Math.PI);
            case LOGISTIC:
                return 1 / (Math.pow(Math.E, distance) + 2 + Math.pow(Math.E, -distance));
            case SIGMOID:
                return 2 / (Math.PI * (Math.pow(Math.E, distance) + Math.pow(Math.E, -distance)));
        }
        return result;
    }

    public static void run() {
        //input
        FastScanner scan = new FastScanner(System.in);
        stringAmount = scan.nextInt();
        paramAmount = scan.nextInt();
        inpData = new Integer[stringAmount][paramAmount + 1];
        query = new int[paramAmount];
        for (int i = 0; i < stringAmount; i++) {
            for (int j = 0; j < paramAmount + 1; j++) {
                inpData[i][j] = scan.nextInt();
            }
        }
        for (int i = 0; i < paramAmount; i++) {
            query[i] = scan.nextInt();
        }
        distance = Distance.valueOf(scan.next().toUpperCase());
        kernel = Kernel.valueOf(scan.next().toUpperCase());
        windowType = WindowType.valueOf(scan.next().toUpperCase());
        Double windowSize = 0.0;
        int neighbourAmount = 0;
        if (windowType == WindowType.FIXED) {
            windowSize = (double) scan.nextInt();
        } else if (windowType == WindowType.VARIABLE) {
            neighbourAmount = scan.nextInt();
        }
        //calc average
        average = calculateAVG(inpData);
        //calculate distances
        HashMap<Integer, Double> distances = new HashMap<>();
        for (int i = 0; i < stringAmount; i++) {
            double dist = calculateDistance(query, inpData[i]);
            distances.put(i, dist);
        }
        //sort distances
        LinkedHashMap<Integer, Double> sortedDistances = sortDistances(distances);
        List<Object> sortedValues = Arrays.asList(sortedDistances.values().toArray());
        List<Object> sortedKeys = Arrays.asList(sortedDistances.keySet().toArray());
        //computation of windowSize/neighbourAmount
        if (windowType == WindowType.FIXED) {
            for (int i = 0; i < stringAmount; i++) {
                if ((double) sortedValues.get(i) >= windowSize) {
                    neighbourAmount = i;
                    break;
                }
                if (i == stringAmount - 1) {
                    neighbourAmount = stringAmount;
                    break;
                }
            }
        } else if (windowType == WindowType.VARIABLE) {
            windowSize = (double) sortedValues.get(neighbourAmount);
        }
        //Case if windowSize == 0
        if (windowSize == 0) {
            Integer[][] similar = returnSimilar();
            if (similar.length != 0) {
                System.out.println(calculateAVG(similar));
            } else {
                System.out.println(average);
            }
            return;
        }
        //Case if neighbourAmount == 0
        if (neighbourAmount == 0) {
            System.out.println(average);
            return;
        }

        double numerator = 0;
        double denominator = 0;
        for (int i = 0; i < stringAmount; i++) {
            double kernel = getKernel((double) sortedValues.get(i) / windowSize);
            double target = inpData[(int) sortedKeys.get(i)][paramAmount];
            numerator += target * kernel;
            denominator += kernel;
        }

        if (denominator == 0) {
            System.out.println(average);
        } else {
            System.out.println(numerator / denominator);
        }
    }
}

class TaskB {
    static int[] colSum;
    static int[] lineSum;
    static int[] diagonal;
    static int classAmount;
    static int total;
    static double recallW;
    static double precisionW;

    static double recall(int i) {
        return (double) diagonal[i] / colSum[i];
    }

    static double precision(int i) {
        return (double) diagonal[i] / lineSum[i];
    }

    static void countRecallWPrecisionW() {
        double result = 0.0;
        int sum = 0;
        for (int i = 0; i < classAmount; i++) {
            if (colSum[i] != 0) {
                result += (double) diagonal[i] * lineSum[i] / colSum[i];
                sum += diagonal[i];
            }
        }
        precisionW = result / total;
        recallW = (double) sum / total;
    }


    static void run() {
        //input section
        FastScanner scan = new FastScanner(System.in);
        classAmount = scan.nextInt();
        total = 0;
        colSum = new int[classAmount];
        lineSum = new int[classAmount];
        diagonal = new int[classAmount];
        int current = 0;
        for (int i = 0; i < classAmount; i++) {
            for (int j = 0; j < classAmount; j++) {
                current = scan.nextInt();
                total += current;
                colSum[j] += current;
                lineSum[i] += current;
                if (j == i) {
                    diagonal[i] = current;
                }
            }
        }
        //Macro score
        countRecallWPrecisionW();
        double macroF = 2 * precisionW * recallW / (precisionW + recallW);
        //Micro score
        double microF = 0.0;
        for (int i = 0; i < classAmount; i++) {
            if (diagonal[i] != 0 && colSum[i] != 0 && lineSum[i] != 0) {
                microF += lineSum[i] * 2 * precision(i) * recall(i) / (precision(i) + recall(i)) / total;
            }
        }
        //Output
        System.out.println(macroF);
        System.out.println(microF);
    }
}

class TaskA {
    static void run() {
        //input section
        FastScanner scan = new FastScanner(System.in);
        int objAmount = scan.nextInt();
        int classesAmount = scan.nextInt();
        int partsAmount = scan.nextInt();
        List<Integer> objects = new ArrayList<>(objAmount);
        for (int i = 0; i < objAmount; i++) {
            objects.add(scan.nextInt());
        }
        //create class lists
        List<List<Integer>> objByClass = new ArrayList<>(classesAmount);
        int capacityClassesAmount = objAmount / classesAmount + 1;
        for (int i = 0; i < classesAmount; i++) {
            objByClass.add(new ArrayList<>(capacityClassesAmount));
        }
        //initialize class lists
        for (int i = 0; i < objAmount; i++) {
            objByClass.get(objects.get(i) - 1).add(i + 1);
        }
        //create result lists
        List<List<Integer>> result = new ArrayList<>(partsAmount);
        int capacityResult = objAmount / partsAmount + 1;
        for (int i = 0; i < partsAmount; i++) {
            result.add(new ArrayList<>(capacityResult));
        }
        //initialize result lists
        int currentPos = 0;
        for (int i = 0; i < classesAmount; i++) {
            for (int j = 0; j < objByClass.get(i).size(); j++) {
                if (currentPos == partsAmount) {
                    currentPos = 0;
                }
                result.get(currentPos).add(objByClass.get(i).get(j));
                currentPos++;
            }
        }
        //print result
        for (int i = 0; i < partsAmount; i++) {
            System.out.print(result.get(i).size());
            for (int j = 0; j < result.get(i).size(); j++) {
                System.out.print(" " + result.get(i).get(j));
            }
            System.out.println();
        }

    }
}

class Scanner {
    BufferedReader br;
    StringTokenizer st;

    public Scanner(InputStream in) {
        br = new BufferedReader(new InputStreamReader(in));
        eat("");
    }

    private void eat(String s) {
        st = new StringTokenizer(s);
    }

    public String nextLine() {
        try {
            return br.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    public boolean hasNext() {
        while (!st.hasMoreTokens()) {
            String s = nextLine();
            if (s == null)
                return false;
            eat(s);
        }
        return true;
    }

    public String next() {
        hasNext();
        return st.nextToken();
    }

    public int nextInt() {
        return Integer.parseInt(next());
    }

}