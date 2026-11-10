package com.makjan.sulgilddara.user.common.mail;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.makjan.sulgilddara.user.model.mapper.UserMapper;
import com.makjan.sulgilddara.user.model.vo.Mail;
import com.makjan.sulgilddara.user.model.vo.User;

@Service
public class SendEmailService {

    private final UserMapper mapper;
    private final JavaMailSender mailSender;
    private static final String FROM_ADDRESS = "yeeuncode@gmail.com";

    @Autowired
    public SendEmailService(UserMapper mapper, JavaMailSender mailSender) {
        this.mapper = mapper;
        this.mailSender = mailSender;
    }

    public void sendTemporaryPassword(String userId, String email) {
        String tempPassword = generateTempPassword();
        Mail mail = createMail(userId, email, tempPassword);
        updatePassword(userId, tempPassword);
        sendMail(mail);
    }

	// 새로운 임시번호 생성함
	// 대문자와 숫자로 이루어진 랜덤의 10글자 임시비밀번호 생성
    private String generateTempPassword() {
        char[] charSet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) { // 원하는 길이로 조정
            sb.append(charSet[random.nextInt(charSet.length)]);
        }
        return sb.toString();
    }

    // 메일 생성 - 주소/ 타이틀/ 내용 설정
    private Mail createMail(String userId, String email, String tempPassword) {
        Mail mail = new Mail();
        mail.setAddress(email);
        mail.setTitle(userId + "님의 술길따라 임시비밀번호 안내 이메일");
        mail.setMessage("안녕하세요. 술길따라 임시비밀번호 안내 이메일입니다.\n\n"
                + "아이디: " + userId + "\n"
                + "임시 비밀번호: " + tempPassword + "\n\n"
                + "로그인 후 비밀번호를 변경해 주세요.");
        return mail;
    }

    // 임시비밀번호를 해당 유저의 비밀번호로 업데이트함
    private void updatePassword(String userId, String tempPassword) {
        String encryptedPassword = EncryptionUtils.encryptMD5(tempPassword);
        User user = mapper.selectOneById(userId);
        if (user != null) {
            user.setUserPw(encryptedPassword);
            mapper.updateUser(user);
        }
    }

    // 메일 전송
    public void sendMail(Mail mail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(mail.getAddress());
        message.setFrom(FROM_ADDRESS);
        message.setSubject(mail.getTitle());
        message.setText(mail.getMessage());
        mailSender.send(message);
    }
}
