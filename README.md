# Java + OpenCV 答题卡识别
---

### Demo介绍 
> * 识别静态图片答题卡，对图片要求比较高，必须A4大小，内容铺满窗口；
> * 印刷质量太差影响答案精度（已测）
> * 扫描质量太差可能影响答案精度（未测）

#### 准备工具
> *  OpenCV 3.30
> *  IntelliJ IDEA 2017.2.5 
> *  JDK 1.8_151
> *  A4公务员考试答题卡

#### 环境配置
     1. 打开IDEA 选择 File -> Project Structure -> Modules -> Add -> JARs or directories
     2. 选择 opencv 目录下的 build > java > opencv-330.jar
     3. File -> Project Structure -> Libraries -> New Project Library
     4. 选择 opencv 目录下的 build > java > x64 > opencv_java330.dll
 主要步骤
---
    1. 读取图片
    2. 图片转化为灰度图
    3. 图片设定阈值
    4. 开运算(先腐蚀，后膨胀)
    5. 指定答题区域
    6. 找到涂选框
    7. 根据涂选框包含的坐标确定所涂选的选项及题号

#### 具体实现
    
 > 1. 加载Library
   
     static {
               System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
           }
 
 >　2. 设定答题卡图片path
   
     String sheet = "E:/picpool/A4.jpg";
 
 >　3. 读取图片
    
    Mat img = Imgcodecs.imread(oriImg);
    Mat srcImage2 = new Mat();
    Mat srcImage3 = new Mat();
    Mat srcImage4 = new Mat();
    Mat srcImage5 = new Mat();
    Mat colorfulImage = new Mat();
    
 >　4. 图片变成灰度图片
    
    Imgproc.cvtColor(img, srcImage2, Imgproc.COLOR_RGB2GRAY);
    
 >　5. 图片二值化
 
    Imgproc.adaptiveThreshold(srcImage2, srcImage3, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 255, 1);
    
 >　6. 腐蚀膨胀操作（图片印刷质量太差影响精度）
 
    Mat element = Imgproc.getStructuringElement(MORPH_RECT, new Size(1, 6));
    Imgproc.erode(srcImage3, srcImage4, element);
    Imgproc.dilate(srcImage4, srcImage5, element);
  
 >　7. 指定答题区域（根据矩形覆盖面积选取，可用多个对象对多块区域进行操作）
 
    Mat imag_ch1 = srcImage4.submat(new Rect(200, 1065, 1930, 2210));
    Mat imag_ch2 = srcImage4.submat(new Rect(100, 500, 300, 1000));
    
 >　8. 识别提取轮廓，根据宽度范围找到涂选框
 
    Vector<MatOfPoint> chapter1 = new Vector<>();
    Imgproc.findContours(imag_ch1, chapter1, new Mat(), 2, 3);
    Mat result = new Mat(imag_ch1.size(), CV_8U, new Scalar(255));
    Imgproc.drawContours(colorfulImage, chapter1, -1, new Scalar(0), 2);
    
    List<RectComp> RectCompList = new ArrayList<>();
        double startX, startY, endX, endY;
        for (MatOfPoint mop : chapter1) {
            Rect rm = Imgproc.boundingRect(mop);
            RectComp ti = new RectComp(rm);
            if (ti.rm.width > 60 && ti.rm.width < 85) {
                RectCompList.add(ti);
                Imgproc.rectangle(colorfulImage, new Point(startX, startY), new Point(endX, endY), new Scalar(0, 0, 255), 2);
            }
        }
    
 >　9. 根据涂选框包含的坐标确定所涂选的选项及题号
 
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
            } 
        }
     }
     
 >　10. 输出结果   
 
    Iterator iter = listenAnswer.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            System.out.println("第" + key + "题,选项:" + val);
        }   
