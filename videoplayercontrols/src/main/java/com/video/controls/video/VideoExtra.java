package com.video.controls.video;

/**
 * Created by Gajendra on 10/04/17
 */

public class VideoExtra {

    /**
     * Video Detail with related videos activities extra keys
     */
    public interface VIDEO_DETAIL {
        /**
         * List of video items
         * Used in: player's next and previous click
         */
        String CHANNEL_ITEMS = "channel_items";
        /**
         * Compulsory extra should contains detail to play video
         */
        String CHANNEL_ITEM = "channel_item";
        /**
         * true if launching from deeplink else false(or don't pass)
         */
        String FROM_DEEPLINK = "isFromDeeplink";
        /**
         * true if launching from widget else false(or don't pass)
         */
        String FROM_WIDGET = "isFromWidget";
        /**
         * Analytic string for launching screen
         * Used in: Analytic parent screen
         */
        String ACTIONBAR_NAME = "actionbar_name";

        /**
         * Item position for the item
         * Used in: Analytic for sending position
         */
        String ITEM_POS = "item_pos";

        /**
         * View Type of the item
         * Used in: Analytic for sending position
         */
        String VIEW_TYPE = "view_type";
    }

}
