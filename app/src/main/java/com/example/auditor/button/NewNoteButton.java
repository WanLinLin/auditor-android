package com.example.auditor.button;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.example.auditor.R;

/**
 * Created by Wan Lin on 2015/10/15.
 * NewNoteButton
 */
public class NewNoteButton extends View {
    private static final String LOG_TAG = "NewNoteButton";
    private Paint mPaint;

    public NewNoteButton(Context context) {
        super(context);
        init();
    }

    public NewNoteButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NewNoteButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) getResources().getDimension(R.dimen.new_note_button_size), (int) getResources().getDimension(R.dimen.new_note_button_size));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int x = getMeasuredWidth() / 2;
        int y = (int) ((getMeasuredHeight() / 2) - ((mPaint.descent() + mPaint.ascent()) / 2));
        canvas.drawText("+", x, y, mPaint);
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(getResources().getDimension(R.dimen.new_note_button_size) / 2);
        mPaint.setTextAlign(Paint.Align.CENTER);

        // on long click call startDrag()
        this.setTag("newNoteButton");
        this.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                // Create a new ClipData.Item from the NewNoteButton's tag
                ClipData.Item item = new ClipData.Item(v.getTag().toString());

                // Create a new ClipData using the tag as a label, the plain text MIME type, and
                // the already-created item. This will create a new ClipDescription object within the
                // ClipData, and set its MIME type entry to "text/plain"
                ClipData dragData = new ClipData(v.getTag().toString(), new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);

                // Instantiates the drag shadow builder.
                View.DragShadowBuilder myShadow = new MyDragShadowBuilder(NewNoteButton.this);

                // Starts the drag
                v.startDrag(dragData,   // the data to be dragged
                        myShadow,       // the drag shadow builder
                        null,           // no need to use local data
                        0               // flags (not currently used, set to 0)
                );

                return false;
            }
        });
    }

    private static class MyDragShadowBuilder extends View.DragShadowBuilder {
        private static Drawable shadow;

        public MyDragShadowBuilder(View v) {
            super(v);
            shadow = new ColorDrawable(Color.LTGRAY);
        }

        @Override
        public void onProvideShadowMetrics (Point size, Point touch) {
            int width, height;

            width = (int) (getView().getWidth() * 0.7);
            height = (int) (getView().getHeight() * 0.7);

            // shadow rectangle
            shadow.setBounds(0, 0, width, height);

            // Sets the size parameter's width and height values. These get back to the system
            // through the size parameter.
            size.set(width, height);

            // Sets the touch point's position to be in the middle of the drag shadow
            touch.set(width / 2, height / 2);
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            shadow.draw(canvas);
        }
    }
}
