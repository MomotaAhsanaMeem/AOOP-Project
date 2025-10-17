package com.algoarena.client.audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

public final class Bgm {
    private static MediaPlayer mp;
    private Bgm() {}

    public static void ensureStarted() {
        if (mp != null) return;
        String url = Bgm.class.getResource("/audio/game_bgm.mp3").toExternalForm();
        Media media = new Media(url);
        mp = new MediaPlayer(media);
        mp.setCycleCount(MediaPlayer.INDEFINITE);
        mp.setVolume(0.30);
        mp.play();
    }

    public static void setVolume(double v) {
        if (mp != null) mp.setVolume(v);
    }

    public static void fadeOutStop(double seconds) {
        if (mp == null) return;
        MediaPlayer player = mp;
        mp = null;

        Timeline t = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(player.volumeProperty(), player.getVolume())),
                new KeyFrame(Duration.seconds(seconds), new KeyValue(player.volumeProperty(), 0))
        );
        t.setOnFinished(e -> { player.stop(); player.dispose(); });
        t.play();
    }
}
