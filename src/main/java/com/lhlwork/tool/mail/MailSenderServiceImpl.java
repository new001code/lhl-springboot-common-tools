package com.lhlwork.tool.mail;

import com.lhlwork.pojo.mail.MailDTO;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;

@Service
@Slf4j
public class MailSenderServiceImpl implements MailSenderService {

    @Resource
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String mailSender;


    @Override
    public void send(MailDTO mailDTO) {
        if (mailDTO.isHtml() || mailDTO.getFiles() != null) {
            MimeMessage message = javaMailSender.createMimeMessage();
            try {
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                //发送方
                helper.setFrom(mailSender);
                //接收方
                helper.setTo(mailDTO.getReceiver().split(","));
                //邮件主题
                helper.setSubject(mailDTO.getSubject());
                //邮件内容
                helper.setText(mailDTO.getText(), mailDTO.isHtml());
                //邮件发送时间
                helper.setSentDate(new Date());
                if (mailDTO.getFiles() != null) {
                    //添加附件
                    for (File file : mailDTO.getFiles()) {
                        log.debug("添加附件：{}, 大小{}", file.getName(), file.getTotalSpace());
                        helper.addAttachment(file.getName(), file);
                    }
                }
                javaMailSender.send(message);
            } catch (MessagingException e) {
                log.error("邮件发送失败", e);
            }
        } else {
            SimpleMailMessage message = new SimpleMailMessage();
            //发送方
            message.setFrom(mailSender);
            //接收方
            message.setTo(mailDTO.getReceiver().split(","));
            //邮件主题
            message.setSubject(mailDTO.getSubject());
            //邮件内容
            message.setText(mailDTO.getText());
            //邮件发送时间
            message.setSentDate(new Date());

            //发送邮件
            javaMailSender.send(message);
        }
        log.info("邮件发送成功：{} -> {}", mailSender, mailDTO.getReceiver());
    }
}
