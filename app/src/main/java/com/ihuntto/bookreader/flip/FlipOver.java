package com.ihuntto.bookreader.flip;

public interface FlipOver {

    public interface PageProvider {
        int getPageCount();

        FlipOverPage updatePage(int index, int width, int height);
    }

    public interface OnPageFlipListener {
        void onFlipLeft();

        void onFlipRight();

        void onPageClick();
    }

    public void setPageProvider(PageProvider pageProvider);

    public void setOnPageFlipListener(OnPageFlipListener onPageFlipListener);
}
