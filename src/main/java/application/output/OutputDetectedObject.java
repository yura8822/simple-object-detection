package application.output;

import application.convert.MatConvertToBufferedImage;
import application.detection.DetectorObject;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Date;

public class OutputDetectedObject {

    private static boolean isRun = true;
    private static boolean isEnd = false;

    private static int cameraFrameWidth = 640;
    private static int cameraFrameHeight = 480;

    private static int videoCaptureIndex = 0;

    private static int objectTemplateCreationTime = 8000;

    private static int patternFrameWidth = 300;
    private static int patternFrameHeight = 300;

    private static int framesPerSecond = 20;


    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void start(){
        JFrame window = new JFrame("Press Esc to turn off the camera");
        window.setSize(640, 480);
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.setLocationRelativeTo(null);

        //Handling the button close in the window title
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                isRun = false;
                if (isEnd){
                    window.dispose();
                    System.exit(0);
                }
                else {
                    System.out.println("First press Esc, then close");
                }
            }
        });
        //Esc key processing
        window.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if(e.getKeyCode() == 27) {
                    isRun = false;

                }
            }
        });

        JLabel label = new JLabel();
        window.setContentPane(label);
        window.setVisible(true);

        VideoCapture camera = new VideoCapture(videoCaptureIndex);
        if (!camera.isOpened()){
            window.setTitle("Could not connect to camera");
            isRun = false;
            isEnd = true;
            return;
        }

        try {
            camera.set(Videoio.CAP_PROP_FRAME_WIDTH, cameraFrameWidth);
            camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, cameraFrameHeight);

            BufferedImage img = null;
            Mat frameScene = new Mat();
            Mat frameObject = null;


            Date date = new Date();

            while (isRun){
                if (camera.read(frameScene)){
                    // Template creation (frameObject)
                    if ((new Date().getTime()) - date.getTime() < objectTemplateCreationTime){
                        if (cameraFrameWidth != frameScene.cols() || cameraFrameHeight != frameScene.rows())
                            throw new RuntimeException("Invalid resolution. Possible variant " + frameScene.cols() + "x" + frameScene.rows());

                        Imgproc.rectangle(frameScene, new Point(cameraFrameWidth/2 - patternFrameWidth/2, cameraFrameHeight/2 + patternFrameHeight/2),
                                new Point(cameraFrameWidth/2 + patternFrameWidth/2, cameraFrameHeight/2 - patternFrameHeight/2),
                                new Scalar(0, 255, 0), 8);

                        img = MatConvertToBufferedImage.MatToBufferedImage(frameScene);

                    }else {
                        //Detection object
                        if (frameObject == null){
                            frameObject = frameScene.submat(new Rect(cameraFrameWidth/2 - patternFrameWidth/2,
                                    cameraFrameHeight/2 - patternFrameHeight/2, patternFrameWidth, patternFrameHeight)).clone();
                            DetectorObject.setFrameObject(frameObject);
                        }

                        DetectorObject.run(frameScene);

                        img = MatConvertToBufferedImage.MatToBufferedImage(frameScene);
                    }


                    if (img != null) {
                        ImageIcon imageIcon = new ImageIcon(img);
                        label.setIcon(imageIcon);
                        label.repaint();
                        window.pack();
                    }
                    try {
                        Thread.sleep(framesPerSecond);
                    } catch (InterruptedException e) {}
                }
                else {
                    System.out.println("Could not capture frame");
                    break;
                }
            }
        }

        finally {
            camera.release();
            isRun = false;
            isEnd = true;
        }
        window.setTitle("Camera off");
    }

    public static void setCameraFrameWidth(int cameraFrameWidth) {
        OutputDetectedObject.cameraFrameWidth = cameraFrameWidth;
    }

    public static void setCameraFrameHeight(int cameraFrameHeight) {
        OutputDetectedObject.cameraFrameHeight = cameraFrameHeight;
    }

    public static void setVideoCaptureIndex(int videoCaptureIndex) {
        OutputDetectedObject.videoCaptureIndex = videoCaptureIndex;
    }

    public static void setPatternFrameWidth(int patternFrameWidth) {
        if (cameraFrameWidth-cameraFrameWidth/4 > patternFrameWidth)
        OutputDetectedObject.patternFrameWidth = patternFrameWidth;
        else throw  new RuntimeException("You must set a value patternFrameWidth less than the current");

    }

    public static void setPatternFrameHeight(int patternFrameHeight) {
        if (cameraFrameHeight-cameraFrameHeight/4 > patternFrameHeight)
        OutputDetectedObject.patternFrameHeight = patternFrameHeight;
        else throw  new RuntimeException("You must set a value patternFrameHeight less than the current");
    }

    public static void setObjectTemplateCreationTime(int objectTemplateCreationTime) {
        OutputDetectedObject.objectTemplateCreationTime = objectTemplateCreationTime;
    }

    public static void setFramesPerSecond(int framesPerSecond) {
        OutputDetectedObject.framesPerSecond = 1000/framesPerSecond;
    }
}
