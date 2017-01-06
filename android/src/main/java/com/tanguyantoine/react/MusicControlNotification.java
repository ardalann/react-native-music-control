package com.tanguyantoine.react;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.view.KeyEvent;
import com.facebook.react.bridge.ReactApplicationContext;

public class MusicControlNotification {

    protected static final String REMOVE_NOTIFICATION = "remove_notification";

    private final ReactApplicationContext context;

    private int smallIcon;
    private NotificationCompat.Action play, pause, stop, next, previous;

    public static final String 	ACTION_PLAY		=	"com.tanguyantoine.react.ACTION_PLAY";
    public static final String 	ACTION_PAUSE	=	"com.tanguyantoine.react.ACTION_PAUSE";
    public static final String 	ACTION_PREVIOUS	=	"com.tanguyantoine.react.ACTION_PREVIOUS";
    public static final String 	ACTION_NEXT		=	"com.tanguyantoine.react.ACTION_NEXT";
    public static final String 	ACTION_STOP		=	"com.tanguyantoine.react.ACTION_STOP";
    public static final String 	ACTION_SERVICE_INIT =	"com.tanguyantoine.react.ACTION_SERVICE_INIT";
    public static final String  ACTION_PLAYMODE_UPDATE = "com.tanguyantoine.react.ACTION_PLAYMODE_UPDATE";

    public MusicControlNotification(ReactApplicationContext context) {
        this.context = context;

        Resources r = context.getResources();
        String packageName = context.getPackageName();
        smallIcon = r.getIdentifier("play", "drawable", packageName);
    }

    public void updateActions(long mask) {
        play = createAction("play", "Play", mask, ACTION_PLAY, play);
        pause = createAction("pause", "Pause", mask, ACTION_PAUSE, pause);
        stop = createAction("stop", "Stop", mask, ACTION_STOP, stop);
        next = createAction("next", "Next", mask, ACTION_NEXT, next);
        previous = createAction("previous", "Previous", mask, ACTION_PREVIOUS, previous);
    }

    public void show(NotificationCompat.Builder builder, boolean isPlaying) {

        // Add the buttons
        builder.mActions.clear();
        if(previous != null) builder.addAction(previous);
        if(play != null && !isPlaying) builder.addAction(play);
        if(pause != null && isPlaying) builder.addAction(pause);
        //if(stop != null) builder.addAction(stop);
        if(next != null) builder.addAction(next);

        builder.setOngoing(isPlaying);
        builder.setSmallIcon(smallIcon);

        // Open the app when the notification is clicked
        Intent openApp = new Intent(context, getMainActivityClass());
        openApp.setAction(Intent.ACTION_MAIN);
        openApp.addCategory(Intent.CATEGORY_LAUNCHER);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, openApp, 0));

        if(!isPlaying) {
            // Remove notification
            Intent remove = new Intent(REMOVE_NOTIFICATION);
            builder.setDeleteIntent(PendingIntent.getBroadcast(context, 0, remove, 0));
        }

        NotificationManagerCompat.from(context).notify("MusicControl", 0, builder.build());
    }

    public void hide() {
        NotificationManagerCompat.from(context).cancel("MusicControl", 0);
    }

    private NotificationCompat.Action createAction(String iconName, String title, long mask, String action, NotificationCompat.Action oldAction) {
        //if((mask & action) == 0) return null;
        if(oldAction != null) return oldAction;

        Resources r = context.getResources();
        String packageName = context.getPackageName();
        int icon = r.getIdentifier(iconName, "drawable", packageName);

        // Replace this to MediaButtonReceiver.buildMediaButtonPendingIntent when React Native updates the support library
        int keyCode = 8686;//toKeyCode(action);
        //Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        Intent intent = new Intent(action);
        //intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
        PendingIntent i = PendingIntent.getBroadcast(context, keyCode, intent, 0);

        return new NotificationCompat.Action(icon, title, i);
    }

    public static class NotificationService extends Service {
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            return START_NOT_STICKY;
        }

        @Override
        public void onTaskRemoved(Intent rootIntent) {
            if(MusicControlModule.INSTANCE != null) {
                MusicControlModule.INSTANCE.destroy();
            }
            stopSelf();
        }

    }

    public Class getMainActivityClass() {
        String packageName = context.getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        String className = launchIntent.getComponent().getClassName();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}
