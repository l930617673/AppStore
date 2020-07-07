package top.lhjjjlxays.appstore.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.request.target.ImageViewTarget;

/**
 * @author lhj
 * @version 1.0
 * @date 2020/5/19 16:14
 * @description
 */
public class GlideSizeTransformUtil extends ImageViewTarget<Bitmap> {

    private Context context;
    private ImageView target;

    public GlideSizeTransformUtil(ImageView target, Context context) {
        super(target);
        this.target = target;
        this.context = context;
    }

    @Override
    protected void setResource(Bitmap resource) {

        if (resource == null) return;

        view.setImageBitmap(resource);

        //获取原图的宽高
        int width = resource.getWidth();
        int height = resource.getHeight();

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        assert wm != null;
        wm.getDefaultDisplay().getMetrics(dm);
        int owidth = dm.widthPixels;

        ViewGroup.LayoutParams params = target.getLayoutParams();

        if (width < height) { //2.5
            int tw = (int) (owidth / 2.5);

            params.width = tw;
            params.height = (int) ((tw * 1.0) / (width * 1.0) * height);
        } else {  //1.8
            int th = (int) (owidth / 1.8);

            params.height = th;
            params.width = (int) ((th * 1.0) / (height * 1.0) * width);
        }

        target.setLayoutParams(params);
    }
}