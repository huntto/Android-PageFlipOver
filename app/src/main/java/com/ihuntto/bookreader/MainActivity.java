package com.ihuntto.bookreader;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.ihuntto.bookreader.flip.FlipOver;
import com.ihuntto.bookreader.flip.FlipOverPage;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private FlipOver mFlipOver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFlipOver = findViewById(R.id.view_pager_flip_over);
        mFlipOver.setOnPageFlipListener(mOnPageFlipListener);
        mFlipOver.setPageProvider(mPageProvider);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFlipOver.setOnPageFlipListener(null);
        mFlipOver.setPageProvider(null);
    }

    private FlipOver.OnPageFlipListener mOnPageFlipListener = new FlipOver.OnPageFlipListener() {
        @Override
        public void onFlipLeft() {

        }

        @Override
        public void onFlipRight() {

        }

        @Override
        public void onPageClick() {

        }
    };

    private FlipOver.PageProvider mPageProvider = new FlipOver.PageProvider() {
        private int[] mBitmapIds = new int[]{
                R.mipmap.one,
                R.mipmap.two,
                R.mipmap.three
        };

        private Bitmap[] mBitmaps = new Bitmap[mBitmapIds.length];

        @Override
        public int getPageCount() {
            return mBitmaps.length;
        }

        @Override
        public FlipOverPage updatePage(int index, int width, int height) {
            Bitmap leftPageBitmap = loadBitmap(index - 1, width, height);
            Bitmap currentPageBitmap = loadBitmap(index, width, height);
            Bitmap rightPageBitmap = loadBitmap(index + 1, width, height);
            return new FlipOverPage(leftPageBitmap, currentPageBitmap, rightPageBitmap);
        }


        private Bitmap loadBitmap(final int index, final int width, final int height) {
            if (index < 0 || index > mBitmaps.length - 1 || width <= 0 || height <= 0) {
                Log.e(TAG, "loadBitmap wrong params: index=" + index
                        + " width=" + width
                        + " height=" + height);
                return null;
            }

            Bitmap cachedBitmap = mBitmaps[index];
            if (cachedBitmap != null
                    && cachedBitmap.getWidth() == width
                    && cachedBitmap.getHeight() == height) {
                Log.i(TAG, "use cached bitmap");
                return cachedBitmap;
            }


            cachedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            cachedBitmap.eraseColor(Color.WHITE);
            Canvas canvas = new Canvas(cachedBitmap);
            Drawable drawable = getResources().getDrawable(mBitmapIds[index]);

            int margin = 7;
            int border = 3;
            Rect dirtyRect = new Rect(margin, margin, width - margin, height - margin);

            int imageWidth = dirtyRect.width() - (border * 2);
            int imageHeight = imageWidth * drawable.getIntrinsicHeight()
                    / drawable.getIntrinsicWidth();
            if (imageHeight > dirtyRect.height() - (border * 2)) {
                imageHeight = dirtyRect.height() - (border * 2);
                imageWidth = imageHeight * drawable.getIntrinsicWidth()
                        / drawable.getIntrinsicHeight();
            }

            dirtyRect.left += ((dirtyRect.width() - imageWidth) / 2) - border;
            dirtyRect.right = dirtyRect.left + imageWidth + border + border;
            dirtyRect.top += ((dirtyRect.height() - imageHeight) / 2) - border;
            dirtyRect.bottom = dirtyRect.top + imageHeight + border + border;

            Paint p = new Paint();
            p.setColor(0xFFC0C0C0);
            canvas.drawRect(dirtyRect, p);
            dirtyRect.left += border;
            dirtyRect.right -= border;
            dirtyRect.top += border;
            dirtyRect.bottom -= border;

            drawable.setBounds(dirtyRect);
            drawable.draw(canvas);

            mBitmaps[index] = cachedBitmap;

            return cachedBitmap;
        }
    };
}
