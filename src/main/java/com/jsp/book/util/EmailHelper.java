package com.jsp.book.util;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;

import ch.qos.logback.core.Context;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailHelper {

	public final JavaMailSender mailSender;
	public final TemplateEngine templateEngine;
	
	@Async
	public void sendOtp(int otp, String name, String email) {
		MimeMessage mimeMessage=mailSender.createMimeMessage();
		MimeMessageHelper helper=new MimeMessageHelper(mimeMessage);
		try {
			helper.setFrom("book-my-ticket.com","Book-My-Ticket");
			helper.setTo(email);
			helper.setSubject("OTP for creating account with BookMyTicket");
			Context context=new Context();
			context.setVariable("name",name);
			context.setVariable("otp", otp);g
			String text=templateEngine.process("email-template.html", context);
			helper.setText(text,true);
			mailSender.send(mimeMessage);
		} catch (Exception e) {
			System.out.println("Failed to send OTP: "+ otp);
		}
	}
}
