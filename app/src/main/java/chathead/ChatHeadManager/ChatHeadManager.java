package chathead.ChatHeadManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.rebound.SpringConfigRegistry;
import com.facebook.rebound.SpringSystem;

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
import chathead.ChatHeadUI.ChatHeadDrawable.AvatarDrawer;
import chathead.ChatHeadUI.ChatHeadDrawable.ChatHeadDrawable;
import chathead.ChatHeadUI.ChatHeadDrawable.NotificationDrawer;
import chathead.ChatHeadUI.PopupFragment.ChatHeadViewAdapter;
import chathead.ChatHeadUI.PopupFragment.UpArrowLayout;
import chathead.User;
import chathead.Utils.ChatHeadConfig;
import chathead.Utils.ChatHeadDefaultConfig;
import chathead.Utils.ChatHeadUtils;
import chathead.Utils.SpringConfigsHolder;
import nhutlm2.fresher.demochathead.R;

import static android.view.View.GONE;

/**
 * Created by luvikaser on 07/03/2017.
 */

public class ChatHeadManager implements ChatHeadManagerListener {
    public static final String PREFERENCE_FILE_KEY = "preference_chat_head";
    public static final String IDLE_STATE_X = "idle_state_x";
    public static final String IDLE_STATE_Y = "idle_state_y";
    private final Map<Class<? extends ChatHeadArrangement>, ChatHeadArrangement> arrangements = new HashMap<>(3);
    private final Context context;
    private final ChatHeadContainer chatHeadContainer;
    private List<ChatHead> chatHeads;
    private int maxWidth;
    private int maxHeight;
    private ChatHeadCloseButton closeButton;
    private ChatHeadArrangement activeArrangement;
    private SpringSystem springSystem;
    private ChatHeadConfig config;
    public ArrangementChangeRequest requestedArrangement;
    private DisplayMetrics displayMetrics;
    private UpArrowLayout arrowLayout;
    private ChatHeadViewAdapter viewAdapter;
    public TextView bubbleText = null;
    public ChatHeadManager(Context context, ChatHeadContainer chatHeadContainer) {
        this.context = context;
        this.chatHeadContainer = chatHeadContainer;
        this.displayMetrics = chatHeadContainer.getDisplayMetrics();
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
        if (height != maxHeight && width != maxWidth && maxHeight != 0 && maxWidth != 0) {
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
    public ChatHead addChatHead(User user, boolean bringToFront) {

        ChatHead chatHead = findChatHeadByKey(user);
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
            if (activeArrangement != null) {
                int padding = (activeArrangement instanceof MinimizedArrangement) ? ChatHeadUtils.dpToPx(getContext(), 6) : 0;
                chatHead.setPadding(padding);
                activeArrangement.onChatHeadAdded(chatHead, bringToFront);
            }
            else {
                SharedPreferences sharedPref = getContext().getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
                int initX = (sharedPref.getInt(IDLE_STATE_X, 0) <= 0) ? (sharedPref.getInt(IDLE_STATE_X, 0) - 100) : (sharedPref.getInt(IDLE_STATE_X, 0) + 100);
                int initY = sharedPref.getInt(IDLE_STATE_Y, 0) - 200;
                chatHead.getHorizontalSpring().setCurrentValue(initX);
                chatHead.getVerticalSpring().setCurrentValue(initY);
            }
        } else{
            chatHead.setUser(user);
            if (activeArrangement != null && activeArrangement instanceof MaximizedArrangement && bringToFront) {
                ((MaximizedArrangement) activeArrangement).switchTab(chatHead);
            }
            if (activeArrangement != null && activeArrangement instanceof MinimizedArrangement) {
                activeArrangement.onChatHeadAdded(chatHead, true);
            }

        }
        reloadDrawable(user);
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

    private ChatHeadDrawable getChatHeadDrawable(User user) {
        ChatHeadDrawable chatHeadDrawable = new ChatHeadDrawable();
        chatHeadDrawable.setAvatarDrawer(new AvatarDrawer(user.avatar, new BitmapShader(user.avatar, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)));
        if (user.countMessage != 0)
            chatHeadDrawable.setNotificationDrawer(new NotificationDrawer().setNotificationText(String.valueOf(user.countMessage)).setNotificationAngle(135).setNotificationColor(Color.WHITE, Color.RED));
        return chatHeadDrawable;

    }
    @Override
    public void reloadDrawable(User user) {
        if (findChatHeadByKey(user) != null) {
            findChatHeadByKey(user).setChatHeadDrawable(getChatHeadDrawable(user));
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
            removeView(chatHead, getArrowLayout());
            if (activeArrangement != null)
                activeArrangement.onChatHeadRemoved(chatHead);
        }
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

        closeButton = new ChatHeadCloseButton(context, this);
        ViewGroup.LayoutParams layoutParams = chatHeadContainer.createLayoutParams(chatHeadDefaultConfig.getCloseButtonHeight(), chatHeadDefaultConfig.getCloseButtonWidth(), Gravity.TOP | Gravity.START, 0);
        chatHeadContainer.addView(closeButton, layoutParams);
        arrangements.put(MinimizedArrangement.class, new MinimizedArrangement(this));
        arrangements.put(MaximizedArrangement.class, new MaximizedArrangement(this));
        setConfig(chatHeadDefaultConfig);

        springSystem = SpringSystem.create();
        SpringConfigRegistry.getInstance().addSpringConfig(SpringConfigsHolder.DRAGGING, "dragging mode");
        SpringConfigRegistry.getInstance().addSpringConfig(SpringConfigsHolder.NOT_DRAGGING, "not dragging mode");

    }


    public double getDistanceCloseButtonFromHead(float touchX, float touchY) {
        double xDiff = touchX - closeButton.centerX;
        double yDiff = touchY - closeButton.centerY;
        double distance = Math.hypot(xDiff, yDiff);
        return distance;

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
        int padding = (requestedArrangement instanceof MinimizedArrangement) ? ChatHeadUtils.dpToPx(getContext(), 6) : 0;
        for(ChatHead chatHead: chatHeads){
            chatHead.setPadding(padding);
        }
        requestedArrangement.onActivate(this, extras, maxWidth, maxHeight, true, null);
        if (hasChanged) {
            chatHeadContainer.onArrangementChanged(oldArrangement, newArrangement);
        }

    }

    @Override
    public UpArrowLayout getArrowLayout() {
        return arrowLayout;
    }

    @Override
    public void setViewAdapter(ChatHeadViewAdapter chatHeadViewAdapter) {
        this.viewAdapter = chatHeadViewAdapter;
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
    public void setVisibility(int visibility) {
        chatHeadContainer.setVisibility(visibility);
        bubbleText.setVisibility(visibility);
    }

    @Override
    public void detachView(ChatHead chatHead, ViewGroup parent) {
        viewAdapter.detachView(chatHead.getUser(), chatHead, parent);
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
    public ChatHeadConfig getConfig() {
        return config;
    }

    @Override
    public void setConfig(ChatHeadConfig config) {
        this.config = config;
        if (closeButton != null) {
            if (config.isCloseButtonHidden()) {
                closeButton.setVisibility(GONE);
            } else {
                closeButton.setVisibility(View.VISIBLE);
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

    public class ArrangementChangeRequest {
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

    private Handler hideBubblehandler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            hideBubbleText();
        }
    };

    public void hideBubbleText(){
        if (bubbleText != null){
            bubbleText.setVisibility(View.GONE);
            hideBubblehandler.removeCallbacks(runnable);
        }
    }
    public void showBubbleText(String mess, WindowManager.LayoutParams params){
        hideBubbleText();
        if (bubbleText == null) {
            bubbleText = new TextView(context);
            chatHeadContainer.getWindowManager().addView(bubbleText, params);
        } else{
            bubbleText.setVisibility(View.VISIBLE);
            chatHeadContainer.getWindowManager().updateViewLayout(bubbleText, params);
        }

        bubbleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideBubbleText();
                setArrangement(MaximizedArrangement.class, getActiveArrangement().getBundleWithHero());
            }
        });
        bubbleText.setText(mess);
        bubbleText.setBackgroundColor(Color.BLUE);
        bubbleText.setTextColor(Color.WHITE);
        hideBubblehandler.postDelayed(runnable, 2000);
    }

}
