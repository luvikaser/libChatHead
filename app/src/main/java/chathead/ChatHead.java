package chathead;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import chathead.ChatHeadArrangement.MinimizedArrangement;
import chathead.ChatHeadManager.ChatHeadManager;
import chathead.ChatHeadUI.ChatHeadContainer.ChatHeadContainer;
import chathead.ChatHeadUI.ChatHeadDrawable.AvatarDrawer;
import chathead.ChatHeadUI.ChatHeadDrawable.ChatHeadDrawable;
import chathead.ChatHeadUI.ChatHeadDrawable.NotificationDrawer;
import chathead.ChatHeadUI.PopupFragment.ChatHeadViewAdapter;
import nhutlm2.fresher.demochathead.R;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;


/**
 * Created by cpu1-216-local on 12/05/2017.
 */

public class ChatHead implements ChatHeadManager.ClickChatHeadListener{
    private ChatHeadManager chatHeadManager = null;
    private ChatHeadContainer chatHeadContainer;
    private Map<User, View> viewCache = new HashMap<>();
    private Context context;
    public interface ClickChatHeadListener{
        void onClick(User user);
    }
    private ClickChatHeadListener listener;

    public ChatHead(Context context, ClickChatHeadListener listener){
        this.context = context;
        this.listener = listener;
    }

    public void start(){
        chatHeadContainer = new ChatHeadContainer(context);
        chatHeadManager = new ChatHeadManager(context, chatHeadContainer, this);
        chatHeadManager.setViewAdapter(new ChatHeadViewAdapter<User>() {

            @Override
            public View attachView(User user, chathead.ChatHeadUI.ChatHead chatHead, ViewGroup parent) {
                View cachedView = viewCache.get(user);
                if (cachedView == null) {
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                    View view = inflater.inflate(R.layout.fragment_test, parent, false);
                    TextView identifier = (TextView) view.findViewById(R.id.identifier);
                    identifier.setText(String.valueOf(user.id));
                    cachedView = view;
                    viewCache.put(user, view);
                }
                cachedView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                parent.addView(cachedView);
                return cachedView;
            }

            @Override
            public void detachView(User user, chathead.ChatHeadUI.ChatHead chatHead, ViewGroup parent) {
                View cachedView = viewCache.get(user);
                if (cachedView != null) {
                    parent.removeView(cachedView);
                }
            }

            @Override
            public void removeView(User user, chathead.ChatHeadUI.ChatHead chatHead, ViewGroup parent) {
                View cachedView = viewCache.get(user);
                if (cachedView != null) {
                    viewCache.remove(user);
                    parent.removeView(cachedView);
                }
                if (chatHeadManager.getChatHeads().size() == 0) {
                    chatHeadContainer.destroy();
                }
            }

            @Override
            public Drawable getChatHeadDrawable(User user) {
                return ChatHead.this.getChatHeadDrawable(user);
            }
        });

        chatHeadManager.setArrangement(MinimizedArrangement.class, null);
    }

    public void push(User user) {
        chatHeadManager.removeAllChatHeads();
        chatHeadManager.addChatHead(user);
       // chatHeadManager.bringToFront(chatHeadManager.findChatHeadByKey(user));
    }
    public void setVisibility(int visibility) {
        chatHeadContainer.setVisibility(visibility);
    }

    public void close(){
        chatHeadManager.removeAllChatHeads();
    }
    public Drawable getChatHeadDrawable(User user) {
        ChatHeadDrawable chatHeadDrawable = new ChatHeadDrawable();

        chatHeadDrawable.setAvatarDrawer(new AvatarDrawer(user.avatar, new BitmapShader(user.avatar, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)));
        chatHeadDrawable.setNotificationDrawer(new NotificationDrawer().setNotificationText(String.valueOf(user.countMessage)).setNotificationAngle(135).setNotificationColor(Color.WHITE, Color.RED));
        return chatHeadDrawable;
    }

    @Override
    public void onClick(User user) {
        listener.onClick(user);
    }
}
