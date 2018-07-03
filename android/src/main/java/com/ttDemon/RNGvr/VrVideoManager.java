package com.mlb.RNGvr;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.google.vr.sdk.widgets.common.VrWidgetView;
import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * VrVideoManager.java
 *
 * Created by Pietralberto Mazza on 22/06/17.
 * Copyright © 2017 Facebook. All rights reserved.
 *
 */

public class VrVideoManager extends SimpleViewManager<VrVideoView> {
    private static final String CLASS_NAME = "VrVideo";
    private static final String TAG = VrVideoManager.class.getSimpleName();
    private RCTEventEmitter mEventEmitter;

    private VrVideoView view;

    public VrVideoManager(ReactApplicationContext context) { super(); }

    public enum Events {
        EVENT_LOAD("onLoadSuccess"),
        EVENT_ERROR("onLoadError"),
        EVENT_CHANGE_DISPLAY("onVideoChangeDisplay");

        private final String mName;

        Events(final String name) {
            mName = name;
        }

        @Override
        public String toString() {
            return mName;
        }
    }

    @Override
    @javax.annotation.Nullable
    public Map getExportedCustomDirectEventTypeConstants() {
        MapBuilder.Builder builder = MapBuilder.builder();
        for (Events event : Events.values()) {
            builder.put(event.toString(), MapBuilder.of("registrationName", event.toString()));
        }
        return builder.build();
    }

    public void sendEvent(String eventName, @Nullable WritableMap event) {
        mEventEmitter.receiveEvent(view.getId(), eventName, event);
    }

    @Override
    public String getName() {
        return CLASS_NAME;
    }

    @Override
    protected VrVideoView createViewInstance(ThemedReactContext context) {
        view = new VrVideoView(context.getCurrentActivity());
        mEventEmitter = context.getJSModule(RCTEventEmitter.class);
        view.setEventListener(new ActivityEventListener());

        return view;
    }

    @Override
    public void onDropViewInstance(VrVideoView view) {
        super.onDropViewInstance(view);
        view.pauseVideo();
        view = null;
    }

    @ReactProp(name = "displayMode")
    public void setDisplayMode(VrVideoView view, String mode) {
        switch(mode) {
            case "fullscreen":
                view.setDisplayMode(VrWidgetView.DisplayMode.FULLSCREEN_MONO);
                break;
            case "cardboard":
                view.setDisplayMode(VrWidgetView.DisplayMode.FULLSCREEN_STEREO);
                break;
            case "embedded":
            default:
                view.setDisplayMode(VrWidgetView.DisplayMode.EMBEDDED);
                break;
        }
    }

    @ReactProp(name = "seekTo", defaultFloat = 0)
    public void setSeekTo(VrVideoView view, float seekTo){
        view.seekTo((long)seekTo);
    }

    @ReactProp(name = "volume")
    public void setVolume(VrVideoView view, float value) {
        view.setVolume(value);
    }

    @ReactProp(name = "paused", defaultBoolean = false)
    public void setPaused(final VrVideoView view, boolean paused){
        if(paused){
            view.pauseVideo();
        } else {
            view.playVideo();
        }
    }

    @ReactProp(name = "enableFullscreenButton")
    public void setFullscreenButtonEnabled(VrVideoView view, Boolean enabled) {
        view.setFullscreenButtonEnabled(enabled);
    }

    @ReactProp(name = "enableCardboardButton")
    public void setCardboardButtonEnabled(VrVideoView view, Boolean enabled) {
        view.setStereoModeButtonEnabled(enabled);
    }

    @ReactProp(name = "enableTouchTracking")
    public void setTouchTrackingEnabled(VrVideoView view, Boolean enabled) {
        view.setTouchTrackingEnabled(enabled);
    }

    @ReactProp(name = "hidesTransitionView")
    public void setTransitionViewEnabled(VrVideoView view, Boolean enabled) {
        view.setTransitionViewEnabled(!enabled);
    }

    @ReactProp(name = "enableInfoButton")
    public void setInfoButtonEnabled(VrVideoView view, Boolean enabled) {
        view.setInfoButtonEnabled(enabled);
    }

    @ReactProp(name = "source")
    public void setsource(VrVideoView view, ReadableMap source) {
        String type = source.getString("type");
        String format = source.getString("format");
        String uri = source.getString("uri");

        VrVideoView.Options videoOptions = new VrVideoView.Options();

        switch(format) {
            case "dash":
                videoOptions.inputFormat = VrVideoView.Options.FORMAT_DASH;
                break;
            case "hls":
                videoOptions.inputFormat = VrVideoView.Options.FORMAT_HLS;
                break;
            case "standard":
            default:
                videoOptions.inputFormat = VrVideoView.Options.FORMAT_DEFAULT;
                break;
        }

        switch(type) {
            case "stereo":
                videoOptions.inputType = VrVideoView.Options.TYPE_STEREO_OVER_UNDER;
                break;
            case "mono":
            default:
                videoOptions.inputType = VrVideoView.Options.TYPE_MONO;
                break;
        }

        Source _source = new Source(uri, videoOptions);
        VideoLoaderTask videoLoaderTask = new VideoLoaderTask();
        videoLoaderTask.execute(_source);
    }

    private class ActivityEventListener extends VrVideoEventListener {
        @Override
        public void onDisplayModeChanged(int newDisplayMode) {
            super.onDisplayModeChanged(newDisplayMode);
            Log.i(TAG, "Successfully change display " + newDisplayMode);
            WritableMap event = Arguments.createMap();
            String displayMode;

            if (VrWidgetView.DisplayMode.FULLSCREEN_MONO == newDisplayMode) {
                displayMode = "fullscreen";
            } else if (VrWidgetView.DisplayMode.FULLSCREEN_STEREO == newDisplayMode) {
                displayMode = "cardboard";
            } else {
                displayMode = "embedded";
            }

            event.putString("event", Events.EVENT_CHANGE_DISPLAY.toString());
            event.putString("displayMode", displayMode);

            sendEvent(Events.EVENT_CHANGE_DISPLAY.toString(), event);
        }

        @Override
        public void onLoadSuccess() {
            Log.i(TAG, "Successfully loaded video " + view.getDuration());
            WritableMap event = Arguments.createMap();

            event.putString("event", Events.EVENT_LOAD.toString());
            event.putDouble("duration", view.getDuration());

            sendEvent(Events.EVENT_LOAD.toString(), event);
        }
        /**
         * Called by video widget on the UI thread on any asynchronous error.
         */
        @Override
        public void onLoadError(String errorMessage) {
            // An error here is normally due to being unable to decode the video format.
            Log.e(TAG, "Error loading video: " + errorMessage);
            WritableMap event = Arguments.createMap();

            event.putString("event", Events.EVENT_ERROR.toString());
            event.putString("message", errorMessage);

            sendEvent(Events.EVENT_ERROR.toString(), event);
        }

        /**
         * Update the UI every frame.
         */
        @Override
        public void onNewFrame() {

        }

        /**
         * Make the video play in a loop. This method could also be used to move to the next video in
         * a playlist.
         */
        @Override
        public void onCompletion() {
            if(view != null) view.seekTo(0);
        }
    }

    class Source {
        public String uri;
        public VrVideoView.Options options;

        public Source(String uri, VrVideoView.Options videoOptions) {
            this.uri = uri;
            this.options = videoOptions;
        }
    }

    class VideoLoaderTask extends AsyncTask<Source, Void, Boolean> {
        @SuppressWarnings("WrongThread")
        protected Boolean doInBackground(Source... args) {
            try {
                Uri uri = Uri.parse(args[0].uri);
                view.loadVideo(uri, args[0].options);
            } catch (IOException e) {
                WritableMap event = Arguments.createMap();

                event.putString("event", Events.EVENT_ERROR.toString());
                event.putString("message", e.getMessage());

                sendEvent(Events.EVENT_ERROR.toString(), event);
            }

            return true;
        }
    }
}

