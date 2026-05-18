package com.notehub.module.scheduler.task;

import com.notehub.module.scheduler.model.Event;
import com.notehub.module.scheduler.service.CalculatorService;
import com.notehub.module.scheduler.service.MailService;
import com.notehub.module.scheduler.service.MailTemplateService;
import com.notehub.module.scheduler.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulerTask {

    private final JsonUtil jsonUtil;
    private final CalculatorService calculatorService;
    private final MailTemplateService mailTemplateService;
    private final MailService mailService;

    @Scheduled(cron = "${scheduler.cron}")
    public void dailyCheck() {
        log.info("========== 开始每日纪念日检查 ==========");
        List<Event> events = jsonUtil.loadEvents();

        for (Event event : events) {
            try {
                Map<String, Object> result = calculatorService.calculate(event);
                if (result == null) {
                    continue;
                }

                String subject = "💕 " + event.getName() + " - " + result.get("reminderText");
                String html = mailTemplateService.render(event.getName(), result);
                boolean sent = mailService.sendHtml(subject, html);

                if (sent) {
                    log.info("✅ 已发送提醒: {} -> {}", event.getName(), result.get("reminderText"));
                }
            } catch (Exception e) {
                log.error("处理事件失败: {}, 错误: {}", event.getName(), e.getMessage(), e);
            }
        }

        log.info("========== 每日检查完成 ==========");
    }
}
