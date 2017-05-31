package chathead.ChatHeadUI;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.ImageView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

import chathead.ChatHeadArrangement.MaximizedArrangement;
import chathead.ChatHeadArrangement.MinimizedArrangement;
import chathead.ChatHeadManager.ChatHeadManager;
import chathead.User;
import chathead.Utils.ChatHeadUtils;
import chathead.Utils.SpringConfigsHolder;

/**
 * Created by luvikaser on 01/03/2017.
 */

public class ChatHead extends ImageView implements SpringListener {

    public final int CLOSE_ATTRACTION_THRESHOLD = ChatHeadUtils.dpToPx(getContext(), 110);
    private final int touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    private ChatHeadManager manager;
    private SpringSystem springSystem;
    private State state;
    private User user;
    private float downX = -1;
    private float downY = -1;
    private VelocityTracker velocityTracker;
    private boolean isDragging;
    private float downTranslationX;
    private float downTranslationY;
    private SpringListener xPositionListener;
    private SpringListener yPositionListener;
    private Spring scaleSpring;
    private Spring xPositionSpring;
    private Spring yPositionSpring;
    private boolean isHero;
//    public ChatHead(Context context) {
//        super(context);
//        throw new IllegalArgumentException("This constructor cannot be used");
//    }
//
//    public ChatHead(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        throw new IllegalArgumentException("This constructor cannot be used");
//    }
//
//    public ChatHead(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        throw new IllegalArgumentException("This constructor cannot be used");
//    }

    public ChatHead(ChatHeadManager manager, SpringSystem springsHolder, Context context) {
        super(context);
        this.manager = manager;
        this.springSystem = springsHolder;
        init();
    }

    public boolean isHero() {
        return isHero;
    }

    public void setHero(boolean hero) {
        isHero = hero;
    }

    public Spring getHorizontalSpring() {
        return xPositionSpring;
    }

    public Spring getVerticalSpring() {
        return yPositionSpring;
    }

    private void init() {
        xPositionListener = new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                super.onSpringUpdate(spring);
                manager.getChatHeadContainer().setViewX(ChatHead.this, (int)spring.getCurrentValue());
            }

            @Override
            public void onSpringAtRest(Spring spring) {
                super.onSpringAtRest(spring);
            }
        };
        xPositionSpring = springSystem.createSpring();
        xPositionSpring.addListener(xPositionListener);
        xPositionSpring.addListener(this);

        yPositionListener = new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                super.onSpringUpdate(spring);
                manager.getChatHeadContainer().setViewY(ChatHead.this, (int)spring.getCurrentValue());
            }

            @Override
            public void onSpringAtRest(Spring spring) {
                super.onSpringAtRest(spring);
            }
        };
        yPositionSpring = springSystem.createSpring();
        yPositionSpring.addListener(yPositionListener);
        yPositionSpring.addListener(this);

        scaleSpring = springSystem.createSpring();
        scaleSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                super.onSpringUpdate(spring);
                double currentValue = spring.getCurrentValue();
                setScaleX((float) currentValue);
                setScaleY((float) currentValue);
            }
        });
        scaleSpring.setCurrentValue(1).setAtRest();
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void onSpringUpdate(Spring spring) {
        if (xPositionSpring != null && yPositionSpring != null) {
            Spring activeHorizontalSpring = xPositionSpring;
            Spring activeVerticalSpring = yPositionSpring;
            if (spring != activeHorizontalSpring && spring != activeVerticalSpring)
                return;
            int totalVelocity = (int) Math.hypot(activeHorizontalSpring.getVelocity(), activeVerticalSpring.getVelocity());
            if (manager.getActiveArrangement() != null)
                manager.getActiveArrangement().onSpringUpdate(this, isDragging, manager.getMaxWidth(), manager.getMaxHeight(), spring, activeHorizontalSpring, activeVerticalSpring, totalVelocity);
        }
    }

    @Override
    public void onSpringAtRest(Spring spring) {

    }

    @Override
    public void onSpringActivate(Spring spring) {

    }


    @Override
    public void onSpringEndStateChange(Spring spring) {

    }


    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        super.onTouchEvent(event);
        if(xPositionSpring == null || yPositionSpring == null) return false;
        //Chathead view will set the correct active springs on touch
        Spring activeHorizontalSpring = xPositionSpring;
        Spring activeVerticalSpring = yPositionSpring;

        int action = event.getAction();
        float rawX = event.getRawX();
        float rawY = event.getRawY();
        float offsetX = rawX - downX;
        float offsetY = rawY - downY;
        event.offsetLocation(manager.getChatHeadContainer().getViewX(this), manager.getChatHeadContainer().getViewY(this));
        if (action == MotionEvent.ACTION_DOWN) {
            if (velocityTracker == null) {
                velocityTracker = VelocityTracker.obtain();
            } else {
                velocityTracker.clear();
            }
            activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
            activeVerticalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
            setState(State.FREE);
            downX = rawX;
            downY = rawY;
            downTranslationX = (float) activeHorizontalSpring.getCurrentValue();
            downTranslationY = (float) activeVerticalSpring.getCurrentValue();
            scaleSpring.setEndValue(.9f);
            activeHorizontalSpring.setAtRest();
            activeVerticalSpring.setAtRest();
            velocityTracker.addMovement(event);
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (Math.hypot(offsetX, offsetY) > touchSlop) {
                isDragging = true;
                manager.getCloseButton().appear();
            }
            velocityTracker.addMovement(event);

            if (isDragging) {
                manager.getCloseButton().pointTo(rawX, rawY);
                if (manager.getActiveArrangement().canDrag(this)) {
                    double distanceCloseButtonFromHead = manager.getDistanceCloseButtonFromHead(rawX, rawY);
                    if (distanceCloseButtonFromHead < CLOSE_ATTRACTION_THRESHOLD) {
                        setState(State.CAPTURED);
                        activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                        activeVerticalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                        int[] coords = manager.getChatHeadCoordsForCloseButton(this);
                        activeHorizontalSpring.setEndValue(coords[0]);
                        activeVerticalSpring.setEndValue(coords[1]);
                        manager.getCloseButton().onCapture();
                    } else {
                        setState(State.FREE);
                        activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.DRAGGING);
                        activeVerticalSpring.setSpringConfig(SpringConfigsHolder.DRAGGING);
                        activeHorizontalSpring.setCurrentValue(downTranslationX + offsetX);
                        activeVerticalSpring.setCurrentValue(downTranslationY + offsetY);
                        manager.getCloseButton().onRelease();
                    }

                }

            }

        } else {
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                }
                velocityTracker.computeCurrentVelocity(1000);

                boolean wasDragging = isDragging;

                activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.DRAGGING);
                activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.DRAGGING);
                isDragging = false;
                scaleSpring.setEndValue(1);
                int xVelocity = (int) velocityTracker.getXVelocity();
                int yVelocity = (int) velocityTracker.getYVelocity();
                velocityTracker.recycle();
                velocityTracker = null;
                if(xPositionSpring != null && yPositionSpring != null) {
                    manager.getActiveArrangement().handleTouchUp(this, xVelocity, yVelocity, activeHorizontalSpring, activeVerticalSpring, wasDragging);
                }
            }
        }

        return true;
    }

    public void onRemove() {
        xPositionSpring.setAtRest();
        xPositionSpring.removeAllListeners();
        xPositionSpring.destroy();
        xPositionSpring = null;
        yPositionSpring.setAtRest();
        yPositionSpring.removeAllListeners();
        yPositionSpring.destroy();
        yPositionSpring = null;
        scaleSpring.setAtRest();
        scaleSpring.removeAllListeners();
        scaleSpring.destroy();
        scaleSpring = null;
    }

    public void setImageDrawable(Drawable chatHeadDrawable) {
        super.setImageDrawable(chatHeadDrawable);
    }


    public enum State {
        FREE, CAPTURED
    }
}