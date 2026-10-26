package com.accommodation.bookingservice.notification;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TelegramNotificationService implements NotificationService {
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot%s/sendMessage";

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.chat.id}")
    private String chatId;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    public void sendNotification(String message) {
        if (botToken == null || botToken.isBlank()
                || botToken.contains("your_telegram_bot_token")
                || chatId == null || chatId.isBlank()
                || chatId.contains("your_chat_id")) {
            log.info("Telegram notification skipped (not configured): {}", message);
            return;
        }

        try {
            String encodedText = URLEncoder.encode(message, StandardCharsets.UTF_8);
            String url = String.format(TELEGRAM_API_URL, botToken)
                    + "?chat_id=" + chatId
                    + "&text=" + encodedText
                    + "&parse_mode=HTML";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("Failed to send Telegram notification, status: {}, response: {}",
                        response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.warn("Error while sending Telegram notification: {}", e.getMessage());
        }
    }
}
