// package com.game.server.net;

// import com.game.server.ws.GameWsHandler;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.socket.config.annotation.EnableWebSocket;
// import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
// import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

// @Configuration
// @EnableWebSocket
// public class WebSocketConfig implements WebSocketConfigurer {

//   private final GameWsHandler gameWsHandler;

//   public WebSocketConfig(GameWsHandler gameWsHandler) {
//     this.gameWsHandler = gameWsHandler;
//   }

//   @Override
//   public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//     registry.addHandler(gameWsHandler, "/ws")
//             .setAllowedOrigins("*"); // allow localhost dev
//   }
// }
