package chathead.Utils;

import com.facebook.rebound.SpringConfig;

/**
 * Created by luvikaser on 01/03/2017.
 */

public class SpringConfigsHolder {
    public static SpringConfig NOT_DRAGGING = SpringConfig.fromOrigamiTensionAndFriction(150D, 18D);
    public static SpringConfig CAPTURING = SpringConfig.fromOrigamiTensionAndFriction(100D, 10D);
    public static SpringConfig DRAGGING = SpringConfig.fromOrigamiTensionAndFriction(0D, 7D);
}
