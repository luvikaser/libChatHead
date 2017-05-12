package chathead.ChatHeadUI.ChatHeadDrawable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by luvikaser on 01/03/2017.
 */

public class NotificationDrawer {
    private float mNotificationCenterX;
    private float mNotificationCenterY;
    private Rect outerBounds;
    protected float mNotificationPadding; //Pixels
    protected float mNotificationHeight; //Pixels
    protected float mNotificationWidth; //Pixels
    protected double mNotificationAngleFromHorizontal; //Radians
    protected Rect mNotificationBounds;
    protected final Paint mNotificationPaint;
    protected String mNotificationText;
    protected final Paint mNotificationTextPaint;
    private Float radius;
    private Float textSize;

    public NotificationDrawer() {
        mNotificationPaint = new Paint();
        mNotificationPaint.setAntiAlias(true);
        mNotificationPaint.setDither(true);
        mNotificationPaint.setColor(Color.RED);

        mNotificationTextPaint = new Paint();
        mNotificationTextPaint.setColor(Color.WHITE);
        mNotificationTextPaint.setTextSize(90);
        mNotificationTextPaint.setLinearText(true);
        mNotificationTextPaint.setAntiAlias(true);
        mNotificationTextPaint.setDither(true);
        mNotificationTextPaint.setTextAlign(Paint.Align.CENTER);

        this.mNotificationAngleFromHorizontal = 0.785; //Radians (45 Degrees)
        mNotificationBounds = new Rect();
    }

    /**
     * Show the notification ticker text
     *
     * @param notificationText Text to be shown
     */
    public NotificationDrawer setNotificationText(String notificationText) {
        this.mNotificationText = notificationText;
        return this;
    }

    /**
     * Set Notification Angle from horizontal
     *
     * @param notificationAngle angle from horizontal (in Degree).
     */
    public NotificationDrawer setNotificationAngle(int notificationAngle) {
        this.mNotificationAngleFromHorizontal = 3.14 * notificationAngle / 180;
        return this;
    }

    /**
     * Set notification background Color
     *
     * @param textColor       Text color
     * @param backgroundColor Background color
     */
    public NotificationDrawer setNotificationColor(int textColor, int backgroundColor) {
        mNotificationPaint.setColor(backgroundColor);
        mNotificationTextPaint.setColor(textColor);
        return this;
    }

    public void drawNotification(Canvas canvas) {
        draw(canvas, outerBounds, mNotificationCenterX, mNotificationCenterY);
    }

    public void onBoundsChange(Rect bounds) {
        outerBounds = bounds;
        mNotificationTextPaint.setTextSize((bounds.height()) * 0.16f);
        mNotificationPadding = mNotificationTextPaint.getTextSize() * 0.3f;

        int notificationTextLength = mNotificationText.length();
        float textSize = mNotificationTextPaint.getTextSize();
        mNotificationTextPaint.getTextBounds(mNotificationText, 0, notificationTextLength, mNotificationBounds);
        mNotificationCenterX = (float) (bounds.centerX() + (bounds.width() / 2) * Math.cos(mNotificationAngleFromHorizontal));
        //Adjust notificationCenterX so that it shall not go out of bounds
        float effectiveWidth = mNotificationBounds.width() + mNotificationPadding * 2;
        if (mNotificationCenterX + effectiveWidth / 2 > bounds.right) mNotificationCenterX = bounds.right - effectiveWidth / 2;
        else if (mNotificationCenterX - effectiveWidth / 2 < bounds.left) mNotificationCenterX = bounds.left + effectiveWidth / 2;

        mNotificationCenterY = (float) (bounds.centerY() - (bounds.width() / 2) * Math.sin(mNotificationAngleFromHorizontal));
    }

    public void draw(Canvas canvas, Rect outerBounds, float notificationCenterX, float notificationCenterY) {
        if (outerBounds == null) return;

        //Adjust notificationCenterY so that it shall not go out of bounds
        float effectiveWidth = getEffectiveWidth();

        if (notificationCenterY - effectiveWidth / 2 < outerBounds.top) notificationCenterY = outerBounds.top + effectiveWidth / 2;
        else if (notificationCenterY + effectiveWidth / 2 > outerBounds.bottom) notificationCenterY = outerBounds.bottom - effectiveWidth / 2;

        if (notificationCenterX + effectiveWidth / 2 > outerBounds.right) notificationCenterX = outerBounds.right - effectiveWidth / 2;
        else if (notificationCenterX - effectiveWidth / 2 < outerBounds.left) notificationCenterX = outerBounds.left + effectiveWidth / 2;

        canvas.drawCircle(notificationCenterX, notificationCenterY, effectiveWidth / 2, mNotificationPaint);

        prepareNotificationTextPaint();

        canvas.drawText(mNotificationText, 0, mNotificationText.length(), notificationCenterX, notificationCenterY - (mNotificationTextPaint.ascent() +
                mNotificationTextPaint.descent()) / 2, mNotificationTextPaint);
    }

    private void prepareNotificationTextPaint() {
        if (textSize != null) mNotificationTextPaint.setTextSize(textSize);
    }

    private float getEffectiveWidth() {
        if (radius == null) return mNotificationBounds.width() + mNotificationPadding * 2;
        else return 2 * radius;
    }}