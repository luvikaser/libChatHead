package chathead.ChatHeadUI.ChatHeadContainer;

import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import chathead.ChatHeadArrangement.ChatHeadArrangement;
import chathead.ChatHeadManager.ChatHeadManager;

/**
 * Created by luvikaser on 07/03/2017.
 */

public interface ChatHeadContainerListener {
    void onInitialized(ChatHeadManager manager);

    DisplayMetrics getDisplayMetrics();

    ViewGroup.LayoutParams createLayoutParams(int height, int width, int gravity, int bottomMargin);

    void setViewX(View view, int xPosition);

    void setViewY(View view, int yPosition);

    int getViewX(View view);

    int getViewY(View view);

    void bringToFront(View view);

    void addView(View view, ViewGroup.LayoutParams layoutParams);

    void removeView(View view);

    void onArrangementChanged(ChatHeadArrangement oldArrangement, ChatHeadArrangement newArrangement);

    void requestLayout();
}