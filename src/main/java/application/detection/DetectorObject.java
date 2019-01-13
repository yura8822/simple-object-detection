package application.detection;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.features2d.*;
import org.opencv.imgproc.Imgproc;

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
    private static int listMatchSize;
    private static int listMatchGoodSize;
    private static Mat homography = new Mat();


    private static Feature2D  keyPointsDetector = AKAZE.create();
    private static Mat frameObject = null;
    private static DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
    private static double matchSelectionFactor = 3;
    private static double ransacReprojThreshold = 3;

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
        findGoodMatch();
        if ((listMatchGoodSize >= 4) && (listMatchSize != listMatchGoodSize)) {
            findHomography();
            selectFoundObject(frameScene);
        }
    }

    private static void findGoodMatch(){
        double minDist = Double.MAX_VALUE;
        double dist = 0;
        List<DMatch> list = matOfDMatch.toList();
        for (int i = 0; i < list.size(); i++){
            dist = list.get(i).distance;
            if (dist == 0) continue;
            if (dist < minDist) minDist = dist;
        }
        listMatchSize = list.size();

        LinkedList<DMatch> listGood = new LinkedList<DMatch>();
        for (int i = 0; i < list.size(); i++){
            if (list.get(i).distance < minDist*matchSelectionFactor)
                listGood.add(list.get(i));
        }
        listMatchGoodSize = listGood.size();
        matOfDMatchGood.fromList(listGood);
        System.out.println(listMatchSize + " " + listMatchGoodSize); // test
    }

    private static void findHomography(){
        List<KeyPoint> keysObject = keyPointObject.toList();
        List<KeyPoint> keysScene = keyPointScene.toList();
        LinkedList<Point> listObject = new LinkedList<Point>();
        LinkedList<Point> listScene = new LinkedList<Point>();

        DMatch dMatch = null;
        List<DMatch> listGood = matOfDMatchGood.toList();

        for (int i = 0; i < listGood.size(); i++) {
            dMatch = listGood.get(i);
            listObject.add(keysObject.get(dMatch.queryIdx).pt);
            listScene.add(keysScene.get(dMatch.trainIdx).pt);
        }

        MatOfPoint2f matOfPoint2fObject = new MatOfPoint2f();
        MatOfPoint2f matOfPoint2fScene = new MatOfPoint2f();
        matOfPoint2fObject.fromList(listObject);
        matOfPoint2fScene.fromList(listScene);

        homography = Calib3d.findHomography(matOfPoint2fObject, matOfPoint2fScene, Calib3d.RANSAC, ransacReprojThreshold);

    }

    private static void selectFoundObject(Mat frameScene){
        Mat cornersObject = new Mat(4,1,CvType.CV_32FC2);
        Mat cornersScene = new Mat(4,1,CvType.CV_32FC2);

        cornersObject.put(0, 0, new double[] {0,0});
        cornersObject.put(1, 0, new double[] {frameObject.cols(),0});
        cornersObject.put(2, 0, new double[] {frameObject.cols(),frameObject.rows()});
        cornersObject.put(3, 0, new double[] {0,frameObject.rows()});

        if (!homography.empty()) {
            Core.perspectiveTransform(cornersObject,cornersScene, homography);

            Imgproc.rectangle(frameScene, new Point(cornersScene.get(0,0)),
                    new Point(cornersScene.get(2,0)), Scalar.all(-1),4);
        }

        Imgproc.line(frameScene, new Point(cornersScene.get(0,0)), new Point(cornersScene.get(1,0)), Scalar.all(-1),4);
        Imgproc.line(frameScene, new Point(cornersScene.get(1,0)), new Point(cornersScene.get(2,0)), Scalar.all(-1),4);
        Imgproc.line(frameScene, new Point(cornersScene.get(2,0)), new Point(cornersScene.get(3,0)), Scalar.all(-1),4);
        Imgproc.line(frameScene, new Point(cornersScene.get(3,0)), new Point(cornersScene.get(0,0)), Scalar.all(-1),4);

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

    public static void setMatchSelectionFactor(double matchSelectionFactor) {
        DetectorObject.matchSelectionFactor = matchSelectionFactor;
    }

    public static void setRansacReprojThreshold(double ransacReprojThreshold) {
        DetectorObject.ransacReprojThreshold = ransacReprojThreshold;
    }
}
