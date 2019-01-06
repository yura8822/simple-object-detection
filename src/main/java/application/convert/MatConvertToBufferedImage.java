package application.convert;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class MatConvertToBufferedImage {

    public static BufferedImage MatToBufferedImage(Mat m){
        if (m == null || m.empty()) return null;
        if (m.depth() == CvType.CV_8U){}
        else if (m.depth() == CvType.CV_16U){
            Mat m_16 = new Mat();
            m.convertTo(m_16, CvType.CV_8U,255.0 / 65535);
            m = m_16;
        }
        else if (m.depth() == CvType.CV_32F){
            Mat m_32 = new Mat();
            m.convertTo(m_32, CvType.CV_8U, 255);
            m = m_32;
        }
        else return null;

        int type = 0;
        if (m.channels() == 1) type = BufferedImage.TYPE_BYTE_GRAY;       // TYPE_BYTE_GRAY
        else if (m.channels() == 3) type = BufferedImage.TYPE_3BYTE_BGR;  // TYPE_3BYTE_BGR
        else if (m.channels() == 4) type = BufferedImage.TYPE_4BYTE_ABGR; // TYPE_4BYTE_ABGR
        else  return null;

        byte[] buf = new byte[m.channels() * m.cols() * m.rows()];
        m.get(0, 0, buf);
        byte tmp = 0;
        if (m.channels() == 4){
            for (int i = 0; i < buf.length; i += 4){
                tmp = buf[i+3];
                buf[i+3] = buf[i+2];
                buf[i+2] = buf[i+1];
                buf[i+1] = buf[i];
                buf[i] = tmp;
            }
        }

        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buf, 0, data, 0, buf.length);
        return image;
    }
}