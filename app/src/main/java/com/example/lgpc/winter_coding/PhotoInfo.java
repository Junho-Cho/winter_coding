package com.example.lgpc.winter_coding;

import android.graphics.Bitmap;

/**
 * Created by LGPC on 2018-11-01.
 */

public class PhotoInfo {
    public Bitmap thumbnail;
    public Bitmap largeImage;

    public PhotoInfo(Bitmap thumbnail, Bitmap largeImage) {
        this.thumbnail = thumbnail;
        this.largeImage = largeImage;
    }
}
