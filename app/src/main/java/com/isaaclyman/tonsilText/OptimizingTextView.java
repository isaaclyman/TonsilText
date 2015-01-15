package com.isaaclyman.tonsilText;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class OptimizingTextView extends TextView {

    private boolean isStale;
    private String workingText;
    private int lineHeight;
    private int maxLines;
    private int currentLines;
    private float lineSpacingMultiplier;
    private float lineAdditionalVerticalPadding;
    public int height;

    private void updateVars() {
        lineHeight = super.getLineHeight();
        currentLines = createWorkingLayout(workingText).getLineCount();
    }

    public OptimizingTextView(Context context) {
        super(context);
    }

    public OptimizingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OptimizingTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setDefaultState(int height) {
        this.height = height;
        maxLines = 1;
        currentLines = 1;
        setLineSpacing(0.0f, 0.9f);
        setLineHeight(getUsableHeight() / 3);
    }

    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
        float convertedTextSize = lineHeight / lineSpacingMultiplier;
        super.setTextSize(TypedValue.COMPLEX_UNIT_PX, Math.round(convertedTextSize));
    }

    private int getUsableHeight() {
        updateVars();
        return Math.round(height - super.getPaddingTop() - super.getPaddingBottom());
    }

    @Override
    public void setMaxLines(int maxLines) {
        super.setMaxLines(maxLines);
        this.maxLines = maxLines;
        currentLines = maxLines;
        isStale = true;
    }

    @Override
    public void setLineSpacing(float add, float mult) {
        this.lineAdditionalVerticalPadding = add;
        this.lineSpacingMultiplier = mult;
        super.setLineSpacing(add, mult);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int before, int after) {
        super.onTextChanged(text, start, before, after);
        workingText = text.toString();
        isStale = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isStale) {
            super.setEllipsize(null);
            resetText();
        }
        super.onDraw(canvas);
    }

    private void resetText() {

        updateVars();
        // If there are less lines than expected...
        if (currentLines < maxLines) {
            // Set the defaults, then allow the following loops to resize
            setDefaultState(height);
        }
        updateVars();
        // If the text is on too many lines...
        if(currentLines > maxLines) {
            // For as long as the text is on too many lines...
            while(currentLines > maxLines) {
                // Shrink it
                lineHeight -= 2;
                setLineHeight(lineHeight);
                currentLines = createWorkingLayout(workingText).getLineCount();
            }
            // If the text is small enough that another line would fit on the layout...
            while(lineHeight < (getUsableHeight() / (maxLines + 1))) {
                // Then add a line and set the line height to match it
                setMaxLines(maxLines + 1);
                setLineHeight(getUsableHeight() / maxLines);
            }
            resetText();
        }

        // Don't continue resetting text
        isStale = false;
    }

    private Layout createWorkingLayout(String workingText) {
        return new StaticLayout(workingText, getPaint(), getWidth() - getPaddingLeft() - getPaddingRight(),
                Alignment.ALIGN_NORMAL, lineSpacingMultiplier, lineAdditionalVerticalPadding, false);
    }
}