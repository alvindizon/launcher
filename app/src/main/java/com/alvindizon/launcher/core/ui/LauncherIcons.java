/*
 * Copyright (C) 2016 The Android Open Source Project
 * Modifications Copyright (C) 2020 Alvin Dizon for launcher <https://github.com/alvindizon>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alvindizon.launcher.core.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;


public class LauncherIcons {

    private static final Rect sOldBounds = new Rect();
    private static final Canvas sCanvas = new Canvas();
    public static final float BLUR_FACTOR = 0.5f/48; // taken from ShadowGenerator.java

    /**
     * Returns a bitmap which is of the appropriate size to be displayed as an icon
     * @param icon The source Drawable to be converted to a Bitmap
     * @param context Context from View
     * @param scale the scale to apply before drawing {@param icon} on the canvas
     * @return a Bitmap that is optimized for displaying
     */
    public static Bitmap createIconBitmap(Drawable icon, Context context, float scale) {
        synchronized (sCanvas) {
            InvariantDeviceProfile idp = new InvariantDeviceProfile(context);
            final int iconBitmapSize = idp.iconBitmapSize;
            int width = iconBitmapSize;
            int height = iconBitmapSize;

            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap != null && bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }

            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();
            if (sourceWidth > 0 && sourceHeight > 0) {
                // Scale the icon proportionally to the icon dimensions
                final float ratio = (float) sourceWidth / sourceHeight;
                if (sourceWidth > sourceHeight) {
                    height = (int) (width / ratio);
                } else if (sourceHeight > sourceWidth) {
                    width = (int) (height * ratio);
                }
            }
            // no intrinsic size --> use default size
            int textureWidth = iconBitmapSize;
            int textureHeight = iconBitmapSize;

            Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            final int left = (textureWidth-width) / 2;
            final int top = (textureHeight-height) / 2;

            sOldBounds.set(icon.getBounds());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && icon instanceof AdaptiveIconDrawable) {
                int offset = Math.max((int)(BLUR_FACTOR * iconBitmapSize),
                        Math.min(left, top));
                int size = Math.max(width, height);
                icon.setBounds(offset, offset, size, size);
            } else {
                icon.setBounds(left, top, left+width, top+height);
            }
            canvas.save();
            canvas.scale(scale, scale, textureWidth / 2, textureHeight / 2);
            icon.draw(canvas);
            canvas.restore();
            icon.setBounds(sOldBounds);
            canvas.setBitmap(null);

            return bitmap;
        }
    }

    /**
     * Various utility methods from Utilities.java
     */
    public static float dpiFromPx(int size, DisplayMetrics metrics){
        float densityRatio = (float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT;
        return (size / densityRatio);
    }

    public static int pxFromDp(float size, DisplayMetrics metrics) {
        return (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                size, metrics));
    }
}
