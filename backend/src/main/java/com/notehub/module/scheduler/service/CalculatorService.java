package com.notehub.module.scheduler.service;

import com.notehub.module.scheduler.model.Event;
import com.notehub.module.scheduler.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日期计算引擎
 * 根据事件的开关配置执行计算逻辑
 */
@Slf4j
@Service
public class CalculatorService {

    /**
     * 计算事件的相关天数信息，存入 Map
     * 针对开启的正计时和倒计时分别进行计算，判断是否需要发送通知。
     */
    public Map<String, Object> calculate(Event event) {
        Map<String, Object> result = new ConcurrentHashMap<>();
        boolean shouldNotify = false;
        StringBuilder reminderText = new StringBuilder();

        // 正计时逻辑 (Count Up)
        if (Boolean.TRUE.equals(event.getEnableCountUp())) {
            long daysPassed = DateUtil.daysBetween(event.getDate());
            result.put("daysPassed", daysPassed);
            
            // 正计时如果开启通知，默认按百天提醒
            if (Boolean.TRUE.equals(event.getEnableNotification()) && DateUtil.isHundredsMultiple(daysPassed)) {
                shouldNotify = true;
                reminderText.append(String.format("今天是我们【%s】的第 %d 天！💕\n", event.getName(), daysPassed));
            }
        }

        // 倒计时逻辑 (Countdown / Annual)
        if (Boolean.TRUE.equals(event.getEnableCountdown())) {
            long daysUntil;
            if (Boolean.TRUE.equals(event.getIsAnnual())) {
                daysUntil = DateUtil.daysUntilBirthday(event.getDate());
                int nextAge = daysUntil == 0 
                        ? DateUtil.calculateAge(event.getDate()) 
                        : DateUtil.calculateAge(event.getDate()) + 1;
                result.put("nextAge", nextAge);
            } else {
                daysUntil = DateUtil.daysUntilTarget(event.getDate());
            }
            result.put("daysUntil", daysUntil);

            if (Boolean.TRUE.equals(event.getEnableNotification())) {
                List<Integer> advanceDays = event.getNotifyAdvanceDays();
                if (advanceDays != null && advanceDays.contains((int) daysUntil)) {
                    shouldNotify = true;
                    if (daysUntil == 0) {
                        reminderText.append(String.format("今天就是【%s】啦！🎂\n", event.getName()));
                    } else {
                        reminderText.append(String.format("距离【%s】还有 %d 天，别忘了准备哦～🎁\n", event.getName(), daysUntil));
                    }
                }
            }
        }

        if (!shouldNotify) {
            return null;
        }

        result.put("shouldNotify", true);
        result.put("reminderText", reminderText.toString().trim());
        return result;
    }
}