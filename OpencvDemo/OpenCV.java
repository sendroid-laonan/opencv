package com.sendroids.opencv;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.*;

import static org.opencv.core.CvType.CV_8U;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;

public class OpenCV {
    static {
        //OpenCV -ver 3.30
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String args[]) {
        String sheet = "E:/picpool/A4.jpg";
        String results = "E:/picpool/card/enresults.jpg";
        String msg = rowsAndCols(sheet, results);
        System.out.println(msg);
    }

    private static void Canny(String oriImg) {
        //装载图片
        Mat img = Imgcodecs.imread(oriImg);
        Mat srcImage2 = new Mat();
        Mat srcImage3 = new Mat();
        Mat srcImage4 = new Mat();
        Mat srcImage5 = new Mat();
        Mat colorfulImage = new Mat();

        //图片变成灰度图片
        Imgproc.cvtColor(img, srcImage2, Imgproc.COLOR_RGB2GRAY);
        //图片二值化
        Imgproc.adaptiveThreshold(srcImage2, srcImage3, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 255, 1);
        //确定腐蚀和膨胀核的大小
        Mat element = Imgproc.getStructuringElement(MORPH_RECT, new Size(1, 6));
        //腐蚀操作
        Imgproc.erode(srcImage3, srcImage4, element);
        //膨胀操作
        Imgproc.dilate(srcImage4, srcImage5, element);
        //输出第一次操作后的效果图 实际开发请省略掉
        Imgcodecs.imwrite("E:/picpool/card/enresults.jpg", srcImage4);

        //确定每张答题卡的ROI区域
        Mat imag_ch1 = srcImage4.submat(new Rect(200, 1065, 1930, 2210));
        //输出ROI区域图 实际开发请省略掉
        Imgcodecs.imwrite("E:/picpool/card/imag_ch1.jpg", imag_ch1);


        //识别所有轮廓
        Vector<MatOfPoint> chapter1 = new Vector<>();
        Imgproc.findContours(imag_ch1, chapter1, new Mat(), 2, 3);
        Mat result = new Mat(imag_ch1.size(), CV_8U, new Scalar(255));
        //转换色域 GRAY --> RGB
        Imgproc.cvtColor(result, colorfulImage, Imgproc.COLOR_GRAY2RGB);
        //画出轮廓
        Imgproc.drawContours(colorfulImage, chapter1, -1, new Scalar(0), 2);

        //new一个 矩形集合 用来装 轮廓
        List<RectComp> RectCompList = new ArrayList<>();
        double startX, startY, endX, endY;
        for (MatOfPoint mop : chapter1) {
            Rect rm = Imgproc.boundingRect(mop);
            RectComp ti = new RectComp(rm);
            //把轮廓宽度区间在 50 - 80 范围内的轮廓装进矩形集合
            if (ti.rm.width > 60 && ti.rm.width < 85) {
                RectCompList.add(ti);
                //在选中的轮廓外加红色矩形框 +1 -1 按需求更改
                startX = ti.rm.x - 1;    //起始X
                startY = ti.rm.y - 1;    //起始Y
                endX = ti.rm.x + ti.rm.width + 1;   //结束X
                endY = ti.rm.y + ti.rm.height + 1;  //结束Y
                Imgproc.rectangle(colorfulImage, new Point(startX, startY), new Point(endX, endY), new Scalar(0, 0, 255), 2);
            }
        }

        //输出画好外框的效果图
        Imgcodecs.imwrite("E:/picpool/card/result.jpg", colorfulImage);


        //new一个 map 用来存储答题卡上填的答案 (A/B/C/D)
        TreeMap<Integer, String> listenAnswer = new TreeMap<>();
        //按 X轴 对listenAnswer进行排序
        RectCompList.sort((o1, o2) -> {
            if (o1.rm.x > o2.rm.x) {
                return 1;
            }
            if (o1.rm.x == o2.rm.x) {
                return 0;
            }
            if (o1.rm.x < o2.rm.x) {
                return -1;
            }
            return -1;
        });

        //根据 X轴确定题号 Y轴确定被选择答案 (A/B/C/D)  待优化
        for (RectComp rc : RectCompList) {

            for (int h = 0; h < 7; h++) {
                if ((rc.rm.contains(new Point(rc.rm.x + 20, 115 + (320 * h))))) {
                    for (int w = 0; w < 4; w++) {
                        if (rc.rm.contains(new Point(55 + (500 * w), rc.rm.y))) {
                            listenAnswer.put(1 + (20 * h) + (5 * w), "A");
                        } else if (rc.rm.contains(new Point(135 + (500 * w), rc.rm.y))) {
                            listenAnswer.put(2 + (20 * h) + (5 * w), "A");
                        } else if (rc.rm.contains(new Point(215 + (500 * w), rc.rm.y))) {
                            listenAnswer.put(3 + (20 * h) + (5 * w), "A");
                        } else if (rc.rm.contains(new Point(300 + (500 * w), rc.rm.y))) {
                            listenAnswer.put(4 + (20 * h) + (5 * w), "A");
                        } else if (rc.rm.contains(new Point(380 + (500 * w), rc.rm.y))) {
                            listenAnswer.put(5 + (20 * h) + (5 * w), "A");
                        }
                    }
                } else if ((rc.rm.contains(new Point(rc.rm.x + 20, 165 + (320 * h))))) {
                    for (int w = 0; w < 4; w++) {
                        if (rc.rm.contains(new Point(55 + (500 * w), rc.rm.y))) {
                            listenAnswer.put(1 + (20 * h) + (5 * w), "B");
                        } else if (rc.rm.contains(new Point(135 + (500 * w), rc.rm.y))) {
                            listenAnswer.put(2 + (20 * h) + (5 * w), "B");
                        } else if (rc.rm.contains(new Point(215 + (500 * w), rc.rm.y))) {
                            listenAnswer.put(3 + (20 * h) + (5 * w), "B");
                        } else if (rc.rm.contains(new Point(300 + (500 * w), rc.rm.y))) {
                            listenAnswer.put(4 + (20 * h) + (5 * w), "B");
                        } else if (rc.rm.contains(new Point(380 + (500 * w), rc.rm.y))) {
                            listenAnswer.put(5 + (20 * h) + (5 * w), "B");
                        }
                    }
                } else if ((rc.rm.contains(new Point(rc.rm.x + 20, 220 + (320 * h))))) {
                    for (int w = 0; w < 4; w++) {
                        if (rc.rm.contains(new Point(55 + (500 * w), rc.rm.y))) {
                            listenAnswer.put(1 + (20 * h) + (5 * w), "C");
                        } else if (rc.rm.contains(new Point(135 + (500 * w), rc.rm.y))) {
                            listenAnswer.put(2 + (20 * h) + (5 * w), "C");
                        } else if (rc.rm.contains(new Point(215 + (500 * w), rc.rm.y))) {
                            listenAnswer.put(3 + (20 * h) + (5 * w), "C");
                        } else if (rc.rm.contains(new Point(300 + (500 * w), rc.rm.y))) {
                            listenAnswer.put(4 + (20 * h) + (5 * w), "C");
                        } else if (rc.rm.contains(new Point(380 + (500 * w), rc.rm.y))) {
                            listenAnswer.put(5 + (20 * h) + (5 * w), "C");
                        }
                    }
                } else if ((rc.rm.contains(new Point(rc.rm.x + 20, 275 + (320 * h))))) {
                    for (int w = 0; w < 4; w++) {
                        if (rc.rm.contains(new Point(55 + (500 * w), rc.rm.y))) {
                            listenAnswer.put(1 + (20 * h) + (5 * w), "D");
                        } else if (rc.rm.contains(new Point(135 + (500 * w), rc.rm.y))) {
                            listenAnswer.put(2 + (20 * h) + (5 * w), "D");
                        } else if (rc.rm.contains(new Point(215 + (500 * w), rc.rm.y))) {
                            listenAnswer.put(3 + (20 * h) + (5 * w), "D");
                        } else if (rc.rm.contains(new Point(300 + (500 * w), rc.rm.y))) {
                            listenAnswer.put(4 + (20 * h) + (5 * w), "D");
                        } else if (rc.rm.contains(new Point(380 + (500 * w), rc.rm.y))) {
                            listenAnswer.put(5 + (20 * h) + (5 * w), "D");
                        }
                    }
                }
            }
        }

        //最终识别出的题号和选项
        Iterator iter = listenAnswer.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            System.out.println("第" + key + "题,选项:" + val);
        }

    }

    private static String rowsAndCols(String oriImg, String dstImg) {
        String msg = "";

        Canny(oriImg);

        Mat mat = Imgcodecs.imread(dstImg);
        msg += "\n行数:" + mat.rows();
        msg += "\n列数:" + mat.cols();
        msg += "\nheight:" + mat.height();
        msg += "\nwidth:" + mat.width();
        msg += "\nelemSide:" + mat.elemSize();

        return msg;
    }
}
