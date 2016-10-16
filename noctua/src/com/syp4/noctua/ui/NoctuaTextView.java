package com.syp4.noctua.ui;

import android.content.*;
import android.content.res.*;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.*;
import android.widget.*;
import com.syp4.noctua.R;

public class NoctuaTextView extends TextView
{
    public NoctuaTextView(Context context)
    {
        this(context, null);
    }

    public NoctuaTextView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public NoctuaTextView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomFonts);
        String customFont = a.getString(R.styleable.CustomFonts_customFont);
        SetCustomFont(customFont);
        a.recycle();
    }

    public void SetCustomFont(String asset)
    {
        Typeface tf;
        try
        {
            tf = Typeface.createFromAsset(getContext().getAssets(), asset);
        }
        catch (Exception e)
        {
            Log.e("NoctuaTextView", "Could not get Typeface: " + asset + " Error: " + e);
            return;
        }

        if (null == tf) return;

        setTypeface(tf);
    }
}
