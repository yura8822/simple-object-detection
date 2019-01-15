package application;

import application.detection.DetectorObject;
import application.detection.DetectorType;
import application.detection.MatcherType;
import application.output.OutputDetectedObject;

public class App {
    public static void main(String[] args) {
        OutputDetectedObject.setVideoCaptureIndex(1);

        OutputDetectedObject.setCameraFrameWidth(640);
        OutputDetectedObject.setCameraFrameHeight(480);

        OutputDetectedObject.setPatternFrameWidth(160);
        OutputDetectedObject.setPatternFrameHeight(160);

        OutputDetectedObject.setObjectTemplateCreationTime(4000);

        OutputDetectedObject.setFramesPerSecond(30);


        DetectorObject.setKeyPointsDetector(DetectorType.AKAZE);
        DetectorObject.setDescriptorMatcher(MatcherType.BRUTEFORCE_HAMMING);
        DetectorObject.setMatchSelectionFactor(3);
        DetectorObject.setRansacReprojThreshold(3);
        DetectorObject.setColorBGR2GRAY(true);

        OutputDetectedObject.start();
    }
}
