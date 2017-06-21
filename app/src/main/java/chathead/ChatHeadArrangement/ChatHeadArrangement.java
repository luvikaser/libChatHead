package chathead.ChatHeadArrangement;

import android.os.Bundle;

import com.facebook.rebound.Spring;

import chathead.ChatHeadManager.ChatHeadManager;
import chathead.ChatHeadUI.ChatHead;
import chathead.Utils.ChatHeadConfig;

/**
 * Created by luvikaser on 07/03/2017.
 */

public abstract class ChatHeadArrangement {
    public abstract void onActivate(ChatHeadManager container, Bundle extras, int maxWidth, int maxHeight, boolean bringToFront, ChatHead activeChatHead);

    public abstract void onDeactivate(int maxWidth, int maxHeight);

    public abstract void onSpringUpdate(ChatHead activeChatHead, boolean isDragging, int maxWidth, int maxHeight, Spring spring, Spring activeHorizontalSpring, Spring activeVerticalSpring, int totalVelocity);

    public abstract boolean handleTouchUp(ChatHead activeChatHead, int xVelocity, int yVelocity, Spring activeHorizontalSpring, Spring activeVerticalSpring, boolean wasDragging);

    public abstract void onChatHeadAdded(ChatHead chatHead, boolean bringToFront);

    public abstract void onChatHeadRemoved(ChatHead removed);


    public abstract Integer getHeroIndex();

    public abstract void onConfigChanged(ChatHeadConfig newConfig);

    public abstract boolean canDrag(ChatHead chatHead);

    public abstract void removeOldestChatHead();

    public abstract Bundle getBundleWithHero();

}
