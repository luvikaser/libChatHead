package chathead.ChatHeadArrangement;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;

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


    public MaximizedArrangement(ChatHeadManager manager) {
        this.manager = manager;
    }


    @Override
    public void onActivate(ChatHeadManager container, Bundle extras, int maxWidth, int maxHeight) {
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
            heroIndex = 0;
        }
        if (chatHeads.size() > 0 && heroIndex < chatHeads.size()) {
            currentChatHead = chatHeads.get(heroIndex);
            maxDistanceFromOriginal = (int) MAX_DISTANCE_FROM_ORIGINAL;

            int spacing = container.getConfig().getHeadHorizontalSpacing();
            int widthPerHead = container.getConfig().getHeadWidth();
            topPadding = ChatHeadUtils.dpToPx(container.getContext(), 5);
            int leftIndent = maxWidth - (chatHeads.size() * (widthPerHead + spacing));
            for (int i = 0; i < chatHeads.size(); i++) {
                ChatHead chatHead = chatHeads.get(i);
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
            selectChatHead(currentChatHead);
        }
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
            // this is a hack. If both velocities are 0, onSprintUpdate is not called and the chat head remains whereever it is
            // so we give a a negligible velocity to artificially fire onSpringUpdate
            xVelocity = 1;
            yVelocity = 1;
        }

        activeHorizontalSpring.setVelocity(xVelocity);
        activeVerticalSpring.setVelocity(yVelocity);

        if (wasDragging) {
            return true;
        } else {
            if (activeChatHead != currentChatHead) {
                selectTab(activeChatHead);
                return true;

            }
            deactivate();
            return false;
        }
    }

    private void selectTab(final ChatHead activeChatHead) {
        if (currentChatHead != activeChatHead) {
            manager.detachView(currentChatHead, getArrowLayout());
            currentChatHead = activeChatHead;
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
        getArrowLayout().removeAllViews();
        manager.attachView(activeChatHead, arrowLayout);
        Point point = positions.get(activeChatHead);
        if (point != null) {
            int padding = manager.getConfig().getHeadVerticalSpacing();
            arrowLayout.pointTo(point.x + manager.getConfig().getHeadWidth() / 2, point.y + manager.getConfig().getHeadHeight() + padding);
        }
    }

    private void positionToOriginal(ChatHead activeChatHead, Spring activeHorizontalSpring, Spring activeVerticalSpring) {
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

        if (activeChatHead == currentChatHead)
            showOrHideView(activeChatHead);

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
                showView(dx, dy, distanceFromOriginal);
            } else {
                hideView();
            }
        }

    }

    private void showView(double dx, double dy, double distanceFromOriginal) {
        UpArrowLayout arrowLayout = getArrowLayout();
        arrowLayout.setVisibility(View.VISIBLE);
        arrowLayout.setTranslationX((float) dx);
        arrowLayout.setTranslationY((float) dy);
        arrowLayout.setAlpha(1f - ((float) distanceFromOriginal / (float) maxDistanceFromOriginal));
    }

    @Override
    public void onChatHeadAdded(final ChatHead chatHead) {
        //we post so that chat head measurement is done

        Spring spring = chatHead.getHorizontalSpring();
        spring.setCurrentValue(maxWidth).setAtRest();
        spring = chatHead.getVerticalSpring();
        spring.setCurrentValue(topPadding).setAtRest();
        onActivate(manager, getBundleWithHero(), maxWidth, maxHeight);

    }

    @Override
    public void onChatHeadRemoved(ChatHead removed) {
        manager.detachView(removed, getArrowLayout());
        positions.remove(removed);
        boolean isEmpty = false;

        if (currentChatHead == removed) {
            ChatHead nextBestChatHead = null;
            if (manager.getChatHeads().size() != 0){
                nextBestChatHead = manager.getChatHeads().get(0);
            }
            if (nextBestChatHead != null) {
                isEmpty = false;
                selectTab(nextBestChatHead);
            } else {
                isEmpty = true;
            }
        }
        if (!isEmpty) {
            onActivate(manager, getBundleWithHero(), maxWidth, maxHeight);
        } else {
            deactivate();
        }

    }

    @Override
    public void selectChatHead(final ChatHead chatHead) {
        selectTab(chatHead);
    }

    private void deactivate() {
        manager.setArrangement(MinimizedArrangement.class, getBundleWithHero());
    }

    @Override
    public Bundle getBundleWithHero() {
        Bundle bundle = new Bundle();
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
        for (ChatHead chatHead : manager.getChatHeads()) {
            //we dont remove sticky chat heads as well as the currently selected chat head
            if (chatHead != currentChatHead) {
                manager.removeChatHead(chatHead.getUser());
                break;
            }
        }
    }


    @Override
    public void bringToFront(final ChatHead chatHead) {
        //nothing to do, everything is in front.
        selectChatHead(chatHead);
    }

    private void hideView() {
        UpArrowLayout arrowLayout = getArrowLayout();
        arrowLayout.setVisibility(View.GONE);
    }

}