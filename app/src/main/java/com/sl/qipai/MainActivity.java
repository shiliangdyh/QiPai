package com.sl.qipai;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.sl.qipai.App;
import com.sl.qipai.NotificationBean;
import com.sl.qipai.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;

import static android.app.PendingIntent.FLAG_ONE_SHOT;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private NotificationBean notificationBean;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            loadLogo();
            return true;
        }
    });

    private NotificationManager notificationManager;

    private Bitmap logoBitmap;
    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_main);

//        NotificationUtil mNotificationUtils = new NotificationUtil(this);
//        mNotificationUtils.showNotification();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


//        addNotification();


        BmobQuery<NotificationBean> notificationBeanBmobQuery = new BmobQuery<>();
        notificationBeanBmobQuery.findObjects(new FindListener<NotificationBean>() {
            @Override
            public void done(List<NotificationBean> list, BmobException e) {
                Log.d(TAG, "done: ");
                if (list != null && !list.isEmpty()) {
                    notificationBean = list.get(0);
                    Log.d(TAG, "done: " + notificationBean.toString());
                    handler.sendEmptyMessageDelayed(0, notificationBean.getDelayTime());
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    private void addNotification() {
        NotificationBean notificationBean = new NotificationBean();
        notificationBean.setLogoUrl("http://www.kupan123.com/upload/1590151889x-1404755431.png");
        notificationBean.setAppName("今日头条");
        notificationBean.setTitle("这是主标题");
        notificationBean.setDelayTime(3000);
        notificationBean.setJumpUrl("https://www.baidu.com");
        notificationBean.setContent("国庆七天乐，10号内每天都可以申请彩金。棋牌平台可以下7码，流水非常容易打满，玩家稳赚不赔的活动！");
        notificationBean.setImageUrl("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1589971630701&di=1ef150e8da170562dcca301a22893fae&imgtype=0&src=http%3A%2F%2Fpic1.win4000.com%2Fwallpaper%2F2017-10-30%2F59f68513a33ca.jpg");

        notificationBean.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if (e == null) {
                    LogUtil.INSTANCE.d(TAG, "done: ");
                } else {
                    LogUtil.INSTANCE.d(TAG, "error: " + e.getLocalizedMessage());
                }
            }
        });
    }

    private void loadLogo() {
        LogUtil.INSTANCE.d(TAG, "loadLogo: ");
        if (TextUtils.isEmpty(notificationBean.getLogoUrl())){
            loadImage();
            return;
        }
        Glide.with(this.getApplicationContext()).asBitmap().load(notificationBean.getLogoUrl()).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                MainActivity.this.logoBitmap = resource;
                LogUtil.INSTANCE.d(TAG, "onResourceReady: " + resource);
                loadImage();
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                LogUtil.INSTANCE.d(TAG, "onLoadFailed: ");
                loadImage();
            }

        });
    }

    private void loadImage() {
        if (TextUtils.isEmpty(notificationBean.getImageUrl())){
            showNotification();
            return;
        }
        Glide.with(this.getApplicationContext()).asBitmap().load(notificationBean.getImageUrl()).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                MainActivity.this.imageBitmap = resource;
                LogUtil.INSTANCE.d(TAG, "onResourceReady: " + resource);
                showNotification();
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                LogUtil.INSTANCE.d(TAG, "onLoadFailed: ");
                showNotification();

            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(NotificationBean event) {
        LogUtil.INSTANCE.d(TAG, "onEventMainThread: ");
//        flg = !flg;
//        notificationManager.cancel(NOTIFICATION_ID);
        showNotification();
    }

    boolean flg = true;

    /**
     * 展示通知栏
     */
    public void showNotification() {

        String id = "channel_demo";
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(id, this.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
            mChannel.setDescription("通知栏");
            mChannel.enableLights(false);
            mChannel.setLightColor(Color.BLUE);
            mChannel.enableVibration(false);
            mChannel.setVibrationPattern(new long[]{0});
            notificationManager.createNotificationChannel(mChannel);
            notification = new NotificationCompat.Builder(this, id)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(getDefaultIntent(Notification.FLAG_ONGOING_EVENT))
                    .setCustomBigContentView(getContentView(true))
                    .setCustomContentView(getContentView(true))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setChannelId(mChannel.getId())
                    .build();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification = new NotificationCompat.Builder(this, id)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(getDefaultIntent(Notification.FLAG_ONGOING_EVENT))
                    .setCustomBigContentView(getContentView(true))
                    .setCustomContentView(getContentView(false))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build();
        } else {
            notification = new NotificationCompat.Builder(this, id)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(getDefaultIntent(Notification.FLAG_ONGOING_EVENT))
                    .setContent(getContentView(false))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build();
        }


        notificationManager.notify(NOTIFICATION_ID, notification);
    }

//    /**
//     * 展示通知栏
//     */
//    public void showNotification(){
//        Log.d(TAG, "showNotification: ");
//        String id = "channel_demo";
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel mChannel = new NotificationChannel(id, getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW);
//            mChannel.setDescription("通知栏");
//            mChannel.enableLights(false);
//            mChannel.setLightColor(Color.BLUE);
//            mChannel.enableVibration(true);
//            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
//            notificationManager.createNotificationChannel(mChannel);
//            notification = new NotificationCompat.Builder(this, id)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setWhen(System.currentTimeMillis())
//                    .setContentIntent(getDefaultIntent(Notification.FLAG_ONGOING_EVENT))
//                    .setCustomBigContentView(getContentView(true))
//                    .setCustomContentView(getContentView(true))
//                    .setPriority(NotificationCompat.PRIORITY_HIGH)
//                    .setChannelId(mChannel.getId())
//                    .build();
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            notification = new NotificationCompat.Builder(this, id)
//                    .setWhen(System.currentTimeMillis())
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setContentIntent(getDefaultIntent(Notification.FLAG_ONGOING_EVENT))
//                    .setCustomBigContentView(getContentView(true))
//                    .setCustomContentView(getContentView(true))
//                    .setPriority(NotificationCompat.PRIORITY_HIGH)
//                    .setOngoing(true)
//                    .build();
//        } else {
//            notification = new NotificationCompat.Builder(this, id)
//                    .setWhen(System.currentTimeMillis())
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setContentIntent(getDefaultIntent(Notification.FLAG_ONGOING_EVENT))
//                    .setContent(getContentView(true))
//                    .setPriority(NotificationCompat.PRIORITY_HIGH)
//                    .setOngoing(true)
//                    .build();
//        }
//
//
//        notificationManager.notify(NOTIFICATION_ID, notification);
//    }

    /**
     * 获取自定义通知栏view
     *
     * @param showBigView
     * @return
     */
    private RemoteViews getContentView(boolean showBigView) {
        int layout = -1;
        Log.d(TAG, "getContentView: " + flg);
        if (imageBitmap != null) {
            layout = R.layout.view_notify_big;
        } else {
            layout = R.layout.view_notify_big2;
        }
        RemoteViews mRemoteViews = new RemoteViews(getPackageName(), layout);
        if (logoBitmap != null) {
            mRemoteViews.setImageViewBitmap(R.id.iv_logo, logoBitmap);
        }
        mRemoteViews.setTextViewText(R.id.content, notificationBean.getContent());
        mRemoteViews.setTextViewText(R.id.tv_title, notificationBean.getAppName());
        mRemoteViews.setTextViewText(R.id.title, notificationBean.getTitle());
        mRemoteViews.setOnClickPendingIntent(R.id.rootview, getClickPendingIntent());

        if (imageBitmap != null) {
            mRemoteViews.setImageViewBitmap(R.id.custom_song_icon, imageBitmap);
        }
        NotificationCompatColor.AutomationUse(this)
                .setContentTitleColor(mRemoteViews, R.id.title)
                .setContentTitleSize(mRemoteViews, R.id.title)
                .setContentTextSize(mRemoteViews, R.id.content)
                .setContentTextColor(mRemoteViews, R.id.content)
                .setTitleColor(mRemoteViews, R.id.tv_title)
                .setTitleSize(mRemoteViews, R.id.tv_title)

        ;
        return mRemoteViews;
    }

    private PendingIntent getClickPendingIntent() {
//        Intent intent = new Intent(this, MyBroatCast.class);
//        intent.setAction("notification_card");
//        PendingIntent pendingIntentClick0 = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        return pendingIntentClick0;
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(notificationBean.getJumpUrl());
        intent.setData(content_url);
        PendingIntent pendingIntentClick0 = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntentClick0;
    }

    private PendingIntent getDefaultIntent(int flags) {
        return PendingIntent.getActivity(this, 1, new Intent(), flags);
    }

    public static final int NOTIFICATION_ID = 10003;

    public static class MyBroatCast extends BroadcastReceiver {

        public MyBroatCast() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: ");
            EventBus.getDefault().post(new NotificationBean());

        }
    }
}


