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
    private static boolean drawSquare = true;
    private static boolean drawLines = true;
    private static boolean colorBGR2GRAY = true;
    //filters
    private static boolean blur = false;
    private static float filterCoreSizeBlur = 3;

    private static boolean gaussianBlur = false;
    private static float filterCoreSizeGaussianBlur = 3;

    private static boolean medianBlur = false;
    private static int filterCoreSizeMedianBlur = 3;

    private static boolean dilate = false;
    private static int filterCoreSizeDilate = 3;

    private static boolean erode = false;
    private static int filterCoreSizeErode = 3;



    public static void run(Mat frameScene){
        if (frameObject == null)
            throw new RuntimeException("Set the value for frameObject (DetectorObject.setFrameObject())");
        if (descriptorsObject == null){
            if (colorBGR2GRAY) Imgproc.cvtColor(frameObject, frameObject, Imgproc.COLOR_BGR2GRAY);

            if (blur) Imgproc.blur(frameObject, frameObject, new Size(filterCoreSizeBlur, filterCoreSizeBlur));

            if (gaussianBlur) Imgproc.GaussianBlur(frameObject, frameObject,
                    new Size(filterCoreSizeGaussianBlur, filterCoreSizeGaussianBlur), 0);

            if (medianBlur) Imgproc.medianBlur(frameObject, frameObject, filterCoreSizeMedianBlur);

            if (dilate) Imgproc.dilate(frameObject, frameObject,
                    Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(filterCoreSizeDilate, filterCoreSizeDilate)));

            if (erode) Imgproc.erode(frameObject, frameObject,
                    Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(filterCoreSizeErode, filterCoreSizeErode)));

            keyPointsDetector.detect(frameObject, keyPointObject);
            descriptorsObject = new Mat();
            keyPointsDetector.compute(frameObject, keyPointObject, descriptorsObject);
        }

        if (colorBGR2GRAY) Imgproc.cvtColor(frameScene, frameScene, Imgproc.COLOR_BGR2GRAY);

        if (blur) Imgproc.blur(frameScene, frameScene, new Size(filterCoreSizeBlur, filterCoreSizeBlur));

        if (gaussianBlur) Imgproc.GaussianBlur(frameScene, frameScene,
                new Size(filterCoreSizeGaussianBlur, filterCoreSizeGaussianBlur), 0);

        if (medianBlur) Imgproc.medianBlur(frameScene, frameScene, filterCoreSizeMedianBlur);

        if (dilate) Imgproc.dilate(frameScene, frameScene,
                Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(filterCoreSizeDilate, filterCoreSizeDilate)));

        if (erode) Imgproc.erode(frameScene, frameScene,
                Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(filterCoreSizeErode, filterCoreSizeErode)));

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

            if (drawSquare) {
                Imgproc.rectangle(frameScene, new Point(cornersScene.get(0,0)),
                        new Point(cornersScene.get(2,0)), Scalar.all(-1),4);
            }

            if (drawLines) {
                Imgproc.line(frameScene, new Point(cornersScene.get(0,0)),
                        new Point(cornersScene.get(1,0)), Scalar.all(-1),4);
                Imgproc.line(frameScene, new Point(cornersScene.get(1,0)),
                        new Point(cornersScene.get(2,0)), Scalar.all(-1),4);
                Imgproc.line(frameScene, new Point(cornersScene.get(2,0)),
                        new Point(cornersScene.get(3,0)), Scalar.all(-1),4);
                Imgproc.line(frameScene, new Point(cornersScene.get(3,0)),
                        new Point(cornersScene.get(0,0)), Scalar.all(-1),4);
            }
        }
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

    public static void setDrawSquare(boolean drawSquare) {
        DetectorObject.drawSquare = drawSquare;
    }

    public static void setDrawLines(boolean drawLines) {
        DetectorObject.drawLines = drawLines;
    }

    public static void setColorBGR2GRAY(boolean colorBGR2GRAY) {
        DetectorObject.colorBGR2GRAY = colorBGR2GRAY;
    }

    public static void setfilterBlur(boolean blur, float filterCoreSizeBlur){
        DetectorObject.blur = blur;
        DetectorObject.filterCoreSizeBlur = filterCoreSizeBlur;
    }

    public static void setGaussianBlur(boolean gaussianBlur, float filterCoreSizeGaussianBlur){
        DetectorObject.gaussianBlur = gaussianBlur;
        DetectorObject.filterCoreSizeGaussianBlur = filterCoreSizeGaussianBlur;
    }

    public static void setMedianBlur(boolean medianBlur, int filterCoreSizeMedianBlur){
        DetectorObject.medianBlur = medianBlur;
        DetectorObject.filterCoreSizeMedianBlur = filterCoreSizeMedianBlur;
    }

    public static void setDilate(boolean dilate, int filterCoreSizeDilate){
        DetectorObject.dilate = dilate;
        DetectorObject.filterCoreSizeDilate = filterCoreSizeDilate;
    }

    public static void setErode(boolean erode, int filterCoreSizeErode){
        DetectorObject.erode = erode;
        DetectorObject.filterCoreSizeErode = filterCoreSizeErode;
    }
}
