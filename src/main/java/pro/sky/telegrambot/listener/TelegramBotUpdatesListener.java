package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private NotificationTaskRepository notificationTaskRepository;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            var chatId = update.message().chat().id();
            // Process your updates here
            if (update.message().text().equals("/start")){
                String messageText = "Hello here!";
                sendMessage(messageText, chatId);
            }
            Pattern pattern = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");
            Matcher matcher = pattern.matcher(update.message().text());
            if (matcher.matches()){
                NotificationTask newTask = new NotificationTask();
                String dateTime = matcher.group(1);
                String noification = matcher.group(3);
                newTask.setDateTime(LocalDateTime.parse( dateTime, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
                newTask.setNotification(noification);
                newTask.setChatId(chatId);
                notificationTaskRepository.save(newTask);
                String messageText = "Task created" + newTask.getDateTime() + " chatId: " + newTask.getChatId();
                sendMessage(messageText, chatId);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void sendMessage(String messageText, Long chatId) {
        SendMessage message = new SendMessage(chatId, messageText);
        SendResponse response = telegramBot.execute(message);
        System.out.println(response);
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void sheduledTask() {
        var now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        for (var task :notificationTaskRepository.findAllByDateTime(now)){
            System.out.println("In time "+now+" send to "+task.getChatId()+" mes "+ task.getNotification());
            sendMessage(task.getNotification(),task.getChatId());
        }
    }
}
