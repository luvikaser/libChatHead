package chathead.ChatHeadManager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.facebook.rebound.SpringConfigRegistry;
import com.facebook.rebound.SpringSystem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import chathead.ChatHeadArrangement.ChatHeadArrangement;
import chathead.ChatHeadArrangement.MaximizedArrangement;
import chathead.ChatHeadArrangement.MinimizedArrangement;
import chathead.ChatHeadUI.ChatHead;
import chathead.ChatHeadUI.ChatHeadCloseButton;
import chathead.ChatHeadUI.ChatHeadContainer.ChatHeadContainer;
import chathead.ChatHeadUI.PopupFragment.ChatHeadViewAdapter;
import chathead.ChatHeadUI.PopupFragment.UpArrowLayout;
import chathead.User;
import chathead.Utils.ChatHeadConfig;
import chathead.Utils.ChatHeadDefaultConfig;
import chathead.Utils.ChatHeadOverlayView;
import chathead.Utils.SpringConfigsHolder;
import nhutlm2.fresher.demochathead.R;

/**
 * Created by luvikaser on 07/03/2017.
 */

public class ChatHeadManager implements ChatHeadManagerListener {
    private static final int OVERLAY_TRANSITION_DURATION = 200;
    private final Map<Class<? extends ChatHeadArrangement>, ChatHeadArrangement> arrangements = new HashMap<>(3);
    private final Context context;
    private final ChatHeadContainer chatHeadContainer;
    private List<ChatHead> chatHeads;
    private int maxWidth;
    private int maxHeight;
    private ChatHeadCloseButton closeButton;
    private ChatHeadArrangement activeArrangement;
    private ChatHeadViewAdapter viewAdapter;
    private ChatHeadOverlayView overlayView;
    private boolean overlayVisible;
    private ImageView closeButtonShadow;
    private SpringSystem springSystem;
    private ChatHeadConfig config;
    private ArrangementChangeRequest requestedArrangement;
    private DisplayMetrics displayMetrics;
    private UpArrowLayout arrowLayout;
    public ClickChatHeadListener listener;
    public ChatHeadManager(Context context, ChatHeadContainer chatHeadContainer, ClickChatHeadListener listener) {
        this.context = context;
        this.chatHeadContainer = chatHeadContainer;
        this.displayMetrics = chatHeadContainer.getDisplayMetrics();
        this.listener = listener;
        init(context, new ChatHeadDefaultConfig(context));
    }

    public ChatHeadContainer getChatHeadContainer() {
        return chatHeadContainer;
    }

    @Override
    public DisplayMetrics getDisplayMetrics() {
        return displayMetrics;
    }

    @Override
    public List<ChatHead> getChatHeads() {
        return chatHeads;
    }

    @Override
    public void setViewAdapter(ChatHeadViewAdapter chatHeadViewAdapter) {
        this.viewAdapter = chatHeadViewAdapter;
    }

    @Override
    public ChatHeadCloseButton getCloseButton() {
        return closeButton;
    }

    @Override
    public int getMaxWidth() {
        return maxWidth;
    }

    @Override
    public int getMaxHeight() {
        return maxHeight;
    }

    @Override
    public Context getContext() {
        return context;
    }


    @Override
    public ChatHeadArrangement getActiveArrangement() {
        if (activeArrangement != null) {
            return activeArrangement;
        }
        return null;
    }

    @Override
    public void onMeasure(int height, int width) {
        boolean needsLayout = false;
        if (height != maxHeight && width != maxWidth) {
            needsLayout = true; // both changed, must be screen rotation.
        }
        maxHeight = height;
        maxWidth = width;

        int closeButtonCenterX = (int) ((float) width * 0.5f);
        int closeButtonCenterY = (int) ((float) height * 0.9f);

        closeButton.onParentHeightRefreshed();
        closeButton.setCenter(closeButtonCenterX, closeButtonCenterY);

        if (maxHeight > 0 && maxWidth > 0) {
            if (requestedArrangement != null) {
                setArrangementImpl(requestedArrangement);
                requestedArrangement = null;
            } else {
                if (needsLayout) {
                    // this means height changed and we need to redraw.
                    setArrangementImpl(new ArrangementChangeRequest(activeArrangement.getClass(), null));

                }
            }
        }

    }



    @Override
    public ChatHead addChatHead(User user) {
        android.util.Log.d("abc","add");
        ChatHead chatHead = null;
        if (chatHead == null) {
            chatHead = new ChatHead(this, springSystem, getContext());
            chatHead.setUser(user);
            chatHeads.add(chatHead);
            ViewGroup.LayoutParams layoutParams = chatHeadContainer.createLayoutParams(getConfig().getHeadWidth(), getConfig().getHeadHeight(), Gravity.START | Gravity.TOP, 0);

            chatHeadContainer.addView(chatHead, layoutParams);
            if (chatHeads.size() > config.getMaxChatHeads()) {
                if (activeArrangement != null) {
                    activeArrangement.removeOldestChatHead();
                } else{
                    removeChatHead(chatHeads.get(0).getUser());
                }
            }
            reloadDrawable(user);
            if (activeArrangement != null)
                activeArrangement.onChatHeadAdded(chatHead);
            else {
                chatHead.getHorizontalSpring().setCurrentValue(-100);
                chatHead.getVerticalSpring().setCurrentValue(-100);
            }
            closeButtonShadow.bringToFront();
        }
        return chatHead;
    }

    @Override
    public ChatHead findChatHeadByKey(User user) {
        for (ChatHead chatHead : chatHeads) {
            if (chatHead.getUser().equals(user))
                return chatHead;
        }

        return null;
    }

    @Override
    public void reloadDrawable(User user) {
        Drawable chatHeadDrawable = viewAdapter.getChatHeadDrawable(user);
        if (chatHeadDrawable != null) {
            findChatHeadByKey(user).setImageDrawable(viewAdapter.getChatHeadDrawable(user));
         //   findChatHeadByKey(key).addShadow();
        }
    }

    @Override
    public void removeAllChatHeads() {
        for (Iterator<ChatHead> iterator = chatHeads.iterator(); iterator.hasNext(); ) {
            ChatHead chatHead = iterator.next();
            iterator.remove();
            onChatHeadRemoved(chatHead);
        }
    }

    @Override
    public boolean removeChatHead(User user) {

        ChatHead chatHead = findChatHeadByKey(user);
        if (chatHead != null) {
            chatHeads.remove(chatHead);
            onChatHeadRemoved(chatHead);
            return true;
        }
        return false;
    }

    private void onChatHeadRemoved(ChatHead chatHead) {
        if (chatHead != null && chatHead.getParent() != null) {
            chatHead.onRemove();
            chatHeadContainer.removeView(chatHead);
            if (activeArrangement != null)
                activeArrangement.onChatHeadRemoved(chatHead);
        }
    }

    @Override
    public ChatHeadOverlayView getOverlayView() {
        return overlayView;
    }

    private void init(Context context, ChatHeadConfig chatHeadDefaultConfig) {
        chatHeadContainer.onInitialized(this);
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        this.displayMetrics = metrics;
        this.config = chatHeadDefaultConfig; //TODO : needs cleanup
        chatHeads = new ArrayList<>(5);
        arrowLayout = new UpArrowLayout(context);
        arrowLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        chatHeadContainer.addView(arrowLayout, arrowLayout.getLayoutParams());
        arrowLayout.setVisibility(View.GONE);
        springSystem = SpringSystem.create();
        closeButton = new ChatHeadCloseButton(context, this);
        ViewGroup.LayoutParams layoutParams = chatHeadContainer.createLayoutParams(chatHeadDefaultConfig.getCloseButtonHeight(), chatHeadDefaultConfig.getCloseButtonWidth(), Gravity.TOP | Gravity.START, 0);
        chatHeadContainer.addView(closeButton, layoutParams);
        closeButtonShadow = new ImageView(getContext());
        ViewGroup.LayoutParams shadowLayoutParams = chatHeadContainer.createLayoutParams(metrics.heightPixels / 8, metrics.widthPixels, Gravity.BOTTOM, 0);
        closeButtonShadow.setImageResource(R.drawable.dismiss_shadow);
        closeButtonShadow.setVisibility(View.GONE);
        chatHeadContainer.addView(closeButtonShadow, shadowLayoutParams);

        arrangements.put(MinimizedArrangement.class, new MinimizedArrangement(this));
        arrangements.put(MaximizedArrangement.class, new MaximizedArrangement(this));
        setupOverlay(context);
        setConfig(chatHeadDefaultConfig);
        SpringConfigRegistry.getInstance().addSpringConfig(SpringConfigsHolder.DRAGGING, "dragging mode");
        SpringConfigRegistry.getInstance().addSpringConfig(SpringConfigsHolder.NOT_DRAGGING, "not dragging mode");
    }

    private void setupOverlay(Context context) {
        overlayView = new ChatHeadOverlayView(context);
        overlayView.setBackgroundResource(R.drawable.overlay_transition);
        ViewGroup.LayoutParams layoutParams = getChatHeadContainer().createLayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, Gravity.NO_GRAVITY, 0);
        getChatHeadContainer().addView(overlayView, layoutParams);
    }

    public double getDistanceCloseButtonFromHead(float touchX, float touchY) {
        if (closeButton.isDisappeared()) {
            return Double.MAX_VALUE;
        } else {
            int left = closeButton.getLeft();
            int top = closeButton.getTop();
            double xDiff = touchX - left - getChatHeadContainer().getViewX(closeButton) - closeButton.getMeasuredWidth() / 2;
            double yDiff = touchY - top - getChatHeadContainer().getViewY(closeButton) - closeButton.getMeasuredHeight() / 2;
            double distance = Math.hypot(xDiff, yDiff);
            return distance;
        }
    }

    @Override
    public UpArrowLayout getArrowLayout() {
        return arrowLayout;
    }

    @Override
    public void setArrangement(Class<? extends ChatHeadArrangement> arrangement, Bundle extras) {
        this.requestedArrangement = new ArrangementChangeRequest(arrangement, extras);
        chatHeadContainer.requestLayout();
    }

    /**
     * Should only be called after onMeasure
     *
     * @param requestedArrangementParam
     */
    private void setArrangementImpl(ArrangementChangeRequest requestedArrangementParam) {
        boolean hasChanged = false;
        ChatHeadArrangement requestedArrangement = arrangements.get(requestedArrangementParam.getArrangement());
        ChatHeadArrangement oldArrangement = null;
        ChatHeadArrangement newArrangement = requestedArrangement;
        Bundle extras = requestedArrangementParam.getExtras();
        if (activeArrangement != requestedArrangement) hasChanged = true;
        if (extras == null) extras = new Bundle();

        if (activeArrangement != null) {
            extras.putAll(activeArrangement.getBundleWithHero());
            activeArrangement.onDeactivate(maxWidth, maxHeight);
            oldArrangement = activeArrangement;
        }
        activeArrangement = requestedArrangement;
        requestedArrangement.onActivate(this, extras, maxWidth, maxHeight);
        if (hasChanged) {
            chatHeadContainer.onArrangementChanged(oldArrangement, newArrangement);
        }

    }

    @Override
    public void hideOverlayView(boolean animated) {
        if (overlayVisible) {
            TransitionDrawable drawable = (TransitionDrawable) overlayView.getBackground();
            int duration = OVERLAY_TRANSITION_DURATION;
            if (!animated) duration = 0;
            drawable.reverseTransition(duration);
            overlayView.setClickable(false);
            overlayVisible = false;
        }
    }

    @Override
    public void showOverlayView(boolean animated) {
        if (!overlayVisible) {
            TransitionDrawable drawable = (TransitionDrawable) overlayView.getBackground();
            int duration = OVERLAY_TRANSITION_DURATION;
            if (!animated) duration = 0;
            drawable.startTransition(duration);
            overlayView.setClickable(true);
            overlayVisible = true;
        }
    }

    @Override
    public int[] getChatHeadCoordsForCloseButton(ChatHead chatHead) {
        int[] coords = new int[2];
        int x = (int) (closeButton.getLeft() + closeButton.getEndValueX() + closeButton.getMeasuredWidth() / 2 - chatHead.getMeasuredWidth() / 2);
        int y = (int) (closeButton.getTop() + closeButton.getEndValueY() + closeButton.getMeasuredHeight() / 2 - chatHead.getMeasuredHeight() / 2);
        coords[0] = x;
        coords[1] = y;
        return coords;
    }

    @Override
    public void bringToFront(ChatHead chatHead) {
        if (activeArrangement != null) {
            activeArrangement.bringToFront(chatHead);
        }
    }

    public void onCloseButtonAppear() {
        if (!getConfig().isCloseButtonHidden()) {
            closeButtonShadow.setVisibility(View.VISIBLE);
        }
    }

    public void onCloseButtonDisappear() {
        closeButtonShadow.setVisibility(View.GONE);
    }

    @Override
    public View attachView(ChatHead activeChatHead, ViewGroup parent) {
        View view = viewAdapter.attachView(activeChatHead.getUser(), activeChatHead, parent);
        return view;
    }

    @Override
    public void removeView(ChatHead chatHead, ViewGroup parent) {
        viewAdapter.removeView(chatHead.getUser(), chatHead, parent);
    }

    @Override
    public void detachView(ChatHead chatHead, ViewGroup parent) {
        viewAdapter.detachView(chatHead.getUser(), chatHead, parent);
    }

    @Override
    public ChatHeadConfig getConfig() {
        return config;
    }

    @Override
    public void setConfig(ChatHeadConfig config) {
        this.config = config;
        if (closeButton != null) {
            if (config.isCloseButtonHidden()) {
                closeButton.setVisibility(View.GONE);
                closeButtonShadow.setVisibility(View.GONE);
            } else {
                closeButton.setVisibility(View.VISIBLE);
                closeButtonShadow.setVisibility(View.VISIBLE);
            }
        }
        for (Map.Entry<Class<? extends ChatHeadArrangement>, ChatHeadArrangement> arrangementEntry : arrangements.entrySet()) {
            arrangementEntry.getValue().onConfigChanged(config);
        }

    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (closeButton != null) {
            closeButton.onParentHeightRefreshed();
        }
    }

    private class ArrangementChangeRequest {
        private final Class<? extends ChatHeadArrangement> arrangement;
        private final Bundle extras;

        public ArrangementChangeRequest(Class<? extends ChatHeadArrangement> arrangement, Bundle extras) {
            this.arrangement = arrangement;
            this.extras = extras;

        }

        public Bundle getExtras() {
            return extras;
        }

        public Class<? extends ChatHeadArrangement> getArrangement() {
            return arrangement;
        }

    }

    public interface ClickChatHeadListener{
        void onClick(User user);
    }
}
