package com.video.controls.video.player;

/**
 * Player to user actions
 */
public interface ControllerEventListener {
    public static int SHOW_PROGRESS = 0;
    public static int PLACE_HOLDER_VISIBILITY = 1;
    public static int RESET_CLICK = 2;
    /**
     * Player specific error if occured like due to network or any other error
     * data: is ExoPlaybackException
     */
    public static int PLAYER_ERROR = 3;
    //Share click event on player controller
    public static int SHARE = 4;
    public static int SETTING = 5;
    public static int FULLSCREEN = 6;
    public static int BACK_ARROW = 7;
    public static int NEXT_BUTTON = 8;
    public static int PREVIOUS_BUTTON = 9;
    /**
     * If some network error or other error occured resume with retry
     */
    public static int RETRY_VIDEO = 10;


    /**
     * GA Events
     *
     * @param eventName
     * @param value
     */

    public static int VIDEO_PLAY_START = 11;
    public static int VIDEO_PLAY_COMPLETE = 12;


    void onEvent(int eventName, Object value);
}
