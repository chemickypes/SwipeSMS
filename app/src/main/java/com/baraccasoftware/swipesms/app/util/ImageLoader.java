package com.baraccasoftware.swipesms.app.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.baraccasoftware.swipesms.app.R;
import com.baraccasoftware.swipesms.app.object.Conversation;

import java.lang.ref.WeakReference;

/**
 * Created by angelo on 31/03/14.
 */
public class ImageLoader {
    private Context mContext;
    private LruCache<Integer, Bitmap> mCache;


    public ImageLoader(Context context){
        this.mContext = context;
        setCache();
    }
    
    public void loadBitimap(Conversation conversation,ImageView imageView){
        Bitmap b = getImgFromMemCache(conversation.get_id());
        if(b != null ){
            imageView.setImageBitmap(b);
        }else if (cancelPotentialWork(conversation, imageView)) {
            final ImageLoaderTask task = new ImageLoaderTask(imageView);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(mContext.getResources(), BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_person), task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(conversation);
        }
        
    }

    public void addImgToMemoryCache(Integer key, Bitmap uri) {
        if (getImgFromMemCache(key) == null) {
            mCache.put(key, uri);
        }
    }

    public Bitmap getImgFromMemCache(Integer key) {
        return mCache.get(key);
    }

    public static boolean cancelPotentialWork(Conversation data, ImageView imageView) {
        final ImageLoaderTask bitmapWorkerTask = getImageLoaderTask(imageView);

        try{
            if (bitmapWorkerTask != null) {
                final Conversation bitmapData = bitmapWorkerTask.conv;
                if (bitmapData.get_id() != data.get_id()) {
                    // Cancel previous task
                    bitmapWorkerTask.cancel(true);
                } else {
                    // The same work is already in progress
                    return false;
                }
            }
        }catch (NullPointerException e){
            bitmapWorkerTask.cancel(true);
            return true;
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private void setCache(){
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 16;

        mCache = new LruCache<Integer, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Integer key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    private Bitmap roundCornerImage(Bitmap src, float round) {
        // Source image size
        int width = src.getWidth();
        int height = src.getHeight();
        Log.i("Imageloader", "img widht: "+width+" height: "+height);
        // create result bitmap output
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // set canvas for painting
        Canvas canvas = new Canvas(result);
        canvas.drawARGB(0, 0, 0, 0);

        // configure paint
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);

        // configure rectangle for embedding
        final Rect rect = new Rect(0, 0, width, height);
        final RectF rectF = new RectF(rect);

        // draw Round rectangle to canvas
        canvas.drawRoundRect(rectF, round, round, paint);

        // create Xfer mode
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        // draw source image to canvas
        canvas.drawBitmap(src, rect, rect, paint);

        // return final image
        return result;
    }

    private static ImageLoaderTask getImageLoaderTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<ImageLoaderTask> bitmapImageLoaderTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             ImageLoaderTask bitmapImageLoaderTask) {
            super(res, bitmap);
            bitmapImageLoaderTaskReference =
                    new WeakReference<ImageLoaderTask>(bitmapImageLoaderTask);
        }

        public ImageLoaderTask getBitmapWorkerTask() {
            return bitmapImageLoaderTaskReference.get();
        }
    }

    class ImageLoaderTask extends AsyncTask<Conversation,Void,Bitmap>{
        ImageView imgview;
        Conversation conv;
        private final WeakReference<ImageView> imageViewReference;

        public ImageLoaderTask(ImageView view){
            imgview = view;
            this.imageViewReference = new WeakReference<ImageView>(imgview);
        }
        @Override
        protected Bitmap doInBackground(Conversation... conv) {
            this.conv = conv[0];

            // mettere la ricerca
            Bitmap bitmap = SwipeSMSProvider.getPhotoContactFromConversation(mContext,this.conv); //= getPhotoContactFromConversation(mContext,this.conv);
            if( bitmap != null) addImgToMemoryCache(this.conv.get_id(),bitmap);

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final ImageLoaderTask imageLoaderTask = getImageLoaderTask(imageView);
                if (imageLoaderTask != null && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
}
