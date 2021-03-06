// Copyright (c) 2014, Intel Corporation
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
// 1. Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
// 3. Neither the name of the copyright holder nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package com.intel.inde.mp.samples.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class RangeSelector extends View
{
    public static final int HandleSize = 48;
    public static final int BarHeight = 8;

    public interface RangeSelectorEvents
    {
        public void onStartPositionChanged(int position);

        public void onEndPositionChanged(int position);
    }

    private Handle[] mHandles;
    private Handle mHandleToMove;

    private Paint mBarPaint;
    private Paint mHandlePaint;
    private Paint mHandleHaloPaint;

    private int mWidth;
    private int mHeight;

    private Rect mBarRect;

    RangeSelectorEvents mEventsListener;

    public RangeSelector(Context context)
    {
        super(context);

        init(null, 0);
    }

    public RangeSelector(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        init(attrs, 0);
    }

    public RangeSelector(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        init(attrs, defStyle);
    }

    public void setEventsListener(RangeSelectorEvents listener) {
        mEventsListener = listener;
    }

    public int getStartPosition()
    {
        return mHandles[0].getPosition();
    }

    public void setStartPosition(int position)
    {
        mHandles[0].setPosition(position);

        invalidate();
    }

    public int getEndPosition()
    {
        return mHandles[1].getPosition();
    }

    public void setEndPosition(int position)
    {
        mHandles[1].setPosition(position);

        invalidate();
    }

    private void init(AttributeSet attrs, int defStyle)
    {
        setFocusable(true);

        mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarPaint.setColor(Color.GRAY);

        mHandleHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHandleHaloPaint.setColor(Color.BLACK);
        mHandleHaloPaint.setAlpha(0x50);

        mHandlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHandlePaint.setColor(0xff8285E2);

        mHandles = new Handle[2];

        mHandles[0] = new Handle();
        mHandles[0].setId(0);
        mHandles[0].setPosition(0);

        mHandles[1] = new Handle();
        mHandles[1].setId(1);
        mHandles[1].setPosition(100);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        int cy = mBarRect.top + (mBarRect.bottom - mBarRect.top) / 2;
        int cx;

        canvas.drawRect(mBarRect, mBarPaint);

        for (Handle handle : mHandles)
        {
            cx = positionToX(handle.getPosition());

            canvas.drawCircle(cx, cy, HandleSize / 2, mHandleHaloPaint);
            canvas.drawCircle(cx, cy, HandleSize / 4, mHandlePaint);
        }

        canvas.drawRect(positionToX(mHandles[0].getPosition()), mBarRect.top, positionToX(mHandles[1].getPosition()), mBarRect.bottom, mHandlePaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        mWidth = w;
        mHeight = h;

        mBarRect = new Rect();
        mBarRect.left = HandleSize / 2;
        mBarRect.right = mWidth - HandleSize / 2;
        mBarRect.top = mHeight / 2 - BarHeight / 2;
        mBarRect.bottom = mBarRect.top + BarHeight / 2;
    }

    public boolean onTouchEvent(MotionEvent event)
    {
        int action = event.getAction();

        int x = (int) event.getX();
        int y = (int) event.getY();

        int cy = mBarRect.top + (mBarRect.bottom - mBarRect.top) / 2;
        int cx;

        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
            {
                mHandleToMove = null;

                for (Handle handle : mHandles)
                {
                    cx = positionToX(handle.getPosition());

                    if (pointInsideRect(cx - HandleSize, cx + HandleSize, cy - HandleSize, cy + HandleSize, x, y))
                    {
                        mHandleToMove = handle;

                        break;
                    }
                }

                // Hit test on handles failed
                // let's pick handle what is more
                // close to touch point
                if(mHandleToMove == null)
                {
                    if(Math.abs(x - positionToX(mHandles[0].getPosition())) < Math.abs(x - positionToX(mHandles[1].getPosition())))
                    {
                        mHandleToMove = mHandles[0];
                    }
                    else
                    {
                        mHandleToMove = mHandles[1];
                    }
                }
            }
            break;

            //Touch drag with the knob
            case MotionEvent.ACTION_MOVE:
            {
                if (mHandleToMove != null)
                {
                    int position = x * 100 / mWidth;

                    if (position > 0 && position < 100)
                    {
                        if (mHandleToMove.getId() == 0)
                        {
                            if ((x + HandleSize * 2) > positionToX(mHandles[1].getPosition()))
                            {
                                position = -1;
                            }
                        }
                        else if (mHandleToMove.getId() == 1)
                        {
                            if ((x - HandleSize * 2) < positionToX(mHandles[0].getPosition()))
                            {
                                position = -1;
                            }
                        }
                    }
                    else
                    {
                        position = -1;
                    }

                    if (position != -1)
                    {
                        mHandleToMove.setPosition(position);

                        if (mEventsListener != null)
                        {
                            if (mHandleToMove.getId() == 0)
                            {
                                mEventsListener.onStartPositionChanged(position);
                            }
                            else if (mHandleToMove.getId() == 1)
                            {
                                mEventsListener.onEndPositionChanged(position);
                            }
                        }
                    }
                }
            }
            break;

            case MotionEvent.ACTION_UP:
            {
                mHandleToMove = null;
            }
            break;
        }

        invalidate();

        return true;
    }

    private int positionToX(int position)
    {
        int barWidth = mBarRect.width();

        return (int) ((position * barWidth / 100) + mBarRect.left);
    }

    private boolean pointInsideRect(int l, int r, int t, int b, int pointX, int pointY)
    {
        if (pointX > l && pointX < r && pointY > t && pointY < b)
        {
            return true;
        }

        return false;
    }

    private class Handle {
        int mPosition;

        int mId;

        public void setId(int id) {
            mId = id;
        }

        public int getId() {
            return mId;
        }

        public void setPosition(int position) {
            mPosition = position;
        }

        public int getPosition() {
            return mPosition;
        }
    }
}
