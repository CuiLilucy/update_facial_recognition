package com.example.myapp5;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class LoginVideoBackgroundUtils {
    private static MediaPlayer player;
    private static SurfaceHolder holder;

    public static void getPlayVideo(Context context, SurfaceView surfaceView, String videopath) {
        player = new MediaPlayer();
        try {
            player.setDataSource(context, Uri.parse(videopath));
            holder = surfaceView.getHolder();
            holder.addCallback(new SurfaceViewCallBack());
            player.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            player.prepare();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    player.start();
                    player.setLooping(true);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private static class SurfaceViewCallBack implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            player.setDisplay(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }
}
