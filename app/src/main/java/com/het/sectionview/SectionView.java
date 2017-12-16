package com.het.sectionview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * @author: houtrry
 * @time: 2017/12/15 16:35
 * @version: $Rev$
 * @description: ${TODO}
 */

public class SectionView extends View {

    private static final String TAG = SectionView.class.getSimpleName();

    private int[] mSectionColors = {Color.RED, Color.BLUE, Color.GREEN, Color.parseColor("#ff33b5e5")};

    private float[] mCriticalValues = {0, 50, 70, 90, 100};

    /**
     * 如果mSectionProportions的数据为空, 则按等比例显示, 否则, 按mSectionProportions中提供的比例显示
     */
    private float[] mSectionProportions = {0.4f, 0.2f, 0.2f, 0.2f};
//    private float[] mSectionProportions;

    private String[] mSectionDesc = {"较差", "一般", "良好", "优秀"};

    private int mDescTextColor = Color.BLACK;
    private float mDescTextSize = 36;
    private Paint mDescPaint;

    private float mCriticalTextSize = 36;
    private int mCriticalTextColor = Color.GRAY;
    private Paint mCriticalPaint;

    private float mSectionLineHeight = 20;
    private Paint mSectionLinePaint;
    private int mWidth;
    private int mHeight;
    private int mStartX;
    private int mStartY;
    private int mLineWidth;

    private float mTopTextMargin = 20;
    private float mBottomTextMargin = 20;

    private float mCurrentPointValue = 53;
    private int mCurrentPointColor = Color.parseColor("#ffff8800");
    private float mCurrentPointRadius = 15;
    private int mCurrentPointBorderColor = Color.DKGRAY;
    private float mCurrentPointBorderWidth = 2;
    private Paint mCurrentPointPaint;
    private Paint mCurrentPointBorderPaint;


    public SectionView(Context context) {
        this(context, null);
    }

    public SectionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SectionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        mStartX = paddingLeft;
        mStartY = getPaddingTop();

        mLineWidth = w - paddingLeft - paddingRight;
        Log.d(TAG, "onSizeChanged: mStartX: " + mStartX + ", mLineWidth: " + mLineWidth);

        mCenterVertical = mHeight * 0.5f;
        createBorderPath(mWidth, mHeight, mSectionLineHeight, paddingLeft, paddingRight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawLine(canvas);
        drawText(canvas);
        drawCurrentPoint(canvas);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private void init(Context context, AttributeSet attrs) {
        initAttrs(context, attrs);
        initPaint();
    }

    private void initAttrs(Context context, AttributeSet attrs) {

    }

    private void initPaint() {
        mDescPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDescPaint.setColor(mDescTextColor);
        mDescPaint.setTextSize(mDescTextSize);

        mCriticalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCriticalPaint.setTextSize(mCriticalTextSize);
        mCriticalPaint.setColor(mCriticalTextColor);

        mSectionLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSectionLinePaint.setStrokeWidth(mSectionLineHeight);

        mCurrentPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCurrentPointPaint.setColor(mCurrentPointColor);
        mCurrentPointPaint.setStyle(Paint.Style.FILL);

        mCurrentPointBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCurrentPointBorderPaint.setColor(mCurrentPointBorderColor);
        mCurrentPointBorderPaint.setStrokeWidth(mCurrentPointBorderWidth);
        mCurrentPointBorderPaint.setStyle(Paint.Style.STROKE);
    }

    private int measureWidth(int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        float topTextWidth = 0;
        float bottomTextWidth = 0;
        if (mode == MeasureSpec.AT_MOST) {
            for (int i = 0; i < mCriticalValues.length - 1; i++) {
                topTextWidth += mDescPaint.measureText(mSectionDesc[i]);
                bottomTextWidth += mCriticalPaint.measureText(getFormatText(mCriticalValues[i], FORMAT_STRING_0));
            }
            bottomTextWidth += mCriticalPaint.measureText(getFormatText(mCriticalValues[mCriticalValues.length - 1], FORMAT_STRING_0));
            //文字总宽度的120%
            size = (int) ((topTextWidth > bottomTextWidth ? topTextWidth : bottomTextWidth) * 1.2f + 0.5f);
        }
        return size;
    }

    private int measureHeight(int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.AT_MOST) {
            size = (int) (mSectionLineHeight + mTopTextMargin + mBottomTextMargin + getTextHeight(mSectionDesc[0], mDescPaint, mRect) + getTextHeight(getFormatText(mCriticalValues[mCriticalValues.length - 1], FORMAT_STRING_0), mCriticalPaint, mRect) + 0.5f);
        }
        return size;
    }

    private float mCurrentStartX = 0;
    private float mCurrentEndX = 0;
    private float mCenterVertical = 0;

    private void drawLine(Canvas canvas) {
        canvas.save();
        canvas.clipPath(mBorderPath);
        mCurrentStartX = mStartX;
        mCurrentEndX = 0;
        for (int i = 0; i < mCriticalValues.length - 1; i++) {
            mSectionLinePaint.setColor(mSectionColors[i]);
            mCurrentEndX = mCurrentStartX + getLineWidth(i);
            canvas.drawLine(mCurrentStartX, mCenterVertical, mCurrentEndX, mCenterVertical, mSectionLinePaint);
            mCurrentStartX = mCurrentEndX;
        }
        canvas.restore();
    }

    private void drawText(Canvas canvas) {
        mCurrentStartX = mStartX;
        mCurrentEndX = 0;
        for (int i = 0; i < mCriticalValues.length - 1; i++) {
            mCurrentEndX = mCurrentStartX + getLineWidth(i);

            drawTopText(canvas, mSectionDesc[i], mCurrentStartX, mCurrentEndX);

            drawBottomText(canvas, i, mCurrentStartX);
            mCurrentStartX = mCurrentEndX;
        }
        drawBottomText(canvas, mCriticalValues.length - 1, mCurrentStartX);
    }

    private void drawCurrentPoint(Canvas canvas) {
        canvas.drawCircle(getCurrentPointX(), mCenterVertical, mCurrentPointRadius, mCurrentPointPaint);
        canvas.drawCircle(getCurrentPointX(), mCenterVertical, mCurrentPointRadius, mCurrentPointBorderPaint);
    }

    private Rect mRect = new Rect();

    private void drawTopText(Canvas canvas, String text, float currentStartX, float currentEndX) {
        canvas.drawText(text, (currentStartX + currentEndX - mDescPaint.measureText(text)) * 0.5f, mCenterVertical - mSectionLineHeight * 0.5f - mTopTextMargin, mDescPaint);
    }

    private void drawBottomText(Canvas canvas, int position, float currentStartX) {
        String text = getFormatText(mCriticalValues[position], FORMAT_STRING_0);
        float textOffset = mDescPaint.measureText(text) * 0.5f;
        float x = currentStartX;
        if (position == mCriticalValues.length - 1) {
            x -= textOffset * 2;
        } else if (position > 0) {
            x -= textOffset;
        }

//        mCriticalPaint.getTextBounds(text, 0, text.length(), mRect);
        canvas.drawText(text, x, mCenterVertical + mSectionLineHeight * 0.5f + mBottomTextMargin + getTextHeight(text, mCriticalPaint, mRect), mCriticalPaint);
    }

    private float getTextHeight(String text, Paint paint, Rect rect) {
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect.height();
    }

    /**
     * 整数
     */
    private static final String FORMAT_STRING_0 = "#";
    /**
     * 保留一位小数
     */
    private static final String FORMAT_STRING_1 = "##.#";
    /**
     * 保留两位小数
     */
    private static final String FORMAT_STRING_2 = "##.##";
    /**
     * 使用%表示
     */
    private static final String FORMAT_STRING_PERCENT = "#%";

    private String getFormatText(float value, String format) {
        final DecimalFormat df = new DecimalFormat(format);
        return df.format(value);
    }

    private float getLineWidth(int position) {
        float proportion = 0;
        if (mSectionProportions == null || mSectionProportions.length == 0) {
            proportion = (mCriticalValues[position + 1] - mCriticalValues[position]) / (mCriticalValues[mCriticalValues.length - 1] - mCriticalValues[0]);
        } else {
            proportion = mSectionProportions[position];
        }
        return mLineWidth * proportion;
    }

    private Path mBorderPath;

    private void createBorderPath(int width, float height, float lineHeight, int paddingLeft, int paddingRight) {
        if (mBorderPath == null) {
            mBorderPath = new Path();
        } else {
            mBorderPath.reset();
        }
        final float radius = lineHeight * 0.5f;
        final float centerVertical = height * 0.5f;
        final float lineWidth = width - paddingLeft - paddingRight;
        mBorderPath.moveTo(paddingLeft + radius, centerVertical - radius);
        mBorderPath.rLineTo(lineWidth - radius * 2, 0);
        mBorderPath.rQuadTo(radius, 0, radius, radius);
        mBorderPath.rQuadTo(0, radius, -radius, radius);
        mBorderPath.rLineTo(-lineWidth + radius * 2, 0);
        mBorderPath.rQuadTo(-radius, 0, -radius, -radius);
        mBorderPath.rQuadTo(0, -radius, radius, -radius);
    }

    private float getCurrentPointX() {

        if (mCurrentPointValue <= mCriticalValues[0]) {
            return mStartX;
        } else if (mCurrentPointValue >= mCriticalValues[mCriticalValues.length - 1]) {
            return mStartX + mLineWidth;
        }
        if (mSectionProportions == null || mSectionProportions.length == 0) {
            return mStartX + mLineWidth * (mCurrentPointValue - mCriticalValues[0]) / (mCriticalValues[mCriticalValues.length - 1] - mCriticalValues[0]);
        }
        float currentPercent = 0;
        for (int i = 0; i < mCriticalValues.length - 1; i++) {
            // FIXME: 2017/12/16 可以使用二分法查找优化这里的算法
            if (mCurrentPointValue >= mCriticalValues[i] && mCurrentPointValue < mCriticalValues[i + 1]) {
                return mStartX + mLineWidth * (currentPercent + mSectionProportions[i] * ((mCurrentPointValue - mCriticalValues[i]) / (mCriticalValues[i + 1] - mCriticalValues[i])));
            }
            currentPercent += mSectionProportions[i];
        }
        throw new IllegalArgumentException("mCurrentPointValue is no between mCriticalValues, and mCurrentPointValue is " + mCurrentPointValue + ", mCriticalValues is " + Arrays.toString(mCriticalValues));
    }

}
