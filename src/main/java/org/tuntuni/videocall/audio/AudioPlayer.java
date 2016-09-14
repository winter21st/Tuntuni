/*
 * Copyright 2016 Tuntuni.
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
package org.tuntuni.videocall.audio;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import org.tuntuni.connection.DataFrame;
import org.tuntuni.connection.StreamServer;
import org.tuntuni.models.Logs;
import org.tuntuni.videocall.VideoFormat;

/**
 *
 * @author Sudipto Chandra
 */
public class AudioPlayer extends StreamServer {

    private DataLine.Info mSourceInfo;
    private SourceDataLine mSourceLine;

    public AudioPlayer(int port) {
        super(port);
    }

    /**
     * Starts the player
     */ 
    public void start() {
        try { 
            // start source line
            mSourceInfo = new DataLine.Info(
                    SourceDataLine.class, VideoFormat.getAudioFormat());
            mSourceLine = (SourceDataLine) AudioSystem.getLine(mSourceInfo);
            mSourceLine.open(VideoFormat.getAudioFormat());
            mSourceLine.start();
        } catch (LineUnavailableException ex) {
            Logs.error(getClass(), "Failed to start the audio line. ERROR: {0}", ex);
        }
    }

    /**
     * Stops the player
     */ 
    public void stop() {
        try { 
            // close player
            mSourceLine.stop();
            mSourceLine.close();
        } catch (Exception ex) {
        }
    }
 
    @Override
    public String getName() {
        return "AudioPlayer";
    }

    @Override
    public synchronized void dataReceived(Object data) {
        if (data != null && data instanceof DataFrame) {
            DataFrame frame = (DataFrame) data;
            byte[] play = frame.getBuffer();
            // play the audio data   
            mSourceLine.write(play, 0, play.length);
        }
    }

}