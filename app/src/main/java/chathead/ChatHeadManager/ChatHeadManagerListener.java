package chathead.ChatHeadManager;

/**
 * Created by luvikaser on 07/03/2017.
 */

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import chathead.ChatHeadArrangement.ChatHeadArrangement;
import chathead.ChatHeadUI.ChatHead;
import chathead.ChatHeadUI.ChatHeadCloseButton;
import chathead.ChatHeadUI.ChatHeadContainer.ChatHeadContainer;
import chathead.ChatHeadUI.PopupFragment.ChatHeadViewAdapter;
import chathead.ChatHeadUI.PopupFragment.UpArrowLayout;
import chathead.User;
import chathead.Utils.ChatHeadConfig;
import chathead.Utils.ChatHeadOverlayView;

public interface ChatHeadManagerListener {
        List<ChatHead> getChatHeads();

        void setViewAdapter(ChatHeadViewAdapter chatHeadViewAdapter);

        ChatHeadCloseButton getCloseButton();

        ChatHeadArrangement getActiveArrangement();

        void onMeasure(int height, int width);

        ChatHead addChatHead(User user);

        ChatHead findChatHeadByKey(User user);

        void reloadDrawable(User user);

        void removeAllChatHeads();

        boolean removeChatHead(User user);

        ChatHeadOverlayView getOverlayView();

        void setArrangement(Class<? extends ChatHeadArrangement> arrangement, Bundle extras);

        View attachView(ChatHead activeChatHead, ViewGroup parent);

        void detachView(ChatHead chatHead, ViewGroup parent);

        void removeView(ChatHead chatHead, ViewGroup parent);

        ChatHeadConfig getConfig();

        void setConfig(ChatHeadConfig config);

        double getDistanceCloseButtonFromHead(float rawX, float rawY);

        void hideOverlayView(boolean animated);

        void showOverlayView(boolean animated);

        int[] getChatHeadCoordsForCloseButton(ChatHead chatHead);

        void bringToFront(ChatHead chatHead);

        UpArrowLayout getArrowLayout();

        ChatHeadContainer getChatHeadContainer();

        DisplayMetrics getDisplayMetrics();

        int getMaxWidth();

        int getMaxHeight();

        Context getContext();

        void onSizeChanged(int w, int h, int oldw, int oldh);


        }