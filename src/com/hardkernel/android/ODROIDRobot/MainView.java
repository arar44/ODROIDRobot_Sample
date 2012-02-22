package com.hardkernel.android.ODROIDRobot;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MainView extends View {
	int disWidth;
	int disHeight;
	int width;
	int height;
	int X_COUNT = 3;
	int Y_COUNT = 5;

	long elTime;

	private Paint mPaint;

	int m1;
	int m2;
	int id1;
	int id2;
	int sendm1;
	int sendm2;
	
	private Bitmap mBm;

	public MainView(Context context) {
		super(context);
		mPaint = new Paint();
		m1 = 0;
		m2 = 0;

		elTime = 0;
	}

	public MainView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint = new Paint();
		m1 = 0;
		m2 = 0;

		elTime = 0;
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		disWidth = w;
		disHeight = h;
		width = disWidth / X_COUNT;
		// height = disHeight/Y_COUNT;
		height = disHeight / Y_COUNT;
		
		Resources res = getContext().getResources();
		//mBm = BitmapFactory.decodeResource(res, R.drawable.speech);
		mBm = getResizedBitmap(BitmapFactory.decodeResource(res, R.drawable.speech_full),
				(int)(w/3), (int)(w/3));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Paint paint = new Paint();
		paint.setColor(Color.argb(255, 255, 255, 255));

		mPaint.setAntiAlias(true);
		
		canvas.drawLine(width, 0, width, disHeight, paint);
		canvas.drawLine(width * 2, 0, width * 2, disHeight, paint);

		for (int iy = 0; iy < Y_COUNT + 1; iy++) {
			canvas.drawLine(0, height * iy, width, height * iy, paint);
			canvas.drawLine(width * 2, height * iy, disWidth, height * iy,
					paint);
		}
		
		//fix by ê¹€ë�„ì§‘, K.E.L.P 
		canvas.drawLine(0, height * Y_COUNT - 1, width, height * Y_COUNT -1, paint);
		canvas.drawLine(width * 2, height * Y_COUNT - 1, disWidth, height * Y_COUNT -1, paint);

		Rect rect1 = new Rect(0, height * (m1 + 2), width, height
				* ((m1 + 2) + 1));
		canvas.drawRect(rect1, paint);

		Rect rect2 = new Rect(width * 2, height * (m2 + 2), disWidth, height
				* ((m2 + 2) + 1));
		canvas.drawRect(rect2, paint);
		
		canvas.drawBitmap(mBm, getWidth() / 2 - (mBm.getWidth() / 2), 
				getHeight() / 2 - (mBm.getHeight() / 2), paint);

	}

	public void SendCmd(int m1, int m2) {

		if (sendm1 != m1) {
			m1 *= -1;
			((ODROIDRobotActivity) getContext()).setMotorValue(2, m1 * 5);
			sendm1 = m1;
		}
		if (sendm2 != m2) {
			m2 *= -1;
			((ODROIDRobotActivity) getContext()).setMotorValue(1, m2 * 5);
			sendm2 = m2;
		}
	}
	
	public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bitmap
		matrix.postScale(scaleWidth, scaleHeight);
		// recreate the new Bitmap
		return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		long eTime = ev.getEventTime();
		int eAction = ev.getAction() & MotionEvent.ACTION_MASK;
		int eActionId = (ev.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
		int pointerCount = ev.getPointerCount();

		if (eAction == MotionEvent.ACTION_UP) {
			m1 = 0;
			m2 = 0;

			Rect voiceRect = new Rect(disWidth / 3 + 10, disHeight / 3 + 10,
					disWidth / 3 * 2 - 10, disHeight / 3 * 2 - 10);

			if (voiceRect.left < ev.getX() && voiceRect.right > ev.getX()) {
				((ODROIDRobotActivity) getContext()).VoiceSpeech();
			}

			invalidate();
		} else if (eAction == MotionEvent.ACTION_POINTER_UP) {

			if (eActionId == id1) {
				m1 = 0;
				invalidate();
			} else if (eActionId == id2) {
				m2 = 0;
				invalidate();
			}

		} else {
			for (int p = 0; p < pointerCount; p++) {
				int x = (int) ev.getX(p);
				int y = (int) ev.getY(p);

				if (x < width) {
					m1 = (y / height) - 2;
					if (m1 > 2) {
						m1 = 2;
					}
					id1 = p;
				} else if ((x > width) && (x < width * 2)) {

				} else if ((x > width * 2) && (x < disWidth)) {
					m2 = (y / height) - 2;
					if (m2 > 2) {
						m2 = 2;
					}
					id2 = p;
				}
			}
			invalidate();
		}
		
		if (ev.getAction() == MotionEvent.ACTION_MOVE) {
			if (Math.abs(elTime - eTime) > 20) {
				SendCmd(m1, m2);
			}
			elTime = eTime;
		} else {
			SendCmd(m1, m2);
		}
		return true;
	}
}