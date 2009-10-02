/*
	ConnectBot: simple, powerful, open-source SSH client for Android
	Copyright (C) 2007-2008 Kenny Root, Jeffrey Sharkey

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.connectbot.util;

import java.util.Vector;

import org.connectbot.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class EntropyView extends View {
	private static final int SHA1_MAX_BYTES = 20;
	private static final int MILLIS_BETWEEN_INPUTS = 50;

	private Paint mPaint;
	private FontMetrics mFontMetrics;
	private boolean mFlipFlop;
	private long mLastTime;
	private Vector<OnEntropyGatheredListener> listeners;

	private byte[] mEntropy;
	private int mEntropyByteIndex;
	private int mEntropyBitIndex;

	private int splitText = 0;

	private float lastX = 0.0f, lastY = 0.0f;

	public EntropyView(Context context) {
		super(context);

		setUpEntropy();
	}

	public EntropyView(Context context, AttributeSet attrs) {
		super(context, attrs);

		setUpEntropy();
	}

	private void setUpEntropy() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setTypeface(Typeface.DEFAULT);
		mPaint.setTextAlign(Paint.Align.CENTER);
		mPaint.setTextSize(16);
		mPaint.setColor(Color.WHITE);
		mFontMetrics = mPaint.getFontMetrics();

		mEntropy = new byte[SHA1_MAX_BYTES];
		mEntropyByteIndex = 0;
		mEntropyBitIndex = 0;

		listeners = new Vector<OnEntropyGatheredListener>();
	}

	public void addOnEntropyGatheredListener(OnEntropyGatheredListener listener) {
		listeners.add(listener);
	}

	public void removeOnEntropyGatheredListener(OnEntropyGatheredListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void onDraw(Canvas c) {
		String prompt = String.format(getResources().getString(R.string.pubkey_touch_prompt),
			(int)(100.0 * (mEntropyByteIndex / 20.0)) + (int)(5.0 * (mEntropyBitIndex / 8.0)));
		if (splitText > 0 ||
				mPaint.measureText(prompt) > (getWidth() * 0.8)) {
			if (splitText == 0)
				splitText = prompt.indexOf(" ", prompt.length() / 2);

			c.drawText(prompt.substring(0, splitText),
					getWidth() / 2.0f,
					getHeight() / 2.0f + (mPaint.ascent() + mPaint.descent()),
					mPaint);
			c.drawText(prompt.substring(splitText),
					getWidth() / 2.0f,
					getHeight() / 2.0f - (mPaint.ascent() + mPaint.descent()),
					mPaint);
		} else {
			c.drawText(prompt,
					getWidth() / 2.0f,
					getHeight() / 2.0f - (mFontMetrics.ascent + mFontMetrics.descent) / 2,
					mPaint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mEntropyByteIndex >= SHA1_MAX_BYTES
				|| lastX == event.getX()
				|| lastY == event.getY())
			return true;

		// Only get entropy every 200 milliseconds to ensure the user has moved around.
		long now = System.currentTimeMillis();
		if ((now - mLastTime) < MILLIS_BETWEEN_INPUTS)
				return true;
		else
				mLastTime = now;

		byte input;

		lastX = event.getX();
		lastY = event.getY();

		// Get the lowest 4 bits of each X, Y input and concat to the entropy-gathering
		// string.
		if (mFlipFlop)
				input = (byte)((((int)lastX & 0x0F) << 4) | ((int)lastY & 0x0F));
		else
				input = (byte)((((int)lastY & 0x0F) << 4) | ((int)lastX & 0x0F));
		mFlipFlop = !mFlipFlop;

		for (int i = 0; i < 4 && mEntropyByteIndex < SHA1_MAX_BYTES; i++) {
			if ((input & 0x3) == 0x1) {
				mEntropy[mEntropyByteIndex] <<= 1;
				mEntropy[mEntropyByteIndex] |= 1;
				mEntropyBitIndex++;
				input >>= 2;
			} else if ((input & 0x3) == 0x2) {
				mEntropy[mEntropyByteIndex] <<= 1;
				mEntropyBitIndex++;
				input >>= 2;
			}

			if (mEntropyBitIndex >= 8) {
				mEntropyBitIndex = 0;
				mEntropyByteIndex++;
			}
		}

		// SHA1PRNG only keeps 160 bits of entropy.
		if (mEntropyByteIndex >= SHA1_MAX_BYTES) {
			for (OnEntropyGatheredListener listener: listeners) {
				listener.onEntropyGathered(mEntropy);
			}
		}

		invalidate();

		return true;
	}
}
