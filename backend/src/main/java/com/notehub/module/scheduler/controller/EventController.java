package com.notehub.module.scheduler.controller;

import com.notehub.module.scheduler.model.Event;
import com.notehub.module.scheduler.util.DateUtil;
import com.notehub.module.scheduler.util.JsonUtil;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * 动态事件 REST API
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final JsonUtil jsonUtil;

    public EventController(JsonUtil jsonUtil) {
        this.jsonUtil = jsonUtil;
    }

    /**
     * 保存所有事件
     */
    @PostMapping
    public List<Map<String, Object>> saveEvents(@RequestBody List<Map<String, Object>> events) {
        for (Map<String, Object> event : events) {
            Object id = event.get("id");
            if (id == null || id.toString().isBlank()) {
                event.put("id", UUID.randomUUID().toString());
            }
            normalizeEventDefaults(event);
        }
        jsonUtil.saveEventMaps(events);
        return events;
    }

    /**
     * 获取所有原始事件（用于编辑）
     */
    @GetMapping("/raw")
    public List<Map<String, Object>> listRawEvents() {
        return jsonUtil.loadEventMaps();
    }

    private void normalizeEventDefaults(Map<String, Object> event) {
        Object title = event.get("title");
        Object name = event.get("name");
        if ((name == null || name.toString().isBlank()) && title != null && !title.toString().isBlank()) {
            event.put("name", title.toString());
        }
        event.remove("title");

        event.putIfAbsent("category", "other");
        event.putIfAbsent("tags", Collections.emptyList());
        event.putIfAbsent("color", "#ec4899");
        event.putIfAbsent("pinned", false);
        event.putIfAbsent("show_in_timeline", false);
        event.putIfAbsent("story", "");
        event.putIfAbsent("location", "");
        event.putIfAbsent("mood", "");
        event.putIfAbsent("photos", Collections.emptyList());
        event.putIfAbsent("notify_advance_days", Collections.emptyList());
    }

    /**
     * 获取所有事件（附带计算信息，用于首页卡片展示）
     */
    @GetMapping
    public List<Map<String, Object>> listEvents() {
        List<Event> events = jsonUtil.loadEvents();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Event event : events) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", event.getId());
            item.put("name", event.getName());
            item.put("date", event.getDate().toString());
            item.put("isAnnual", event.getIsAnnual());
            item.put("enableCountUp", event.getEnableCountUp());
            item.put("enableCountdown", event.getEnableCountdown());
            item.put("category", event.getCategory());
            item.put("tags", event.getTags());
            item.put("color", event.getColor());
            item.put("pinned", event.getPinned());
            item.put("showInTimeline", event.getShowInTimeline());
            item.put("story", event.getStory());
            item.put("location", event.getLocation());
            item.put("mood", event.getMood());
            item.put("photos", event.getPhotos());

            // 计算正计时
            if (Boolean.TRUE.equals(event.getEnableCountUp())) {
                long daysPassed = DateUtil.daysBetween(event.getDate());
                item.put("daysPassed", daysPassed);
                int nextHundred = (int) (Math.ceil(daysPassed / 100.0) * 100);
                if (nextHundred == daysPassed) nextHundred += 100; // 如果刚好是100天，下一个是200天
                item.put("daysToNextHundred", nextHundred - daysPassed);
            }

            // 计算倒计时
            if (Boolean.TRUE.equals(event.getEnableCountdown())) {
                long daysUntil;
                if (Boolean.TRUE.equals(event.getIsAnnual())) {
                    daysUntil = DateUtil.daysUntilBirthday(event.getDate());
                    int nextAge = daysUntil == 0
                            ? DateUtil.calculateAge(event.getDate())
                            : DateUtil.calculateAge(event.getDate()) + 1;
                    item.put("nextAge", nextAge);
                } else {
                    daysUntil = DateUtil.daysUntilTarget(event.getDate());
                }
                item.put("daysUntil", daysUntil);
            }

            result.add(item);
        }

        return result;
    }

    /**
     * 获取日历标记数据（返回所有需要在日历上标记的日期）
     * 支持返回月份数据和跨年的纪念日
     */
    @GetMapping("/calendar")
    public List<Map<String, Object>> calendarMarks(@RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year) {
        List<Event> events = jsonUtil.loadEvents();
        List<Map<String, Object>> marks = new ArrayList<>();

        for (Event event : events) {
            if (Boolean.TRUE.equals(event.getIsAnnual())) {
                // 每年重复的事件（如生日、结婚纪念日），在日历上标记今年的日期
                Map<String, Object> mark = new LinkedHashMap<>();
                mark.put("id", event.getId() + "_annual");
                mark.put("name", event.getName());
                mark.put("isAnnual", true);
                
                LocalDate thisYearDate = DateUtil.safeMonthDay(event.getDate()).atYear(year);
                mark.put("date", thisYearDate.toString());
                mark.put("month", thisYearDate.getMonthValue());
                marks.add(mark);
            } else {
                // 非重复事件，如果是当年发生，则标记当年
                if (event.getDate().getYear() == year) {
                    Map<String, Object> mark = new LinkedHashMap<>();
                    mark.put("id", event.getId() + "_once");
                    mark.put("name", event.getName());
                    mark.put("isAnnual", false);
                    mark.put("date", event.getDate().toString());
                    mark.put("month", event.getDate().getMonthValue());
                    marks.add(mark);
                }
            }

            // 如果开启了正计时，还要在日历上标记百天纪念日
            if (Boolean.TRUE.equals(event.getEnableCountUp())) {
                long currentDaysPassed = DateUtil.daysBetween(event.getDate());
                int startHundred = (int) (currentDaysPassed / 100) * 100;
                if (startHundred == 0) startHundred = 100;
                
                // 找到接下来一年内的百天纪念日
                for (int i = startHundred; i <= startHundred + 1000; i += 100) {
                    LocalDate milestoneDate = event.getDate().plusDays(i - 1);
                    if (milestoneDate.getYear() == year) {
                        Map<String, Object> mark = new LinkedHashMap<>();
                        mark.put("id", event.getId() + "_milestone_" + i);
                        mark.put("name", event.getName() + " " + i + "天");
                        mark.put("date", milestoneDate.toString());
                        mark.put("month", milestoneDate.getMonthValue());
                        marks.add(mark);
                    }
                }
            }
        }

        return marks;
    }
}
