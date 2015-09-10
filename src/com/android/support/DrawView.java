package com.android.support;


import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class DrawView extends View implements OnTouchListener {
	private Canvas mCanvas;
	private Path mPath;
	private Paint mPaint;
	private ArrayList<Path> paths = new ArrayList<Path>();
	private boolean moved = false;
	private List<Points> listPoints = new ArrayList<Points>();
	private Points point = new Points();
	private Bitmap canvasBitmap = null;

	FileOutputStream fos = null;

	public DrawView(Context context,int width,int height) {
		super(context);
		setFocusable(true);
		setFocusableInTouchMode(true);
		setDrawingCacheEnabled(true);
		this.setOnTouchListener(this);


		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(Color.BLACK);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(4);
		canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas();

		mCanvas.setBitmap(canvasBitmap);
		// mCanvas.drawColor(Color.WHITE);
		mPath = new Path();
		paths.add(mPath);

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		for (Path p : paths) {
			canvas.drawPath(p, mPaint);
		}
		if (listPoints.size() > 0) {
			int size = listPoints.size();
			for (int i = 0; i < size; i++) {
				canvas.drawPoint(listPoints.get(i).x, listPoints.get(i).y, mPaint);
			}

		}
		// mCanvas = canvas;
		// canvas.setBitmap(canvasBitmap);
	}

	private float mX, mY;
	private static final float TOUCH_TOLERANCE = 4;

	private void touch_start(float x, float y) {
		mPath.reset();
		mPath.moveTo(x, y);
		mX = x;
		mY = y;
	}

	private void touch_move(float x, float y) {
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
			mX = x;
			mY = y;
		}
	}

	private void touch_up(float x, float y) {

		// commit the path to our offscreen

		// moved = false;
		if (moved) {
			mPath.lineTo(mX, mY);
			mCanvas.drawPath(mPath, mPaint);
			moved = false;
		} else {
			point.x = x;
			point.y = y;
			listPoints.add(point);

			if (listPoints.size() > 0) {
				int size = listPoints.size();
				for (int i = 0; i < size; i++) {
					mCanvas.drawPoint(listPoints.get(i).x, listPoints.get(i).y, mPaint);
				}

			}

			point = new Points();
		}
		// kill this so we don't double draw
		mPath = new Path();
		paths.add(mPath);
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touch_start(x, y);
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			moved = true;
			touch_move(x, y);
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			touch_up(x, y);
			invalidate();
			break;
		}
		return true;
	}

	public Bitmap getCanvasBitmap() {
		
		//Saving transparent background to white
		Bitmap newBitmap = Bitmap.createBitmap(380, 100,Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(newBitmap);
		canvas.drawColor(Color.WHITE);
		canvas.drawBitmap(Bitmap.createScaledBitmap(canvasBitmap, 380, 100, false), 0, 0, null);
		
		return newBitmap;
		//return Bitmap.createScaledBitmap(canvasBitmap, 380, 100, false);
	}

}
