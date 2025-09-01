package com.iitp.domains.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.iitp.domains.notification.dto.FcmDto;
import com.iitp.domains.notification.dto.FcmDto.FcmMessage;
import com.iitp.domains.notification.dto.FcmDto.Message;
import com.iitp.domains.notification.dto.NotifyParams;
import com.iitp.global.exception.ExceptionMessage;
import com.iitp.global.exception.RefoodException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final ObjectMapper objectMapper;
    private final JSONParser jsonParser;

    private static final String FCM_PRIVATE_KEY_PATH = "refood-firebase-private-key.json";
    private static final String fireBaseScope = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String PROJECT_ID_URL = "https://fcm.googleapis.com/v1/projects/refood-42f17/messages:send";

    @Async(value = "AsyncBean")
    public CompletableFuture<Boolean> sendPushMessage(String fcmToken, NotifyParams params) {
        String message = makeMessage(fcmToken, params);
        System.out.println("message = " + message);

        String accessToken = getAccessToken();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(PROJECT_ID_URL)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json; UTF-8")
                .post(RequestBody.create(message, MediaType.parse("application/json; charset=urf-8")))
                .build();
        try (Response response = client.newCall(request).execute()) {
            System.out.println("========================ERROR==============");
            System.out.println("response = " + response);
            if (!response.isSuccessful() && response.body() != null) {
                JSONObject responseBody = (JSONObject) jsonParser.parse(response.body().string());
                String errorMessage = ((JSONObject) responseBody.get("error")).get("message").toString();
                log.warn("FCM [sendPushMessage] okHttp response is not OK : {}", errorMessage);
                return CompletableFuture.completedFuture(false);
            }
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            log.warn("FCM [sendPushMessage] I/O Exception : {}", e.getMessage());
            throw new RefoodException(ExceptionMessage.SEND_FCM_PUSH_ERROR);
        }
    }

    public String makeMessage(String targetToken, NotifyParams params) {
        try {
            FcmMessage fcmMessage = new FcmMessage(
                    false,
                    new Message(
                            targetToken,
                            new FcmDto.Notification(
                                    params.title(),
                                    params.content()
                            )
                    )
            );
            System.out.println("fcmMessage = " + fcmMessage);
            return objectMapper.writeValueAsString(fcmMessage);
        } catch (JsonProcessingException e) {
            log.warn("FCM [makeMessage] Error : {}", e.getMessage());
            throw new RefoodException(ExceptionMessage.FCM_MESSAGE_JSON_PARSING_ERROR);
        }
    }


    private String getAccessToken() {
        try {
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(new ClassPathResource(FCM_PRIVATE_KEY_PATH).getInputStream())
                    .createScoped(List.of(fireBaseScope));
            credentials.refreshIfExpired();
            return credentials.getAccessToken().getTokenValue();
        } catch (IOException e) {
            log.warn("FCM getAccessToken Error : {}", e.getMessage());
            throw new RefoodException(ExceptionMessage.GET_FCM_ACCESS_TOKEN_ERROR);
        }
    }
}
