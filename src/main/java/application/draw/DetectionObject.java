package application.draw;

import application.convert.MatConvertToBufferedImage;
import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public class DetectionObject {

    private boolean isRun = true;
    private boolean isEnd = false;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public void start(){
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

        VideoCapture camera = new VideoCapture(1);
        if (!camera.isOpened()){
            window.setTitle("Could not connect to camera");
            isRun = false;
            isEnd = true;
            return;
        }

        try {
            camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
            camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);

            BufferedImage img = null;
            Mat frame = new Mat();

            while (isRun){
                if (camera.read(frame)) {

                    //code

                    img = MatConvertToBufferedImage.MatToBufferedImage(frame);

                    if (img != null) {
                        ImageIcon imageIcon = new ImageIcon(img);
                        label.setIcon(imageIcon);
                        label.repaint();
                        window.pack();
                    }
                    try {
                        Thread.sleep(66);
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
}
