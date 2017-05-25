package chathead.ChatHeadUI;

import android.content.Context;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;

import chathead.ChatHeadManager.ChatHeadManager;
import chathead.Utils.SpringConfigsHolder;
import nhutlm2.fresher.demochathead.R;

/**
 * Created by luvikaser on 01/03/2017.
 */

public class ChatHeadCloseButton extends ImageView {

    private static final float PERC_PARENT_WIDTH = 0.1f; //perc of parent to be covered during drag
    private static final float PERC_PARENT_HEIGHT = 0.05f; //perc of parent to be covered during drag
    private int mParentWidth;
    private int mParentHeight;
    private Spring scaleSpring;
    private Spring xSpring;
    private Spring ySpring;
    private boolean disappeared;
    private ChatHeadManager chatHeadManager;
    private int centerX;
    private int centerY;

    public ChatHeadCloseButton(Context context, ChatHeadManager manager) {
        super(context);
        init(manager);
    }

    public boolean isDisappeared() {
        return disappeared;
    }

    private void init(final ChatHeadManager manager) {
        this.chatHeadManager = manager;

        setImageResource(R.drawable.dismiss_big);
        SpringSystem springSystem = SpringSystem.create();
        xSpring = springSystem.createSpring();
        xSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                super.onSpringUpdate(spring);
                int x = getXFromSpring(spring);
                manager.getChatHeadContainer().setViewX(ChatHeadCloseButton.this, x);
            }
        });
        ySpring = springSystem.createSpring();
        ySpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                super.onSpringUpdate(spring);
                int y = getYFromSpring(spring);
                manager.getChatHeadContainer().setViewY(ChatHeadCloseButton.this, y);
            }
        });
        scaleSpring = springSystem.createSpring();
        scaleSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                double currentValue = spring.getCurrentValue();
                setScaleX((float) currentValue);
                setScaleY((float) currentValue);
            }
        });
        setEnabled(true);
    }


    private int getYFromSpring(Spring spring) {
        return centerY + (int) spring.getCurrentValue() - getMeasuredHeight() / 2;
    }

    private int getXFromSpring(Spring spring) {
        return centerX + (int) spring.getCurrentValue() - getMeasuredWidth() / 2;
    }

    //Close button appears
    public void appear() {
        if (isEnabled()) {
            ySpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
            xSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
            scaleSpring.setEndValue(.8f);
            ViewParent parent = getParent();
            if (parent instanceof ViewGroup) {
                int i = ((ViewGroup) parent).indexOfChild(this);
                if (i != ((ViewGroup) parent).getChildCount() - 1) {
                    bringToFront();
                }
            }
            disappeared = false;
        }
    }

    //Close button disappears
    public void disappear() {
        ySpring.setEndValue(mParentHeight - centerY + chatHeadManager.getConfig().getCloseButtonHeight());
        ySpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
        xSpring.setEndValue(0);
        ySpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringAtRest(Spring spring) {
                super.onSpringAtRest(spring);
                ySpring.removeListener(this);
            }
        });
        scaleSpring.setEndValue(0.1f);
        disappeared = true;

    }

    //when close button is captured
    public void onCapture() {
        scaleSpring.setEndValue(1);
    }

    //when close button is released
    public void onRelease() {
        scaleSpring.setEndValue(0.8);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        disappear();
    }

    public void onParentHeightRefreshed() {
        mParentWidth = chatHeadManager.getMaxWidth();
        mParentHeight = chatHeadManager.getMaxHeight();
    }

    //set center position for close button when onMeasure
    public void setCenter(int x, int y) {
        boolean changed = false;
        if (x != centerX || y != centerY) {
            changed = true;
        }
        if(changed) {
            this.centerX = x;
            this.centerY = y;
            xSpring.setCurrentValue(0, false);
            ySpring.setCurrentValue(0, false);
        }
    }

    //update endValue for close button translation when dragging chatHead
    public void pointTo(float x, float y) {
        if (isEnabled()) {
            double translationX = getTranslationFromSpring(x, PERC_PARENT_WIDTH, mParentWidth);
            double translationY = getTranslationFromSpring(y, PERC_PARENT_HEIGHT, mParentHeight);
            if (!disappeared) {
                xSpring.setEndValue(translationX);
                ySpring.setEndValue(translationY);
            }
        }
    }

    private double getTranslationFromSpring(double springValue, float percent, int fullValue) {
        float widthToCover = percent * fullValue;
        return SpringUtil.mapValueFromRangeToRange(springValue, 0, fullValue, -widthToCover / 2, widthToCover / 2);
    }

    public int getEndValueX() {
        return getXFromSpring(xSpring);
    }

    public int getEndValueY() {
        return getYFromSpring(ySpring);
    }


}
