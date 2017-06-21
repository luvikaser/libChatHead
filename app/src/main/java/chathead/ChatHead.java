package chathead;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import chathead.ChatHeadArrangement.MaximizedArrangement;
import chathead.ChatHeadArrangement.MinimizedArrangement;
import chathead.ChatHeadManager.ChatHeadManager;
import chathead.ChatHeadUI.ChatHeadContainer.ChatHeadContainer;
import chathead.ChatHeadUI.PopupFragment.ChatHeadViewAdapter;
import nhutlm2.fresher.demochathead.R;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;


/**
 * Created by cpu1-216-local on 12/05/2017.
 */

public class ChatHead{
    private ChatHeadManager chatHeadManager = null;
    private ChatHeadContainer chatHeadContainer;
    private Context context;
    private Map<User, View> viewCache = new HashMap<>();

    public ChatHead(Context context){
        this.context = context;
    }

    public void start(){
        chatHeadContainer = new ChatHeadContainer(context);
        chatHeadManager = new ChatHeadManager(context, chatHeadContainer);
        chatHeadManager.setViewAdapter(new ChatHeadViewAdapter<User>() {

            @Override
            public View attachView(User user, final chathead.ChatHeadUI.ChatHead chatHead, ViewGroup parent) {
                View cachedView = viewCache.get(user);
                if (cachedView == null) {
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                    View view = inflater.inflate(R.layout.fragment_test, parent, false);
                    TextView identifier = (TextView) view.findViewById(R.id.identifier);
                    identifier.setText(String.valueOf(user.id));

                    ((Button)view.findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            Bitmap bitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.pic3, options);
                            User user2 = new User(9, 10, bitmap2, false, "");
                            push(user2, true);
                        }
                    });
                    cachedView = view;
                    viewCache.put(user, view);
                }
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
            }

        });

        chatHeadManager.setArrangement(MinimizedArrangement.class, null);
    }

    public void push(User user, boolean bringToFront) {
        if (!isClose()) {
            chatHeadManager.addChatHead(user, bringToFront);

        }
    }
    public void setVisibility(int visibility) {
        chatHeadManager.setVisibility(visibility);
    }

    public void close(){
        chatHeadManager.removeAllChatHeads();
    }

    public boolean isClose(){
        return chatHeadContainer.isDestroyed();
    }


    public void minimize(){
        if (!isClose()) {
            chatHeadManager.setArrangement(MinimizedArrangement.class, null);
        }
    }

    public void maximize(){
        if (!isClose()) {
            chatHeadManager.setArrangement(MaximizedArrangement.class, null);
        }
    }
}
