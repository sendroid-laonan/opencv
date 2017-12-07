package OpencvDemo;

import org.opencv.core.Rect;

public class RectComp {
    Rect rm;

    RectComp(Rect rms) {
        rm = rms;
    }

    final boolean operator (RectComp ti)
    {
        return rm.x == ti.rm.x;
    }


}