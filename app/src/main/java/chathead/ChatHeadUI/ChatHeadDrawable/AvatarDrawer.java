package chathead.ChatHeadUI.ChatHeadDrawable;

/**
 * Created by luvikaser on 01/03/2017.
 */
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.widget.ImageView;

public class AvatarDrawer {
    private static final float SHADOW_RADIUS = 4f;
    private static final float SHADOW_DX = 0f;
    private static final float SHADOW_DY = 4f;
    private static final int SHADOW_COLOR = Color.BLACK;

    private Bitmap bitmap;
    private Shader bitmapShader;
    private RectF mRect;
    private Paint mPaint;
    private ImageView.ScaleType scaleType = ImageView.ScaleType.FIT_CENTER;

    public AvatarDrawer(Bitmap bitmap, Shader shader){
        this.bitmap = bitmap;
        this.bitmapShader = shader;
        mRect = new RectF();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }
    public void drawAvatar(Canvas canvas){
        mPaint.setShader(bitmapShader);
        mPaint.setShadowLayer(SHADOW_RADIUS, SHADOW_DX, SHADOW_DY, SHADOW_COLOR);
        canvas.drawCircle(mRect.centerX(), mRect.centerY(), mRect.width() / 2 - SHADOW_DY, mPaint);
    }
    protected void onBoundsChange(Rect bounds) {
        mRect.set(bounds.left, bounds.top, bounds.right, bounds.bottom);
        bitmapShader.setLocalMatrix(getLocalMatrix());

    }
    private Matrix getLocalMatrix() {
        float scale;
        Matrix matrix = new Matrix();
        if (scaleType == ImageView.ScaleType.CENTER_CROP) {
            if (bitmap.getHeight() > bitmap.getWidth()) {
                //Portrait
                scale = (mRect.width()) / bitmap.getWidth();
                //Portrait we don't want to translate to middle, since most of the faces are in top area, not in center
                matrix.setScale(scale, scale);
                matrix.postTranslate(mRect.left, mRect.top);
            } else {
                //Landscape
                scale = (mRect.height()) / bitmap.getHeight();
                float difference = mRect.width() - bitmap.getWidth() * scale;
                matrix.setScale(scale, scale);
                matrix.postTranslate(mRect.left + difference / 2, mRect.top);
            }
        } else if (scaleType == ImageView.ScaleType.FIT_CENTER) {
            RectF bitmapRect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
            float divisor = 1.414f; //sqrt of 2 because inner square fitting inside circle has a side of circle's diameter divided by sqrt(2)
            float sideDimension =  (Math.max(mRect.width(), mRect.height()) / divisor); // side of the inner square
            float remainingSpace = (Math.max(mRect.width(), mRect.height()) - sideDimension); // empty space used for center aligning
            RectF newRect = new RectF(mRect.left + (remainingSpace / 2f), mRect.top + (remainingSpace / 2f), mRect.left + (remainingSpace / 2f) + sideDimension, mRect.top + (remainingSpace / 2f) + sideDimension);
            matrix.setRectToRect(bitmapRect, newRect, Matrix.ScaleToFit.CENTER);
        }
        return matrix;
    }
}