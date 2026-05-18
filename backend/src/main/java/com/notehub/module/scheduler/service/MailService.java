package com.notehub.module.scheduler.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 邮件发送服务
 * 封装 Spring Boot Mail，发送 HTML 格式邮件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${scheduler.notify-email}")
    private String notifyEmail;

    /**
     * 发送 HTML 邮件
     *
     * @param subject 邮件主题
     * @param htmlContent HTML 正文
     */
    public boolean sendHtml(String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(notifyEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);  // true = HTML

            mailSender.send(message);
            log.info("邮件发送成功 → 主题: {}", subject);
            return true;

        } catch (MessagingException e) {
            log.error("邮件发送失败: {}", e.getMessage(), e);
            return false;
        }
    }
}