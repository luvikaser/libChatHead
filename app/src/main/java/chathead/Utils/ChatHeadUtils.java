package chathead.Utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

/**
 * Created by luvikaser on 01/03/2017.
 */

public class ChatHeadUtils {


    public static int dpToPx(DisplayMetrics metrics, int dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
        if (px < 1.0f) {
            px = 1;
        }
        return (int) px;
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        return dpToPx(metrics, dp);
    }
}
