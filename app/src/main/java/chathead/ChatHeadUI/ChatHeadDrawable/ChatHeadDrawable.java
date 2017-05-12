package chathead.ChatHeadUI.ChatHeadDrawable;

/**
 * Created by luvikaser on 01/03/2017.
 */
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class ChatHeadDrawable extends Drawable {
    private AvatarDrawer avatarDrawer;
    private NotificationDrawer notificationDrawer;

    private long id;
    private Object tag;

    public void setAvatarDrawer(AvatarDrawer avatarDrawer){
            this.avatarDrawer = avatarDrawer;
    }

    public void setNotificationDrawer(NotificationDrawer notificationDrawer) {
        this.notificationDrawer = notificationDrawer;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        if (avatarDrawer != null) {
            avatarDrawer.onBoundsChange(bounds);
        }
        if (notificationDrawer != null) {
            notificationDrawer.onBoundsChange(bounds);
        }
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public Object getTag() {
        return tag;
    }

    public long getId() {
        return id;
    }

    @Override
    public void draw(Canvas canvas) {

        //Draw Avatar
        if (avatarDrawer != null){
            avatarDrawer.drawAvatar(canvas);
        }

        //Draw Notification
        if (notificationDrawer != null) {
            notificationDrawer.drawNotification(canvas);
        }

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

}
