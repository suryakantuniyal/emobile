package drivers;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

public class EMSBambooImageLoader {
	private final int ALIGN_CENTER = 0, ALIGN_RIGHT = 1;
	private ArrayList<Byte> dataFrames;

	public ArrayList<ArrayList<Byte>> bambooDataWithAlignment(int align, Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		int xpos = 0;
		int new_width = 400;
		Canvas myCanvas = new Canvas();

		Bitmap newBitmap = Bitmap.createBitmap(new_width, height, Bitmap.Config.ARGB_8888);
		myCanvas.setBitmap(newBitmap);

		switch (align) {
		case ALIGN_CENTER: {
			xpos = (new_width / 2) - (width / 2);
			break;
		}
		case ALIGN_RIGHT: {
			xpos = new_width - width;
			break;
		}
		default: {
			xpos = 0;
			break;
		}
		}

		myCanvas.drawBitmap(this.convertToBlackAndWhite(bitmap), xpos, 0, null);

		// Bitmap scaled = Bitmap.createScaledBitmap(bitmap, dstWidth,
		// dstHeight, filter);
		width = new_width;
		List<Byte> list = new ArrayList<Byte>();

		int pixels[] = new int[width * height];
		newBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
		int pixelCount = 0;
		byte eightPixels = 0;

		for (int i = 0; i < pixels.length; i++) {
			byte channels[] = ByteBuffer.allocate(4).putInt(pixels[i]).array();
			int lum = (channels[1] + channels[2] + channels[3]) / 3;
			int alpha = channels[0];

			if (pixelCount > 7) {
				pixelCount = 0;
				list.add(eightPixels);
				eightPixels = 0;
			}

			if (((int) lum & 0xFF) < 128 && ((int) alpha & 0xFF) > 128) {
				eightPixels = (byte) (eightPixels | (1 << (7 - pixelCount)));
			}

			pixelCount++;
		}

		list.add(eightPixels);

		// dataFrames = new ArrayList<Byte>();
		ArrayList<ArrayList<Byte>> dataFramesList = new ArrayList<ArrayList<Byte>>();

		while (list.size() != 0) {
			dataFrames = new ArrayList<Byte>();
			int length = (width / 8) * 0x20;

			if (list.size() < length)
				length = list.size();

			List<Byte> pixelsList = list.subList(0, length);
			list = list.subList(length, list.size());
			Byte[] values = { 0x1B, 0x58, 0x31, 0x32, (byte) (length / 0x32) };
			List<Byte> header = (List<Byte>) (Arrays.asList(values));
			dataFrames.addAll(header);
			dataFrames.addAll(pixelsList);

			dataFramesList.add(dataFrames);
		}
		dataFramesList.get(0);

		bitmap.recycle();
		newBitmap.recycle();

		return dataFramesList;
	}

	private Bitmap convertToBlackAndWhite(Bitmap originalBitmap) {
		ColorMatrix colorMatrix = new ColorMatrix();
		colorMatrix.setSaturation(0);

		ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(colorMatrix);
		Bitmap filteredBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);

		Paint paint = new Paint();
		paint.setColorFilter(colorMatrixFilter);

		Canvas canvas = new Canvas(filteredBitmap);
		canvas.drawBitmap(filteredBitmap, 0, 0, paint);

		return filteredBitmap;
	}
}
