package application.detection;

import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.*;

import java.util.LinkedList;
import java.util.List;

public class DetectorObject {
    private static DetectorType detectorType;

    private static MatOfKeyPoint keyPointObject = new MatOfKeyPoint();
    private static MatOfKeyPoint keyPointScene = new MatOfKeyPoint();
    private static Mat descriptorsObject = null;
    private static Mat descriptorScene = new Mat();
    private static MatOfDMatch matOfDMatch = new MatOfDMatch();
    private static MatOfDMatch matOfDMatchGood = new MatOfDMatch();


    private static Feature2D  keyPointsDetector = AKAZE.create();
    private static Mat frameObject = null;
    private static DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

    public static void run(Mat frameScene){
        if (frameObject == null)
            throw new RuntimeException("Set the value for frameObject (DetectorObject.setFrameObject())");
        if (descriptorsObject == null){
            keyPointsDetector.detect(frameObject, keyPointObject);
            descriptorsObject = new Mat();
            keyPointsDetector.compute(frameObject, keyPointObject, descriptorsObject);
        }

        keyPointsDetector.detect(frameScene, keyPointScene);
        keyPointsDetector.compute(frameScene, keyPointScene, descriptorScene);

        descriptorMatcher.match(descriptorsObject, descriptorScene, matOfDMatch);

        findGoodMatch(matOfDMatch);
    }

    private static void findGoodMatch(MatOfDMatch matOfDMatch){
        double minDist = Double.MAX_VALUE;
        double dist = 0;
        List<DMatch> list = matOfDMatch.toList();
        for (int i = 0; i < list.size(); i++){
            dist = list.get(i).distance;
            if (dist == 0) continue;
            if (dist < minDist) minDist = dist;
        }

        LinkedList<DMatch> listGood = new LinkedList<DMatch>();
        for (int i = 0; i < list.size(); i++){
            if (list.get(i).distance < minDist*3)
                listGood.add(list.get(i));
        }
        matOfDMatchGood.fromList(listGood);
        System.out.println(list.size() + " " + listGood.size()); // test
    }



    public static void setKeyPointsDetector(DetectorType detectorType) {
        if (detectorType == DetectorType.AKAZE)
            keyPointsDetector = AKAZE.create();
        else if (detectorType == DetectorType.BRISK)
            keyPointsDetector = BRISK.create();
        else if (detectorType == DetectorType.ORB)
            keyPointsDetector = ORB.create();
    }

    public static void setFrameObject(Mat frameObject) {
        DetectorObject.frameObject = frameObject;
    }

    public static void setDescriptorMatcher(MatcherType matcher) {
        if (matcher == MatcherType.BRUTEFORCE)
            descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
        else if (matcher == MatcherType.BRUTEFORCE_L1)
            descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_L1);
        else if (matcher == MatcherType.BRUTEFORCE_HAMMING)
            descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        else if (matcher == MatcherType.BRUTEFORCE_HAMMINGLUT)
            descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
        else if (matcher == MatcherType.BRUTEFORCE_SL2)
            descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_SL2);
    }
}
