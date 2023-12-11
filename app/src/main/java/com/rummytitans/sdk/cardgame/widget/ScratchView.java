/*
 *
 * Copyright 2016 Harish Sridharan

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rummytitans.sdk.cardgame.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.rummytitans.sdk.cardgame.R;

import java.nio.ByteBuffer;

public class ScratchView extends View {

    public static final float STROKE_WIDTH = 12f;
    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    /**
     * Bitmap holding the scratch region.
     */
    private Bitmap mScratchBitmap;
    /**
     * Drawable canvas area through which the scratchable area is drawn.
     */
    private Canvas mCanvas;
    /**
     * Path holding the erasing path done by the user.
     */
    private Path mErasePath;
    /**
     * Path to indicate where the user have touched.
     */
    private Path mTouchPath;
    /**
     * Paint properties for drawing the scratch area.
     */
    private Paint mBitmapPaint;
    /**
     * Paint properties for erasing the scratch region.
     */
    private Paint mErasePaint;
    /**
     * Gradient paint properties that lies as a background for scratch region.
     */
    private Paint mGradientBgPaint;
    /**
     * Sample Drawable bitmap having the scratch pattern.
     */
    private BitmapDrawable mDrawable;
    /**
     * Listener object callback reference to send back the callback when the image has been revealed.
     */
    private IRevealListener mRevealListener;
    /**
     * Reveal percent value.
     */
    private float mRevealPercent;
    /**
     * Thread Count
     */
    private int mThreadCount = 0;

    public ScratchView(Context context) {
        super(context);
        init();
    }


    public ScratchView(Context context, AttributeSet set) {
        super(context, set);
        init();
    }

    public ScratchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Set the strokes width based on the parameter multiplier.
     *
     * @param multiplier can be 1,2,3 and so on to set the stroke width of the paint.
     */
    public void setStrokeWidth(int multiplier) {
        mErasePaint.setStrokeWidth(multiplier * STROKE_WIDTH);
    }


    public void resetView() {
        init();
        resetRevealed();
        mCanvas = new Canvas();
        mErasePath = new Path();
        mGradientBgPaint = new Paint();
        mTouchPath = new Path();
        invalidate();

    }

    public void resetRevealed() {
        mRevealPercent = 0;
    }

    /**
     * Initialises the paint drawing elements.
     */
    public void init() {
        mTouchPath = new Path();
        mErasePaint = new Paint();
        mErasePaint.setAntiAlias(true);
        mErasePaint.setDither(true);
        mErasePaint.setColor(0xFFFF0000);
        mErasePaint.setStyle(Paint.Style.STROKE);
        mErasePaint.setStrokeJoin(Paint.Join.BEVEL);
        mErasePaint.setStrokeCap(Paint.Cap.ROUND);
        setStrokeWidth(6);
        mGradientBgPaint = new Paint();
        mErasePath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        Bitmap scratchBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.scratch_card);
        mDrawable = new BitmapDrawable(getResources(), scratchBitmap);
        mDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        setEraserMode();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mScratchBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mScratchBitmap);
        Rect rect = new Rect(0, 0, mScratchBitmap.getWidth(), mScratchBitmap.getHeight());
        mDrawable.setBounds(rect);
        int startGradientColor = ContextCompat.getColor(getContext(), R.color.scratch_start_gradient);
        int endGradientColor = ContextCompat.getColor(getContext(), R.color.scratch_end_gradient);
        mGradientBgPaint.setShader(new LinearGradient(0, 0, 0, getHeight(), startGradientColor, endGradientColor, Shader.TileMode.MIRROR));
        mCanvas.drawRect(rect, mGradientBgPaint);
        mDrawable.draw(mCanvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mScratchBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(mErasePath, mErasePaint);
    }

    private void touch_start(float x, float y) {
        mErasePath.reset();
        mErasePath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    /**
     * clears the scratch area to reveal the hidden image.
     */
    public void clear() {
        int[] bounds = getImageBounds();
        int left = bounds[0];
        int top = bounds[1];
        int right = bounds[2];
        int bottom = bounds[3];
        int width = right - left;
        int height = bottom - top;
        int centerX = left + width / 2;
        int centerY = top + height / 2;
        left = centerX - width / 2;
        top = centerY - height / 2;
        right = left + width;
        bottom = top + height;
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mCanvas.drawRect(left, top, right, bottom, paint);
        checkRevealed();
        invalidate();
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mErasePath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
            drawPath();
        }
        mTouchPath.reset();
        mTouchPath.addCircle(mX, mY, 30, Path.Direction.CW);
    }

    public void drawPath() {
        mErasePath.lineTo(mX, mY);
        // commit the path to our offscreen
        mCanvas.drawPath(mErasePath, mErasePaint);
        // kill this so we don't double draw
        mTouchPath.reset();
        mErasePath.reset();
        mErasePath.moveTo(mX, mY);

        checkRevealed();
    }

    public void reveal() {
        clear();
    }

    private void touch_up() {
        drawPath();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
            default:
                break;
        }
        return true;
    }

    public int getColor() {
        return mErasePaint.getColor();
    }

    public Paint getErasePaint() {
        return mErasePaint;
    }

    public void setEraserMode() {
        getErasePaint().setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    public void setRevealListener(IRevealListener listener) {
        this.mRevealListener = listener;
    }

    public boolean isRevealed() {
        return mRevealPercent == 1;
    }

    public void setRevealed() {
        mRevealPercent = 1;
    }

    @SuppressLint("StaticFieldLeak")
    private void checkRevealed() {

        if (!isRevealed() && mRevealListener != null) {

            int[] bounds = getImageBounds();
            int left = bounds[0];
            int top = bounds[1];
            int width = bounds[2] - left;
            int height = bounds[3] - top;

            // Do not create multiple calls to compare.
            if (mThreadCount > 1) {
                Log.d("Captcha", "Count greater than 1");
                return;
            }

            mThreadCount++;

            new AsyncTask<Integer, Void, Float>() {

                @Override
                protected Float doInBackground(Integer... params) {

                    try {
                        int left = params[0];
                        int top = params[1];
                        int width = params[2];
                        int height = params[3];
                        Bitmap croppedBitmap = Bitmap.createBitmap(mScratchBitmap, left, top, width, height);
                        if (croppedBitmap == null) return 0f;
                        ByteBuffer buffer = ByteBuffer.allocate(croppedBitmap.getHeight() * croppedBitmap.getRowBytes());
                        croppedBitmap.copyPixelsToBuffer(buffer);
                        byte[] array = buffer.array();
                        int len = array.length;
                        int count = 0;
                        for (int i = 0; i < len; i++) if (array[i] == 0) count++;
                        return ((float) (count)) / len;
                    } catch (Exception e) {
                        return ((float) 0);
                    } finally {
                        mThreadCount--;
                    }
                }

                public void onPostExecute(Float percentRevealed) {
                    if (!isRevealed()) {
                        if (percentRevealed == 0) percentRevealed = .5f;
                        float oldValue = mRevealPercent;
                        mRevealPercent = percentRevealed;
                        if (oldValue != percentRevealed)
                            mRevealListener.onRevealPercentChangedListener(ScratchView.this, percentRevealed);
                        // if now revealed.
                        if (isRevealed()) mRevealListener.onRevealed(ScratchView.this);
                    }
                }
            }.execute(left, top, 300, 181);

        }
    }

    public int[] getImageBounds() {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        int vwidth = getWidth() - paddingLeft - paddingRight;
        int vheight = getHeight() - paddingBottom - paddingTop;
        int centerY = vheight / 2;
        Drawable drawable = new BitmapDrawable();
        Rect bounds = drawable.getBounds();
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        if (width <= 0) width = bounds.right - bounds.left;
        if (height <= 0) height = bounds.bottom - bounds.top;
        int left;
        int top;
        if (height > vheight) height = vheight;
        if (width > vwidth) width = vwidth;
        left = paddingLeft;
        top = centerY - height / 2;
        return new int[]{left, top, left + width, top + height};
    }

    public interface IRevealListener {
        void onRevealed(ScratchView iv);

        void onRevealPercentChangedListener(ScratchView siv, float percent);
    }
}
