package com.star4droid.star2d.ui;

/**
 * Single source of truth for the community / support links, shared by the home screen,
 * the support page and the periodic support dialog.
 */
public final class CommunityLinks {
    private CommunityLinks() {}

    /** Optimism (OP Mainnet) USDT wallet used for donations. */
    public static final String USDT_OPTIMISM_ADDRESS = "";
    public static final String DEV_WHATSAPP = "";
    public static final String DEV_TELEGRAM = "";

    /** Set to true once the Telegram community link is available. */
    public static final boolean TELEGRAM_COMMUNITY_ENABLED = false;

    /** {id, English title, English description, url, emoji}. */
    public static String[][] all() {
        // Modified for Lo3bati: the original author's community links, wallet and contacts were removed (list is empty until Lo3bati links are added).
        java.util.ArrayList<String[]> l = new java.util.ArrayList<>();
        return l.toArray(new String[0][]);
    }
}
