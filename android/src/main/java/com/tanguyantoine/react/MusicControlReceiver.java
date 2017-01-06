package com.tanguyantoine.react;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.KeyEvent;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class MusicControlReceiver extends BroadcastReceiver {

    private static final String TAG = "MediaControlService";

    private final MusicControlNotification notification;
    private final MediaSessionCompat session;
    private final ReactApplicationContext reactContext;

    public MusicControlReceiver(MusicControlNotification notification, MediaSessionCompat session, ReactApplicationContext context) {
        this.notification = notification;
        this.session = session;
        this.reactContext = context;
    }

    /* It should be safe to use static variables here once registered via the AudioManager */
    private static long mHeadsetDownTime = 0;
    private static long mHeadsetUpTime = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Action: " + action);

        if(action.equals(MusicControlNotification.REMOVE_NOTIFICATION)) {
            notification.hide();
            session.setActive(false);
        }

        /*
         * Remote / headset control events
         */
        if (action.equalsIgnoreCase(MusicControlNotification.ACTION_PLAY)) {
            sendEvent(reactContext, "play", null);
        } else if (action.equalsIgnoreCase(MusicControlNotification.ACTION_PAUSE)) {
            sendEvent(reactContext, "pause", null);
        } else if (action.equalsIgnoreCase(MusicControlNotification.ACTION_PREVIOUS)) {
            sendEvent(reactContext, "previousTrack", null);
        } else if (action.equalsIgnoreCase(MusicControlNotification.ACTION_NEXT)) {
            sendEvent(reactContext, "nextTrack", null);
        } else if (action.equalsIgnoreCase(MusicControlNotification.ACTION_STOP)) {
            sendEvent(reactContext, "stop", null);
        }
    }

    private static void sendEvent(ReactApplicationContext context, String type, Double value) {
        WritableMap data = Arguments.createMap();
        data.putString("name", type);
        if(value != null) data.putDouble("value", value);

        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("RNMusicControlEvent", data);
    }

}
