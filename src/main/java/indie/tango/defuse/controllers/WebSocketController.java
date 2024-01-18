package indie.tango.defuse.controllers;

import indie.tango.defuse.models.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private int messageCounter = 0;

    @MessageMapping("/sendMessage")
    @SendToUser("/topic/chat")
    public Message sendMessage(Message message, SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("sendMessage");

        try {
            // Затримка на 3 секунди
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Підраховуємо кількість повідомлень
        messageCounter++;

        // Додаємо номер повідомлення до вмісту
        String updatedContent = message.getContent() + " (Message #" + messageCounter + ")";
        message.setContent(updatedContent);

        // Отримати унікальний ідентифікатор користувача з сесії
        String sessionId = headerAccessor.getSessionId();

        // Встановити ідентифікатор відправника в об'єкті Message
        message.setSender(sessionId);

        return message;
    }
}
