package fi.hsl.demoapp.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

//Ugly hack to pass click events to parent view
public class UntouchableRecyclerView extends RecyclerView {

    public UntouchableRecyclerView(Context context) {
        super(context);
    }

    public UntouchableRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UntouchableRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return false;
    }
}