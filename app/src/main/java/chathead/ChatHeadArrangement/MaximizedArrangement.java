package chathead.ChatHeadArrangement;

import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.util.ArrayMap;
import android.view.View;

import com.facebook.rebound.Spring;
import java.util.List;
import java.util.Map;

import chathead.ChatHeadManager.ChatHeadManager;
import chathead.ChatHeadUI.ChatHead;
import chathead.ChatHeadUI.PopupFragment.UpArrowLayout;
import chathead.Utils.ChatHeadConfig;
import chathead.Utils.ChatHeadUtils;
import chathead.Utils.SpringConfigsHolder;

/**
 * Created by luvikaser on 07/03/2017.
 */

public class MaximizedArrangement extends ChatHeadArrangement {
    public static final String BUNDLE_HERO_INDEX_KEY = "hero_index";
    private static double MAX_DISTANCE_FROM_ORIGINAL;
    private static int MIN_VELOCITY_TO_POSITION_BACK;
    private final Map<ChatHead, Point> positions = new ArrayMap<>();
    private ChatHeadManager manager;
    private UpArrowLayout arrowLayout;
    private int maxWidth;
    private int maxHeight;
    private ChatHead currentChatHead = null;
    private int topPadding;
    private Bundle extras;
    private int maxDistanceFromOriginal;
    private boolean needAnimation = false;
    public State state = State.FREE;

    public enum State {
        FREE, REMOVE, CANCEL_REMOVE, POINT_TO, OPEN, SWITCH_TAB
    }
    public MaximizedArrangement(ChatHeadManager manager) {
        this.manager = manager;
    }

    @Override
    public void onActivate(ChatHeadManager container, Bundle extras, int maxWidth, int maxHeight, boolean bringToFront, ChatHead activeChatHead) {
        this.manager = container;
        this.extras = extras;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;

        MIN_VELOCITY_TO_POSITION_BACK = ChatHeadUtils.dpToPx(container.getDisplayMetrics(), 50);
        MAX_DISTANCE_FROM_ORIGINAL = ChatHeadUtils.dpToPx(container.getContext(), 10);

        List<ChatHead> chatHeads = manager.getChatHeads();

        int heroIndex = 0;
        if (extras != null)
            heroIndex = extras.getInt(BUNDLE_HERO_INDEX_KEY, -1);
        if (heroIndex < 0 && currentChatHead != null) {
            heroIndex = getHeroIndex(); //this means we have a current chat head and we carry it forward
        }
        if (heroIndex < 0 || heroIndex > chatHeads.size() - 1) {
            heroIndex = chatHeads.size() - 1;
        }

        if (chatHeads.size() > 0 && heroIndex < chatHeads.size() && heroIndex >= 0) {
            currentChatHead = chatHeads.get(heroIndex);
            maxDistanceFromOriginal = (int) MAX_DISTANCE_FROM_ORIGINAL;

            int spacing = container.getConfig().getHeadHorizontalSpacing();
            int widthPerHead = container.getConfig().getHeadWidth();
            topPadding = ChatHeadUtils.dpToPx(container.getContext(), Build.VERSION.SDK_INT >= 21 ? 25 : 0);
            int leftIndent = maxWidth - (chatHeads.size() * (widthPerHead + spacing));
            for (int i = 0; i < chatHeads.size(); i++) {
                ChatHead chatHead = chatHeads.get(i);
                if (chatHead.getUser().block){
                    chatHead.setVisibility(View.VISIBLE);
                }
                Spring horizontalSpring = chatHead.getHorizontalSpring();
                int xPos = leftIndent + (i * (widthPerHead + spacing));//align right
                positions.put(chatHead, new Point(xPos, topPadding));
                horizontalSpring.setAtRest();
                horizontalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                horizontalSpring.setEndValue(xPos);


                Spring verticalSpring = chatHead.getVerticalSpring();
                verticalSpring.setAtRest();
                verticalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                verticalSpring.setEndValue(topPadding);
            }

            if (bringToFront){
                state = State.OPEN;
                if (activeChatHead == null) {
                    activeChatHead = currentChatHead;
                }
            } else {
                state = State.POINT_TO;
                activeChatHead = currentChatHead;
            }
            animation(activeChatHead);
        }
    }

    public void animation(ChatHead activeChatHead) {
        switch (state){
            case OPEN: //State open and select new chatHead
                selectTab(activeChatHead); //loop
                break;
            case SWITCH_TAB: //State open chatHead is existed or state select chatHead another
                selectTab(activeChatHead);
                state = State.FREE;
                break;
            case REMOVE: //State hide arrowlayout when dragging currentChatHead
                hideView();
                state = State.FREE;
                break;
            case CANCEL_REMOVE: //State when ACTION_UP currentChatHead, reopen arrowlayout
                showOrHideView(activeChatHead); //loop
                break;
            case POINT_TO: //State open and not select chatHead
                pointTo(activeChatHead);
                state = State.FREE;
                break;
            case FREE:
                break;
        }
    }

    public void switchTab(ChatHead activeChatHead){
        state = State.SWITCH_TAB;
        animation(activeChatHead);
    }

    @Override
    public void onDeactivate(int maxWidth, int maxHeight) {
        if (currentChatHead != null) {
            manager.detachView(currentChatHead, getArrowLayout());
        }
        hideView();
        positions.clear();
    }

    private UpArrowLayout getArrowLayout() {
        if (arrowLayout == null) {
            arrowLayout = manager.getArrowLayout();
        }
        return arrowLayout;
    }
    @Override
    public boolean handleTouchUp(ChatHead activeChatHead, int xVelocity, int yVelocity, Spring activeHorizontalSpring, Spring activeVerticalSpring, boolean wasDragging) {


        if (xVelocity == 0 && yVelocity == 0) {
            xVelocity = 1;
            yVelocity = 1;
        }

        activeHorizontalSpring.setVelocity(xVelocity);
        activeVerticalSpring.setVelocity(yVelocity);

        if (wasDragging) {
            return true;
        } else {
            if (activeChatHead != currentChatHead) {
                switchTab(activeChatHead);
                return true;

            }
            deactivate();
            return false;
        }
    }

    public void selectTab(final ChatHead activeChatHead) {
        if (currentChatHead != activeChatHead) {
            manager.detachView(currentChatHead, getArrowLayout());
            currentChatHead = activeChatHead;
            getArrowLayout().removeAllViews();
            manager.attachView(activeChatHead, getArrowLayout());
        } else if (getArrowLayout() != null && getArrowLayout().getChildCount() <= 1) {
            manager.attachView(activeChatHead, getArrowLayout());
        }
        pointTo(activeChatHead);
        showOrHideView(activeChatHead);
    }

    private void pointTo(ChatHead activeChatHead) {
        UpArrowLayout arrowLayout = getArrowLayout();
        arrowLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deactivate();
            }
        });
        Point point = positions.get(activeChatHead);
        if (point != null) {
            int padding = manager.getConfig().getHeadVerticalSpacing();
            arrowLayout.pointTo(point.x + manager.getConfig().getHeadWidth() / 2, point.y + manager.getConfig().getHeadHeight() + padding);
            if (arrowLayout.getChildCount() > 1) {
                arrowLayout.getChildAt(1).setPivotX(point.x + manager.getConfig().getHeadWidth() / 2);
                arrowLayout.getChildAt(1).setPivotY(0f);
                if (needAnimation) {
                    arrowLayout.getChildAt(1).setScaleX(0f);
                    arrowLayout.getChildAt(1).setScaleY(0f);
                }
            }
        }
    }

    private void positionToOriginal(ChatHead activeChatHead, Spring activeHorizontalSpring, Spring activeVerticalSpring) {
        if (activeChatHead == currentChatHead){
            state = State.CANCEL_REMOVE;
        }
        if (activeChatHead.getState() == ChatHead.State.FREE) {
            Point point = positions.get(activeChatHead);
            if (point != null) {
                activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                activeHorizontalSpring.setVelocity(0);
                activeHorizontalSpring.setEndValue(point.x);
                activeVerticalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                activeVerticalSpring.setVelocity(0);
                activeVerticalSpring.setEndValue(point.y);
            }
        }

    }

    @Override
    public void onSpringUpdate(ChatHead activeChatHead, boolean isDragging, int maxWidth, int maxHeight, Spring spring, Spring activeHorizontalSpring, Spring activeVerticalSpring, int totalVelocity) {
        /** Bounds Check **/
        if (spring == activeHorizontalSpring && !isDragging) {
            double xPosition = activeHorizontalSpring.getCurrentValue();
            if (xPosition + manager.getConfig().getHeadWidth() > maxWidth && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.NOT_DRAGGING && !activeHorizontalSpring.isOvershooting()) {
                positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);
            }
            if (xPosition < 0 && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.NOT_DRAGGING && !activeHorizontalSpring.isOvershooting()) {
                positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);
            }
        } else if (spring == activeVerticalSpring && !isDragging) {
            double yPosition = activeVerticalSpring.getCurrentValue();

            if (yPosition + manager.getConfig().getHeadHeight() > maxHeight && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.NOT_DRAGGING && !activeHorizontalSpring.isOvershooting()) {
                positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);
            }
            if (yPosition < 0 && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.NOT_DRAGGING && !activeHorizontalSpring.isOvershooting()) {
                positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);
            }

        }

        /** position it back **/
        if (!isDragging && totalVelocity < MIN_VELOCITY_TO_POSITION_BACK && activeHorizontalSpring.getSpringConfig() == SpringConfigsHolder.DRAGGING) {
            positionToOriginal(activeChatHead, activeHorizontalSpring, activeVerticalSpring);
        }

        if (activeHorizontalSpring.isAtRest() && activeVerticalSpring.isAtRest()){
            state = State.FREE;
        }

        if (activeChatHead == currentChatHead && isDragging){
            state = State.REMOVE;
        }

        animation(currentChatHead);

        if (!isDragging) {
            /** Capturing check **/

            double distanceCloseButtonFromHead = manager.getDistanceCloseButtonFromHead((float) activeHorizontalSpring.getCurrentValue() + manager.getConfig().getHeadWidth() / 2, (float) activeVerticalSpring.getCurrentValue() + manager.getConfig().getHeadHeight() / 2);

            if (distanceCloseButtonFromHead < activeChatHead.CLOSE_ATTRACTION_THRESHOLD && activeHorizontalSpring.getSpringConfig() == SpringConfigsHolder.DRAGGING && activeVerticalSpring.getSpringConfig() == SpringConfigsHolder.DRAGGING) {

                activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                activeVerticalSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
                activeChatHead.setState(ChatHead.State.CAPTURED);
            }
            if (activeChatHead.getState() == ChatHead.State.CAPTURED && activeHorizontalSpring.getSpringConfig() != SpringConfigsHolder.CAPTURING) {
                activeHorizontalSpring.setAtRest();
                activeVerticalSpring.setAtRest();
                activeHorizontalSpring.setSpringConfig(SpringConfigsHolder.CAPTURING);
                activeVerticalSpring.setSpringConfig(SpringConfigsHolder.CAPTURING);

            }
            if (activeChatHead.getState() == ChatHead.State.CAPTURED && activeVerticalSpring.isAtRest()) {
                manager.removeChatHead(activeChatHead.getUser());
                manager.getCloseButton().onCapture();
                manager.getCloseButton().disappear();
            }

            if (!activeVerticalSpring.isAtRest()) {
                manager.getCloseButton().appear();
            } else {
                manager.getCloseButton().disappear();
            }
        }
    }

    private void showOrHideView(ChatHead activeChatHead) {
        Point point = positions.get(activeChatHead);
        if (point != null) {
            double dx = activeChatHead.getHorizontalSpring().getCurrentValue() - point.x;
            double dy = activeChatHead.getVerticalSpring().getCurrentValue() - point.y;
            double distanceFromOriginal = Math.hypot(dx, dy);
            if (distanceFromOriginal < maxDistanceFromOriginal) {
                showView(distanceFromOriginal);
            } else{
                hideView();
            }
        }

    }

    private void showView(double distanceFromOriginal) {
        UpArrowLayout arrowLayout = getArrowLayout();
        arrowLayout.setVisibility(View.VISIBLE);
        arrowLayout.setAlpha(1f - ((float) distanceFromOriginal / (float) maxDistanceFromOriginal));
        if (arrowLayout.getChildCount() > 1 && needAnimation) {
            arrowLayout.getChildAt(1).setScaleX(1f - ((float) distanceFromOriginal / (float) maxDistanceFromOriginal));
            arrowLayout.getChildAt(1).setScaleY(1f - ((float) distanceFromOriginal / (float) maxDistanceFromOriginal));
        }
    }

    @Override
    public void onChatHeadAdded(final ChatHead chatHead, boolean bringToFront) {
        //we post so that chat head measurement is done
        Spring spring = chatHead.getHorizontalSpring();
        spring.setCurrentValue(maxWidth).setAtRest();
        spring = chatHead.getVerticalSpring();
        spring.setCurrentValue(topPadding).setAtRest();
        onActivate(manager, getBundleWithHero(), maxWidth, maxHeight, bringToFront, chatHead);
    }

    @Override
    public void onChatHeadRemoved(ChatHead removed) {
        manager.detachView(removed, getArrowLayout());
        positions.remove(removed);
        boolean isEmpty = false;

        ChatHead nextBestChatHead = null;
        if (currentChatHead == removed) {
            if (manager.getChatHeads().size() > 0){
                nextBestChatHead = manager.getChatHeads().get(0);
                if (nextBestChatHead.getUser().block && manager.getChatHeads().size() > 1) {
                    nextBestChatHead = manager.getChatHeads().get(manager.getChatHeads().size() - 1);
                } else if (nextBestChatHead.getUser().block && manager.getChatHeads().size() == 1) {
                    nextBestChatHead = null;
                }
            }
            if (nextBestChatHead != null) {
                isEmpty = false;
            } else {
                isEmpty = true;
            }
        }
        if (!isEmpty) {
            onActivate(manager, getBundleWithHero(), maxWidth, maxHeight, currentChatHead == removed, nextBestChatHead);
        } else {
          manager.removeAllChatHeads();
        }

    }

    private void deactivate() {
        manager.setArrangement(MinimizedArrangement.class, getBundleWithHero());
    }

    @Override
    public Bundle getBundleWithHero() {
        Bundle bundle = extras;
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putInt(MinimizedArrangement.BUNDLE_HERO_INDEX_KEY, getHeroIndex());
        return bundle;
    }
    /**
     * @return the index of the selected chat head a.k.a the hero
     */
    @Override
    public Integer getHeroIndex() {
        int heroIndex = 0;
        List<ChatHead> chatHeads = manager.getChatHeads();
        int i = 0;
        for (ChatHead chatHead : chatHeads) {
            if (currentChatHead == chatHead) {
                heroIndex = i;
            }
            i++;
        }
        return heroIndex;
    }

    @Override
    public void onConfigChanged(ChatHeadConfig newConfig) {

    }

    @Override
    public boolean canDrag(ChatHead chatHead) {
        return true;
    }

    @Override
    public void removeOldestChatHead() {
        ChatHead chatHead = null;
        for (int i = 0; i < manager.getChatHeads().size(); i++) {
            chatHead = manager.getChatHeads().get(i);
            if (chatHead.getUser().block) {
                continue;
            }
            if (chatHead != currentChatHead) {
                break;
            }
        }
        manager.removeChatHead(chatHead.getUser());
    }

    private void hideView() {
        UpArrowLayout arrowLayout = getArrowLayout();
        arrowLayout.setVisibility(View.GONE);
    }

}