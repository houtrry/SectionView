package com.het.sectionview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
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

    /**
     * 中间分段线条各段的颜色
     */
    private int[] mSectionColors = {Color.RED, Color.BLUE, Color.GREEN, Color.parseColor("#ff33b5e5")};

    /**
     * 中间分段的分割值
     */
    private float[] mCriticalValues = {0, 50, 70, 90, 100};

    /**
     * 如果mSectionProportions的数据为空, 则按等比例显示, 否则, 按mSectionProportions中提供的比例显示
     */
    private float[] mSectionProportions = {0.4f, 0.2f, 0.2f, 0.2f};

    /**
     * 中间分段的各段占比
     */
//    private float[] mSectionProportions;

    /**
     * 中间分段的描述, 位于分段线的上面
     */
    private String[] mSectionDesc = {"较差", "一般", "良好", "优秀"};

    /**
     * 描述的文字颜色
     */
    private int mDescTextColor = Color.BLACK;
    /**
     * 描述的文字大小
     */
    private float mDescTextSize = 36;
    private Paint mDescPaint;

    /**
     * 分割值的文字大小
     */
    private float mCriticalTextSize = 36;
    /**
     * 分割值的文字颜色
     */
    private int mCriticalTextColor = Color.GRAY;
    private Paint mCriticalPaint;

    private float mSectionLineHeight = 20;
    private Paint mSectionLinePaint;
    private int mWidth;
    private int mHeight;
    private int mStartX;
    private int mStartY;
    private int mLineWidth;

    private float mDescTextMarginTop = 20;
    private float mCriticalTextMarginBottom = 20;

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

    public void setSectionColors(int[] sectionColors) {
        mSectionColors = sectionColors;
    }

    public void setCriticalValues(float[] criticalValues) {
        mCriticalValues = criticalValues;
    }

    public void setSectionProportions(float[] sectionProportions) {
        mSectionProportions = sectionProportions;
    }

    public void setSectionDesc(String[] sectionDesc) {
        mSectionDesc = sectionDesc;
    }

    public void setCurrentPointValue(float currentPointValue) {
        this.mCurrentPointValue = currentPointValue;
    }

    public float getCurrentPointValue() {
        return mCurrentPointValue;
    }

    /**
     * 出发UI修改
     */
    public void show() {
        checkArgs();
        ViewCompat.postInvalidateOnAnimation(this);
        if (mOnValueChangeListener != null) {
            mOnValueChangeListener.onValueChange(this, mCurrentPointValue);
        }
    }

    private void init(Context context, AttributeSet attrs) {
        initAttrs(context, attrs);
        initPaint();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SectionView);
        mDescTextColor = typedArray.getColor(R.styleable.SectionView_descTextColor, Color.BLACK);
        mDescTextSize = typedArray.getDimensionPixelSize(R.styleable.SectionView_descTextSize, 36);
        mCriticalTextColor = typedArray.getColor(R.styleable.SectionView_criticalTextColor, Color.GRAY);
        mCriticalTextSize = typedArray.getDimensionPixelSize(R.styleable.SectionView_criticalTextSize, 36);
        mSectionLineHeight = typedArray.getDimensionPixelSize(R.styleable.SectionView_sectionLineHeight, 20);
        mDescTextMarginTop = typedArray.getDimensionPixelSize(R.styleable.SectionView_descTextMarginTop, 20);
        mCriticalTextMarginBottom = typedArray.getDimensionPixelSize(R.styleable.SectionView_criticalTextMarginBottom, 20);
        mCurrentPointColor = typedArray.getColor(R.styleable.SectionView_currentPointColor, Color.parseColor("#ffff8800"));
        mCurrentPointRadius = typedArray.getDimensionPixelSize(R.styleable.SectionView_currentPointRadius, 15);
        mCurrentPointBorderColor = typedArray.getColor(R.styleable.SectionView_currentPointBorderColor, Color.DKGRAY);
        mCurrentPointBorderWidth = typedArray.getDimensionPixelSize(R.styleable.SectionView_currentPointBorderWidth, 2);
        typedArray.recycle();
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
            size = (int) (Math.max(topTextWidth, bottomTextWidth) * 1.2f + 0.5f);

        }
        return size;
    }

    private int measureHeight(int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.AT_MOST) {
            //这里*1.1的原因: 获取的文字高度并不是文字实际显示的高度, 为了避免文字显示不全, 故作此处理
            size = (int) ((mSectionLineHeight + mDescTextMarginTop + mCriticalTextMarginBottom + getTextHeight(mSectionDesc[0], mDescPaint, mRect) + getTextHeight(getFormatText(mCriticalValues[mCriticalValues.length - 1], FORMAT_STRING_0), mCriticalPaint, mRect) * 1.1f) + 0.5f);
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
        canvas.drawText(text, (currentStartX + currentEndX - mDescPaint.measureText(text)) * 0.5f, mCenterVertical - mSectionLineHeight * 0.5f - mDescTextMarginTop, mDescPaint);
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
        canvas.drawText(text, x, mCenterVertical + mSectionLineHeight * 0.5f + mCriticalTextMarginBottom + getTextHeight(text, mCriticalPaint, mRect), mCriticalPaint);
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

    /**
     * 检查各个参数是否正常(主要是检查几个数据是否正常)
     */
    private void checkArgs() {
        if (mCriticalValues == null || mCriticalValues.length < 2) {
            throw new IllegalArgumentException("the arg mCriticalValues is not available, please check first.  mCriticalValues is " + mCriticalValues);
        }
        if (mSectionColors == null || mSectionColors.length != mCriticalValues.length - 1) {
            throw new IllegalArgumentException("the arg mSectionColors is not available, please check first.  mSectionColors is " + mSectionColors);
        }
        if (mSectionDesc == null || mSectionDesc.length != mCriticalValues.length - 1) {
            throw new IllegalArgumentException("the arg mSectionDesc is not available, please check first.  mSectionDesc is " + mSectionDesc);
        }

        if (mSectionProportions != null) {
            float sumSectionProportion = 0;
            for (int i = 0; i < mSectionProportions.length; i++) {
                float proportion = mSectionProportions[i];
                if (proportion < 0 || proportion > 1) {
                    throw new IllegalArgumentException("the arg mSectionProportions is not available, please check first.  mSectionProportions is " + Arrays.toString(mSectionProportions) + ", and mSectionProportions[" + i + "] is " + proportion);
                }
                sumSectionProportion += proportion;
            }
            if (sumSectionProportion != 1) {
                throw new IllegalArgumentException("the arg mSectionProportions is not available, please check first.  mSectionProportions is " + Arrays.toString(mSectionProportions) + ", and sum of mSectionProportions is " + sumSectionProportion);
            }
        }
    }

    private OnValueChangeListener mOnValueChangeListener;

    public void setOnValueChangeListener(OnValueChangeListener onValueChangeListener) {
        mOnValueChangeListener = onValueChangeListener;
    }

    public interface OnValueChangeListener {

        /**
         * 默认方法, 在Android中需要1.Java8的支持; 2.需要Android的最小版本为24
         * 由于第二条, 看来在实际项目中使用还是有问题的
         *
         * @param sectionView
         * @param value
         */
        default void onValueChange(SectionView sectionView, float value) {

        }
    }

}
