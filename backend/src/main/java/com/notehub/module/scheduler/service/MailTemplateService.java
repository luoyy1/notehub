package com.notehub.module.scheduler.service;

import org.springframework.stereotype.Service;
import java.util.Map;

/**
 * HTML 邮件模板生成服务
 * 生成通用动态邮件模板
 */
@Service
public class MailTemplateService {

    public String render(String eventName, Map<String, Object> data) {
        String text = (String) data.get("reminderText");
        StringBuilder highlights = new StringBuilder();

        if (data.containsKey("daysPassed")) {
            long daysPassed = (long) data.get("daysPassed");
            highlights.append("""
                  <div style="display:inline-block;background:linear-gradient(135deg,#fce7f3,#fdf2f8);border-radius:16px;padding:20px 40px;margin:10px;">
                    <span style="font-size:48px;font-weight:800;color:#ec4899;">%d</span>
                    <span style="font-size:18px;color:#be185d;display:block;margin-top:4px;">天 (已发生)</span>
                  </div>
            """.formatted(daysPassed));
        }

        if (data.containsKey("daysUntil")) {
            long daysUntil = (long) data.get("daysUntil");
            String highlightText = daysUntil == 0 ? "今天！" : daysUntil + " 天后";
            highlights.append("""
                  <div style="display:inline-block;background:linear-gradient(135deg,#fef3c7,#fffbeb);border-radius:16px;padding:20px 40px;margin:10px;">
                    <span style="font-size:48px;font-weight:800;color:#f97316;">%s</span>
                    <span style="font-size:18px;color:#c2410c;display:block;margin-top:4px;">(倒计时)</span>
                  </div>
            """.formatted(highlightText));
        }

        return """
        <!DOCTYPE html>
        <html lang="zh-CN">
        <head><meta charset="UTF-8"></head>
        <body style="margin:0;padding:0;background:#fdf2f8;font-family:'PingFang SC','Microsoft YaHei',sans-serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="background:#fdf2f8;padding:40px 0;">
            <tr><td align="center">
              <table width="460" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:20px;box-shadow:0 4px 24px rgba(236,72,153,0.12);overflow:hidden;">
                <!-- 顶部渐变条 -->
                <tr><td style="height:6px;background:linear-gradient(90deg,#ec4899,#f97316);"></td></tr>
                <!-- 图标 -->
                <tr><td align="center" style="padding:32px 0 8px 0;">
                  <span style="font-size:56px;">✨</span>
                </td></tr>
                <!-- 标题 -->
                <tr><td align="center" style="padding:0 30px;">
                  <h1 style="margin:0;font-size:22px;color:#be185d;">%s</h1>
                </td></tr>
                <!-- 高亮模块 -->
                <tr><td align="center" style="padding:20px 10px;">
                  %s
                </td></tr>
                <!-- 正文 -->
                <tr><td align="center" style="padding:8px 30px 12px;">
                  <p style="font-size:16px;color:#6b7280;line-height:1.8;margin:0;">%s</p>
                </td></tr>
                <!-- 底部装饰 -->
                <tr><td align="center" style="padding:16px 0 28px;">
                  <span style="font-size:13px;color:#d1d5db;">—— NoteHub · 我们的小世界 ——</span>
                </td></tr>
              </table>
            </td></tr>
          </table>
        </body>
        </html>
        """.formatted(eventName, highlights.toString(), text);
    }
}
