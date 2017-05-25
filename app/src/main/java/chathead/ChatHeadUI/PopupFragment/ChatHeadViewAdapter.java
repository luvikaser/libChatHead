package chathead.ChatHeadUI.PopupFragment;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;


import chathead.ChatHeadUI.ChatHead;

/**
 * Created by luvikaser on 01/03/2017.
 */

public interface ChatHeadViewAdapter<User> {

    View attachView(User user, ChatHead chatHead, ViewGroup parent);

    void detachView(User user, ChatHead chatHead, ViewGroup parent);

    void removeView(User user, ChatHead chatHead, ViewGroup parent);

}
