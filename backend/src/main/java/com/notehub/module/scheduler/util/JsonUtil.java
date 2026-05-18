package com.notehub.module.scheduler.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.notehub.module.scheduler.model.Event;
import com.notehub.module.scheduler.model.Wish;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON 文件读写工具
 * 负责从物理文件读取和保存事件列表
 */
@Component
public class JsonUtil {

    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);

    private final ObjectMapper objectMapper;

    @Value("${scheduler.event-data-path:data/events.json}")
    private String eventDataPath;

    @Value("${scheduler.wish-data-path:data/wishes.json}")
    private String wishDataPath;

    public JsonUtil() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * 读取 events.json
     */
    public List<Event> loadEvents() {
        File file = new File(eventDataPath);
        if (!file.exists()) {
            log.warn("事件数据文件不存在: {}，返回空列表", file.getAbsolutePath());
            return Collections.emptyList();
        }
        
        try {
            List<Event> events = objectMapper.readValue(file, new TypeReference<List<Event>>() {});
            log.info("成功加载 {} 条事件", events.size());
            return events;
        } catch (IOException e) {
            log.error("读取 {} 失败: {}", eventDataPath, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 按原始 JSON 结构读取 events.json，供编辑器保留扩展字段。
     */
    public List<Map<String, Object>> loadEventMaps() {
        File file = new File(eventDataPath);
        if (!file.exists()) {
            log.warn("事件数据文件不存在: {}，返回空列表", file.getAbsolutePath());
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(file, new TypeReference<List<Map<String, Object>>>() {});
        } catch (IOException e) {
            log.error("读取 {} 失败: {}", eventDataPath, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 写入 events.json
     */
    public void saveEvents(List<Event> events) {
        writeJson(new File(eventDataPath), events, "纪念日");
    }

    /**
     * 按原始 JSON 结构写入 events.json，避免编辑扩展字段时被模型裁剪。
     */
    public void saveEventMaps(List<Map<String, Object>> events) {
        writeJson(new File(eventDataPath), events, "纪念日");
    }

    /**
     * 读取 wishes.json
     */
    public List<Wish> loadWishes() {
        File file = new File(wishDataPath);
        if (!file.exists()) {
            log.warn("愿望数据文件不存在: {}，返回空列表", file.getAbsolutePath());
            return Collections.emptyList();
        }

        try {
            List<Wish> wishes = objectMapper.readValue(file, new TypeReference<List<Wish>>() {});
            log.info("成功加载 {} 条愿望", wishes.size());
            return wishes;
        } catch (IOException e) {
            log.error("读取 {} 失败: {}", wishDataPath, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 写入 wishes.json
     */
    public void saveWishes(List<Wish> wishes) {
        writeJson(new File(wishDataPath), wishes, "愿望");
    }

    private void writeJson(File file, Object data, String label) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        
        try {
            objectMapper.writeValue(file, data);
            log.info("成功保存{}数据到 {}", label, file.getAbsolutePath());
        } catch (IOException e) {
            log.error("保存{}失败: {}", label, e.getMessage(), e);
            throw new RuntimeException("无法保存" + label + "数据", e);
        }
    }
}
