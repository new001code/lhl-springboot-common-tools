package com.lhlwork.tool.mail;

import com.lhlwork.pojo.mail.MailDTO;

public interface MailSenderService {
    void send(MailDTO mailDTO);
}
