package chathead.Utils;

import android.content.Context;
import android.graphics.Point;

/**
 * Created by luvikaser on 01/03/2017.
 */

public class ChatHeadDefaultConfig extends ChatHeadConfig {
    public ChatHeadDefaultConfig(Context context) {
        int diameter = 65;
        setHeadHeight(ChatHeadUtils.dpToPx(context, diameter));
        setHeadWidth(ChatHeadUtils.dpToPx(context, diameter));
        setHeadHorizontalSpacing(ChatHeadUtils.dpToPx(context, 10));
        setHeadVerticalSpacing(ChatHeadUtils.dpToPx(context, 5));
        setInitialPosition(new Point(0, ChatHeadUtils.dpToPx(context, 0)));
        setCloseButtonHidden(false);
        setCloseButtonWidth(ChatHeadUtils.dpToPx(context, 70));
        setCloseButtonHeight(ChatHeadUtils.dpToPx(context, 70));
        setCloseButtonBottomMargin(ChatHeadUtils.dpToPx(context, 50));
        setCircularRingWidth(ChatHeadUtils.dpToPx(context, diameter + 5));
        setCircularRingHeight(ChatHeadUtils.dpToPx(context, diameter + 5));
        setMaxChatHeads(5);
    }

    @Override
    public int getCircularFanOutRadius(int maxWidth, int maxHeight) {
        return (int) (maxWidth / 2.5f);
    }
}
