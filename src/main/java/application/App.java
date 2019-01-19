package application;

import application.detection.DetectorObject;
import application.detection.DetectorType;
import application.detection.MatcherType;
import application.output.OutputDetectedObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class App {
    public static void main(String[] args) {

        FileInputStream fileInputStream;
        Properties properties = new Properties();

        try {
            fileInputStream = new FileInputStream(new File("").getAbsoluteFile()
                    + "/src/main/java/application/properties.txt");
            properties.load(fileInputStream);
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        OutputDetectedObject.setVideoCaptureIndex(Integer.parseInt(properties.getProperty("VideoCaptureIndex")));

        OutputDetectedObject.setCameraFrameWidth(Integer.parseInt(properties.getProperty("CameraFrameWidth")));
        OutputDetectedObject.setCameraFrameHeight(Integer.parseInt(properties.getProperty("CameraFrameHeight")));

        OutputDetectedObject.setPatternFrameWidth(Integer.parseInt(properties.getProperty("PatternFrameWidth")));
        OutputDetectedObject.setPatternFrameHeight(Integer.parseInt(properties.getProperty("PatternFrameHeight")));

        OutputDetectedObject.setObjectTemplateCreationTime(Integer.parseInt(properties.getProperty("ObjectTemplateCreationTime")));

        OutputDetectedObject.setFramesPerSecond(Integer.parseInt(properties.getProperty("FramesPerSecond")));

        if (properties.getProperty("KeyPointsDetector").equals("AKAZE"))
            DetectorObject.setKeyPointsDetector(DetectorType.AKAZE);
        else if (properties.getProperty("KeyPointsDetector").equals("BRISK"))
            DetectorObject.setKeyPointsDetector(DetectorType.BRISK);
        else if (properties.getProperty("KeyPointsDetector").equals("ORB"))
            DetectorObject.setKeyPointsDetector(DetectorType.ORB);

        if (properties.getProperty("DescriptorMatcher").equals("BRUTEFORCE"))
            DetectorObject.setDescriptorMatcher(MatcherType.BRUTEFORCE);
        else if (properties.getProperty("DescriptorMatcher").equals("BRUTEFORCE L1"))
            DetectorObject.setDescriptorMatcher(MatcherType.BRUTEFORCE_L1);
        else if (properties.getProperty("DescriptorMatcher").equals("BRUTEFORCE HAMMING"))
            DetectorObject.setDescriptorMatcher(MatcherType.BRUTEFORCE_HAMMING);
        else if (properties.getProperty("DescriptorMatcher").equals("BRUTEFORCE HAMMINGLUT"))
            DetectorObject.setDescriptorMatcher(MatcherType.BRUTEFORCE_HAMMINGLUT);
        else if (properties.getProperty("DescriptorMatcher").equals("BRUTEFORCE SL2"))
            DetectorObject.setDescriptorMatcher(MatcherType.BRUTEFORCE_SL2);

        DetectorObject.setMatchSelectionFactor(Double.parseDouble(properties.getProperty("MatchSelectionFactor")));

        DetectorObject.setRansacReprojThreshold(Double.parseDouble(properties.getProperty("RansacReprojThreshold")));

        if (properties.getProperty("DrawSquare").equals("true"))
            DetectorObject.setDrawSquare(true);
        else DetectorObject.setDrawSquare(false);

        if (properties.getProperty("DrawLines").equals("true"))
            DetectorObject.setDrawLines(true);
        else DetectorObject.setDrawLines(false);

        if (properties.getProperty("ColorBGR2GRAY").equals("true"))
            DetectorObject.setColorBGR2GRAY(true);
        else DetectorObject.setColorBGR2GRAY(false);


        if (properties.getProperty("FilterBlur").equals("true"))
            DetectorObject.setFilterBlur(true, Float.parseFloat(properties.getProperty("FilterCoreSizeBlur")));
        else DetectorObject.setFilterBlur(false, Float.parseFloat(properties.getProperty("FilterCoreSizeBlur")));

        if (properties.getProperty("GaussianBlur").equals("true"))
            DetectorObject.setGaussianBlur(true, Float.parseFloat(properties.getProperty("FilterCoreSizeGaussianBlur")));
        else DetectorObject.setGaussianBlur(false, Float.parseFloat(properties.getProperty("FilterCoreSizeGaussianBlur")));

        if (properties.getProperty("MedianBlur").equals("true"))
            DetectorObject.setMedianBlur(true, Integer.parseInt(properties.getProperty("FilterCoreSizeMedianBlur")));
        else DetectorObject.setMedianBlur(false, Integer.parseInt(properties.getProperty("FilterCoreSizeMedianBlur")));

        if (properties.getProperty("Dilate").equals("true"))
            DetectorObject.setDilate(true, Integer.parseInt(properties.getProperty("FilterCoreSizeDilate")));
        else DetectorObject.setDilate(false, Integer.parseInt(properties.getProperty("FilterCoreSizeDilate")));

        if (properties.getProperty("Erode").equals("true"))
            DetectorObject.setErode(true, Integer.parseInt(properties.getProperty("FilterCoreSizeErode")));
        else DetectorObject.setErode(false, Integer.parseInt(properties.getProperty("FilterCoreSizeErode")));

        OutputDetectedObject.start();
    }
}
