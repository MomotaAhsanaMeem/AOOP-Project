package com.algoarena.client.net;

public class AppContext {
    private static NetworkClient NET;
    private static String playerName = "Player";

    public static NetworkClient net() {
        if (NET == null) NET = new NetworkClient();
        return NET;
    }

    public static void setPlayerName(String name) { playerName = name; }
    public static String getPlayerName() { return playerName; }
}
