package com.lhlwork.pojo.mail;

import lombok.Data;

import java.io.File;

@Data
public class MailDTO {

    /**
     * 接收者，批量发送用,分开
     */
    private String receiver;
    /**
     * 邮件主题
     */
    private String subject;
    /**
     * 邮件内容
     */
    private String text;
    /**
     * 附件路径
     */
    private File[] files;
    /**
     * 是否是html
     */
    private boolean html;
}
