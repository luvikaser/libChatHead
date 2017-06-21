package chathead.ChatHeadUI.ChatHeadContainer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import chathead.ChatHeadArrangement.ChatHeadArrangement;
import chathead.ChatHeadArrangement.MaximizedArrangement;
import chathead.ChatHeadArrangement.MinimizedArrangement;
import chathead.ChatHeadManager.ChatHeadManager;
import chathead.ChatHeadUI.ChatHead;
import chathead.Utils.ChatHeadUtils;

import static android.content.Context.WINDOW_SERVICE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
import static android.view.WindowManager.LayoutParams.TYPE_PHONE;

/**
 * Created by luvikaser on 07/03/2017.
 */


public class ChatHeadContainer extends FrameChatHeadContainer {

    private boolean isDestroyed;
    private View motionCaptureView;
    private WindowManager windowManager;
    private ChatHeadArrangement currentArrangement;
    private boolean motionCaptureViewAdded = false;

    public ChatHeadContainer(Context context) {
        super(context);
    }

    @Override
    public void onInitialized(ChatHeadManager manager) {
        super.onInitialized(manager);
        isDestroyed = false;
        motionCaptureView = new MotionCaptureView(getContext());
        MotionCapturingTouchListener listener = new MotionCapturingTouchListener();
        motionCaptureView.setOnTouchListener(listener);
    }


    public WindowManager getWindowManager() {
        if (windowManager == null) {
            windowManager = (WindowManager) getContext().getSystemService(WINDOW_SERVICE);
        }
        return windowManager;
    }

    protected void setContainerHeight(View container, int height) {
        WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(container);
        layoutParams.height = height;
        getWindowManager().updateViewLayout(container, layoutParams);
    }

    protected void setContainerWidth(View container, int width) {
        WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(container);
        layoutParams.width = width;
        getWindowManager().updateViewLayout(container, layoutParams);
    }

    protected WindowManager.LayoutParams getOrCreateLayoutParamsForContainer(View container) {
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) container.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = createContainerLayoutParams(false);
            container.setLayoutParams(layoutParams);
        }
        return layoutParams;
    }

    protected void setContainerX(View container, int xPosition) {
        WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(container);
        layoutParams.x = xPosition;
        getWindowManager().updateViewLayout(container, layoutParams);
    }

    protected int getContainerX(View container) {
        WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(container);
        return layoutParams.x;
    }


    protected void setContainerY(View container, int yPosition) {
        WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(container);
        layoutParams.y = yPosition;
        getWindowManager().updateViewLayout(container, layoutParams);
    }

    protected int getContainerY(View container) {
        WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(container);
        return layoutParams.y;
    }

    protected WindowManager.LayoutParams createContainerLayoutParams(boolean focusable) {
        int focusableFlag;
        if (focusable) {
            focusableFlag = FLAG_NOT_TOUCH_MODAL;
        } else {
            focusableFlag = FLAG_NOT_TOUCHABLE | FLAG_NOT_FOCUSABLE;
        }
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(MATCH_PARENT, MATCH_PARENT,
                TYPE_PHONE,
                focusableFlag,
                PixelFormat.TRANSLUCENT);
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.gravity = Gravity.TOP | Gravity.START;
        return layoutParams;
    }

    @Override
    public void addContainer(View container, boolean focusable) {
        WindowManager.LayoutParams containerLayoutParams = createContainerLayoutParams(focusable);
        addContainer(container, containerLayoutParams);
    }

    public void addContainer(View container, WindowManager.LayoutParams containerLayoutParams) {
        container.setLayoutParams(containerLayoutParams);
        getWindowManager().addView(container, containerLayoutParams);

    }

    @Override
    public void setViewX(View view, int xPosition) {
        super.setViewX(view, xPosition);
        if (view instanceof ChatHead) {
            boolean hero = ((ChatHead) view).isHero();
            if (hero && currentArrangement instanceof MinimizedArrangement) {
                setContainerX(motionCaptureView, xPosition);
                setContainerWidth(motionCaptureView, view.getMeasuredWidth());
            }
        }
    }

    @Override
    public void setViewY(View view, int yPosition) {
        super.setViewY(view, yPosition);
        if (view instanceof ChatHead && currentArrangement instanceof MinimizedArrangement) {
            boolean hero = ((ChatHead) view).isHero();
            if (hero) {
                setContainerY(motionCaptureView, yPosition - (Build.VERSION.SDK_INT >= 21 ? ChatHeadUtils.dpToPx(getManager().getContext(), 25) : 0));
                setContainerHeight(motionCaptureView, view.getMeasuredHeight());
            }
        }
    }

    @Override
    public void onArrangementChanged(ChatHeadArrangement oldArrangement, ChatHeadArrangement newArrangement) {
        currentArrangement = newArrangement;

        if (newArrangement instanceof MaximizedArrangement) {
            // about to be maximized
            WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(motionCaptureView);
            layoutParams.flags |= FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCHABLE;
            windowManager.updateViewLayout(motionCaptureView, layoutParams);

            layoutParams = getOrCreateLayoutParamsForContainer(getFrameLayout());
            layoutParams.flags &= ~FLAG_NOT_FOCUSABLE; //add focusability
            layoutParams.flags &= ~FLAG_NOT_TOUCHABLE; //add focusability
            layoutParams.flags |= FLAG_NOT_TOUCH_MODAL;
            windowManager.updateViewLayout(getFrameLayout(), layoutParams);
            setContainerX(motionCaptureView, 0);
            setContainerY(motionCaptureView, 0);
            setContainerWidth(motionCaptureView, getFrameLayout().getMeasuredWidth());
            setContainerHeight(motionCaptureView, getFrameLayout().getMeasuredHeight());

        } else {
            // about to be minimized
            WindowManager.LayoutParams layoutParams = getOrCreateLayoutParamsForContainer(motionCaptureView);
            layoutParams.flags |= FLAG_NOT_FOCUSABLE; //remove focusability
            layoutParams.flags &= ~FLAG_NOT_TOUCHABLE; //add touch
            layoutParams.flags |= FLAG_NOT_TOUCH_MODAL; //add touch
            if (motionCaptureViewAdded) {
                windowManager.updateViewLayout(motionCaptureView, layoutParams);
            }

            layoutParams = getOrCreateLayoutParamsForContainer(getFrameLayout());
            layoutParams.flags |= FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCHABLE;
            windowManager.updateViewLayout(getFrameLayout(), layoutParams);
        }
    }

    @Override
    public void addView(View view, ViewGroup.LayoutParams layoutParams) {
        super.addView(view, layoutParams);
        if (!motionCaptureViewAdded && getManager().getChatHeads().size() > 0) {
            addContainer(motionCaptureView, true);
            WindowManager.LayoutParams motionCaptureParams = getOrCreateLayoutParamsForContainer(motionCaptureView);
            motionCaptureParams.width = 0;
            motionCaptureParams.height = 0;
            windowManager.updateViewLayout(motionCaptureView, motionCaptureParams);
            motionCaptureViewAdded = true;
        }
    }

    @Override
    public void removeView(View view) {
        super.removeView(view);
        if (getManager().getChatHeads().size() <= 0) {
            windowManager.removeViewImmediate(motionCaptureView);
            windowManager.removeView(getFrameLayout());
            motionCaptureViewAdded = false;
            isDestroyed = true;
        }
    }


    public void setVisibility(int visibility){
        getFrameLayout().setVisibility(visibility);
        motionCaptureView.setVisibility(visibility);
    }
    public boolean isDestroyed(){
        return isDestroyed;
    }


    protected class MotionCapturingTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            event.offsetLocation(getContainerX(v), getContainerY(v) + (Build.VERSION.SDK_INT >= 21 ? ChatHeadUtils.dpToPx(getManager().getContext(), 25) : 0));
            HostFrameLayout frameLayout = getFrameLayout();
            if (frameLayout != null) {
                return frameLayout.dispatchTouchEvent(event);
            } else {
                return false;
            }
        }

    }

    private class MotionCaptureView extends View {
        public MotionCaptureView(Context context) {
            super(context);
        }

    }
}