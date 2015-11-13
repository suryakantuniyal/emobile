package com.honeywell.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.honeywell.imagingmanager.ImageConst;

public class ImagingView extends ImageView
  implements ImageConst
{
  private Bitmap mBitmap = null;

  private Bitmap mPreviewBitmap = null;

  private Bitmap mImageFileBitmap = null;

  private boolean isNeedshowBitmap = false;

  private boolean isPreviewing = false;

  private boolean isNeedShowImagefile = false;

  private boolean isNeedUpdateLayout = false;

  private int mOrginWidth = 0;

  private int mOriginHeight = 0;

  private int mScaleWidht = 0;

  private int mScaleHeight = 0;

  private int mMinWidth = 0;

  private int mMaxWidth = 0;

  private int mWidhtStep = 0;

  private int mHeightStep = 0;

  private int mZoomNum = 0;

  private Handler mHandler = null;

  public ImagingView(Context context) {
    super(context);
    setFocusable(true);

    this.mBitmap = Bitmap.createBitmap(832, 640, Bitmap.Config.RGB_565);

    this.mPreviewBitmap = Bitmap.createBitmap(208, 160, Bitmap.Config.ALPHA_8);
  }

  public ImagingView(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    setFocusable(true);

    this.mBitmap = Bitmap.createBitmap(832, 640, Bitmap.Config.RGB_565);

    this.mPreviewBitmap = Bitmap.createBitmap(208, 160, Bitmap.Config.ALPHA_8);
  }

  public ImagingView(Context context, AttributeSet attrs, int defStyle)
  {
    super(context);
    setFocusable(true);

    this.mBitmap = Bitmap.createBitmap(832, 640, Bitmap.Config.RGB_565);

    this.mPreviewBitmap = Bitmap.createBitmap(208, 160, Bitmap.Config.ALPHA_8);
  }

  private void initCaptureParametre()
  {
    this.mZoomNum = 0;
    this.mMinWidth = (getWidth() / 4);
    this.mMaxWidth = (getWidth() * 2);
    this.mWidhtStep = (getWidth() * 1000 / 40);
    this.mHeightStep = (getHeight() * 1000 / 40);
    this.mOrginWidth = (this.mScaleWidht = getWidth());
    this.mOriginHeight = (this.mScaleHeight = getHeight());
  }

  public void setShowBitmapflag(boolean ison)
  {
    if (ison) {
      initCaptureParametre();
    }
    this.isNeedshowBitmap = ison;
  }

  public boolean isNeedShowBitmap()
  {
    return this.isNeedshowBitmap;
  }

  public void setUpdateFlag(boolean isUpdate)
  {
    this.isNeedUpdateLayout = isUpdate;
  }

  public void setPreviewFlag(boolean ispreview)
  {
    if ((!this.isPreviewing) && (ispreview))
      initCaptureParametre();
    this.isPreviewing = ispreview;
  }

  public boolean getPreviewFlag()
  {
    return this.isPreviewing;
  }

  public Bitmap getCaptureBitmap()
  {
    if (this.mImageFileBitmap != null) {
      this.mImageFileBitmap.recycle();
      this.mImageFileBitmap = null;
      this.isNeedShowImagefile = false;
    }
    return this.mBitmap;
  }

  public void setBitmap(Bitmap bitmap) {
    this.mBitmap = bitmap;
  }

  public void setPreviewBitmap(Bitmap bitmap) {
    this.mPreviewBitmap = bitmap;
  }

  public void freePreviewBitmap() {
    if ((this.mPreviewBitmap != null) && (!this.mPreviewBitmap.isRecycled())) {
      this.mPreviewBitmap.recycle();
      this.mPreviewBitmap = null;
    }
  }

  public void creatPreviewBitmap() {
    if (this.mPreviewBitmap == null)
      this.mPreviewBitmap = Bitmap.createBitmap(208, 160, Bitmap.Config.ALPHA_8);
  }

  public Bitmap getPreviewBitmap()
  {
    if (this.mImageFileBitmap != null) {
      this.mImageFileBitmap.recycle();
      this.mImageFileBitmap = null;
    }
    return this.mPreviewBitmap;
  }

  public int getZoomPer() {
    if (this.mOrginWidth != 0)
      return this.mScaleWidht * 100 / this.mOrginWidth;
    return -1;
  }

  public String getImagedepth()
  {
    if (this.mImageFileBitmap != null) {
      return this.mImageFileBitmap.getConfig().name();
    }
    return this.mBitmap.getConfig().name();
  }

  public String getResolution()
  {
    if (this.mImageFileBitmap != null) {
      return this.mImageFileBitmap.getWidth() + "x" + this.mImageFileBitmap.getHeight();
    }
    return this.mBitmap.getWidth() + "x" + this.mBitmap.getHeight();
  }

  public boolean setBitmapByFN(String filename)
  {
    BitmapFactory.Options option = new BitmapFactory.Options();
    option.inSampleSize = 1;
    if (this.mImageFileBitmap != null)
      this.mImageFileBitmap.recycle();
    this.mImageFileBitmap = BitmapFactory.decodeFile(filename, option);
    if (this.mImageFileBitmap != null) {
      int w = this.mImageFileBitmap.getWidth() > getWidth() ? getWidth() : this.mImageFileBitmap.getWidth();

      int h = this.mImageFileBitmap.getHeight() > getHeight() ? getHeight() : this.mImageFileBitmap.getHeight();

      this.mMinWidth = (w / 4);
      this.mMaxWidth = (w * 2);
      this.mWidhtStep = (w * 1000 / 40);
      this.mHeightStep = (h * 1000 / 40);
      this.mOrginWidth = (this.mScaleWidht = w);
      this.mOriginHeight = (this.mScaleHeight = h);
      this.mZoomNum = 0;
      this.isNeedShowImagefile = true;
      setShowBitmapflag(false);
      return true;
    }

    return false;
  }

  public void setHandler(Handler handler)
  {
    this.mHandler = handler;
  }

  public void stop()
  {
    this.mBitmap.recycle();
    this.mBitmap = null;

    if (this.mImageFileBitmap != null) {
      this.mImageFileBitmap.recycle();
      this.mImageFileBitmap = null;
    }

    if (this.mPreviewBitmap != null) {
      this.mPreviewBitmap.recycle();
      this.mPreviewBitmap = null;
    }
    this.isPreviewing = false;
  }

  public void zoomCanvas(boolean iszoomout)
  {
    if (iszoomout) {
      if (this.mScaleWidht > this.mMinWidth)
      {
        this.mZoomNum -= 1;
        this.mScaleWidht = (this.mOrginWidth + this.mWidhtStep * this.mZoomNum / 1000);
        this.mScaleHeight = (this.mOriginHeight + this.mHeightStep * this.mZoomNum / 1000);
      }

    }
    else if (this.mScaleWidht < this.mMaxWidth) {
      this.mZoomNum += 1;
      this.mScaleWidht = (this.mOrginWidth + this.mWidhtStep * this.mZoomNum / 1000);
      this.mScaleHeight = (this.mOriginHeight + this.mHeightStep * this.mZoomNum / 1000);
    }

    invalidate();
    this.mHandler.sendEmptyMessage(4);
  }

  public boolean isZoomEnabled(boolean isZoomout)
  {
    boolean result = true;

    if ((!this.isNeedShowImagefile) && (!this.isNeedshowBitmap) && (!this.isPreviewing))
      return false;
    if (isZoomout) {
      if (this.mScaleWidht <= this.mMinWidth) {
        result = false;
      }
    }
    else if (this.mScaleWidht >= this.mMaxWidth) {
      result = false;
    }

    return result;
  }

  protected void onDraw(Canvas canvas)
  {
    if (this.isNeedUpdateLayout) {
      initCaptureParametre();
      this.isNeedUpdateLayout = false;
    }
    if ((this.isPreviewing) && (this.mPreviewBitmap != null) && (!this.mPreviewBitmap.isRecycled())) {
      Paint p = new Paint();
      p.setColor(-1);
      Rect mSrc = new Rect();
      mSrc.set(0, 0, this.mPreviewBitmap.getWidth(), this.mPreviewBitmap.getHeight());
      Rect mDst = new Rect();
      mDst.set(0, 0, this.mScaleWidht, this.mScaleHeight);
      canvas.drawBitmap(this.mPreviewBitmap, null, mDst, p);
    }
    else if (this.mImageFileBitmap != null) {
      this.isNeedshowBitmap = false;
      canvas.drawColor(-16776961);
      Rect mSrc = new Rect();
      mSrc.set(0, 0, this.mImageFileBitmap.getWidth(), this.mImageFileBitmap.getHeight());
      Rect mDst = new Rect();
      mDst.set(0, 0, this.mScaleWidht, this.mScaleHeight);
      canvas.drawBitmap(this.mImageFileBitmap, mSrc, mDst, null);
    } else if ((this.mBitmap != null) && 
      (this.isNeedshowBitmap)) {
      setBackgroundColor(-16777216);
      Rect mSrc = new Rect();
      canvas.drawColor(-16776961);
      mSrc.set(0, 0, this.mBitmap.getWidth(), this.mBitmap.getHeight());
      Rect mDst = new Rect();
      mDst.set(0, 0, this.mScaleWidht, this.mScaleHeight);
      canvas.drawBitmap(this.mBitmap, mSrc, mDst, null);
    }
  }
}