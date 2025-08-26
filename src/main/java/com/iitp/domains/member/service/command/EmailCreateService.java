package com.iitp.domains.member.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailCreateService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${business.approval.base-url}")
    private String baseUrl;

    /**
     * 사업자 승인 이메일 발송
     */
    @Async
    public void sendBusinessApprovalEmail(String toEmail, String businessLicenseNumber, Long memberId) {
        try {
            log.info("사업자 승인 이메일 발송 시작 - email: {}, memberId: {}", toEmail, memberId);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_NO,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(fromEmail, "RE:FOOD 팀");
            helper.setTo(toEmail);
            helper.setSubject("[RE:FOOD] 사업자 접수 완료 및 승인 요청");

            String htmlContent = createBusinessApprovalEmailHtml(businessLicenseNumber, memberId);
            helper.setText(htmlContent, true);

            message.setHeader("Content-Type", "text/html; charset=UTF-8");

            mailSender.send(message);
            log.info("사업자 승인 이메일 발송 완료 - email: {}, memberId: {}", toEmail, memberId);

        } catch (MessagingException e) {
            log.error("사업자 승인 이메일 발송 실패 - email: {}, memberId: {}, error: {}",
                    toEmail, memberId, e.getMessage(), e);
        } catch (Exception e) {
            log.error("이메일 발송 중 예외 발생 - email: {}, memberId: {}, error: {}",
                    toEmail, memberId, e.getMessage(), e);
        }
    }

    /**
     * 이미지를 Base64로 변환
     */
    private String getImageAsBase64(String imagePath) {
        try {
            ClassPathResource resource = new ClassPathResource("static/images/" + imagePath);
            byte[] imageBytes = StreamUtils.copyToByteArray(resource.getInputStream());
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            log.warn("이미지 로드 실패: {}", imagePath, e);
            return null; // 이미지 로드 실패시 null 반환
        }
    }

    /**
     * 사업자 승인 이메일 HTML
     */
    private String createBusinessApprovalEmailHtml(String businessLicenseNumber, Long memberId) {
        String approvalUrl = baseUrl + "/api/members/business/approve/" + memberId;
        String logoBase64 = getImageAsBase64("refood-logo.png"); // PNG 파일명

        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html lang=\"ko\">");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        html.append("<title>RE:FOOD 사업자 등록 승인</title>");
        html.append("</head>");
        html.append("<body style=\"margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Malgun Gothic', Arial, sans-serif; background-color: #f8f9fa; line-height: 1.6;\">");

        html.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" style=\"background-color: #f8f9fa; margin: 0; padding: 40px 20px;\">");
        html.append("<tr>");
        html.append("<td align=\"center\">");

        html.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"600\" style=\"max-width: 600px; background-color: #ffffff; border-radius: 20px; box-shadow: 0 4px 24px rgba(0,0,0,0.06); overflow: hidden;\">");

        html.append("<tr>");
        html.append("<td style=\"background: #0FB758; padding: 60px 40px; text-align: center;\">");

        // 이미지가 있으면 이미지 사용, 없으면 이모지 사용
        html.append("<div style=\"display: inline-block; width: 60px; height: 60px; background-color: rgba(255,255,255,0.2); border-radius: 16px; text-align: center; line-height: 60px; margin-bottom: 24px;\">");
        if (logoBase64 != null) {
            html.append("<img src=\"data:image/png;base64,").append(logoBase64).append("\" alt=\"RE:FOOD 로고\" style=\"width: 40px; height: 40px; margin-top: 10px; border-radius: 8px;\" />");
        } else {
            html.append("<span style=\"font-size: 30px;\">🍃</span>");
        }
        html.append("</div>");

        html.append("<h1 style=\"color: #ffffff; font-size: 42px; font-weight: 700; margin: 0 0 12px 0; letter-spacing: -0.5px;\">RE:FOOD</h1>");

        html.append("<p style=\"color: rgba(255,255,255,0.95); font-size: 18px; font-weight: 400; margin: 0 0 16px 0;\">Walk, Eat, Save</p>");

        html.append("<p style=\"color: rgba(255,255,255,0.9); font-size: 24px; font-weight: 600; margin: 0; line-height: 1.3;\">환경과 함께하는 똑똑한 한 끼</p>");

        html.append("</td>");
        html.append("</tr>");

        // ... 나머지 HTML 코드는 동일 ...
        html.append("<tr>");
        html.append("<td style=\"padding: 60px 50px;\">");

        html.append("<h2 style=\"font-size: 26px; font-weight: 600; color: #1B5E20; text-align: center; margin: 0 0 24px 0; line-height: 1.3;\">사업자 등록 신청이<br>접수되었습니다! 🎉</h2>");

        html.append("<p style=\"font-size: 16px; color: #424242; text-align: center; margin: 0 0 50px 0; line-height: 1.7;\">");
        html.append("안녕하세요! 아래 버튼을 클릭하여<br>사업자 등록을 완료해주세요.");
        html.append("</p>");

        html.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" style=\"background: #F1F8E9; border: 2px solid #C8E6C9; border-radius: 20px; margin: 50px 0;\">");
        html.append("<tr>");
        html.append("<td style=\"padding: 40px; text-align: center;\">");
        html.append("<p style=\"font-size: 14px; color: #2E7D32; font-weight: 600; margin: 0 0 12px 0; text-transform: uppercase; letter-spacing: 1px;\">사업자 등록번호</p>");
        html.append("<div style=\"font-size: 32px; font-weight: 800; color: #1B5E20; letter-spacing: 3px; margin: 0; font-family: 'Courier New', monospace;\">").append(businessLicenseNumber).append("</div>");
        html.append("</td>");
        html.append("</tr>");
        html.append("</table>");

        html.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" style=\"margin: 50px 0;\">");
        html.append("<tr>");
        html.append("<td align=\"center\">");
        html.append("<a href=\"").append(approvalUrl).append("\" style=\"");
        html.append("display: inline-block; ");
        html.append("background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%); ");
        html.append("color: #ffffff; ");
        html.append("padding: 18px 48px; ");
        html.append("text-decoration: none; ");
        html.append("border-radius: 50px; ");
        html.append("font-size: 16px; ");
        html.append("font-weight: 600; ");
        html.append("letter-spacing: 0.3px; ");
        html.append("box-shadow: 0 8px 24px rgba(76, 175, 80, 0.3); ");
        html.append("transition: all 0.3s ease;");
        html.append("\">");
        html.append("<span style=\"margin-right: 8px; font-size: 18px;\">✅</span>");
        html.append("사업자 등록 승인하기");
        html.append("</a>");
        html.append("</td>");
        html.append("</tr>");
        html.append("</table>");

        html.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" style=\"background: #FFF8E1; border: 1px solid #FFD54F; border-radius: 16px; margin: 50px 0;\">");
        html.append("<tr>");
        html.append("<td style=\"padding: 24px 32px;\">");
        html.append("<p style=\"color: #F57F17; font-weight: 500; font-size: 14px; margin: 0; text-align: center;\">");
        html.append("🔒 보안을 위해 이 승인 링크는 한 번만 사용할 수 있습니다");
        html.append("</p>");
        html.append("</td>");
        html.append("</tr>");
        html.append("</table>");

        html.append("</td>");
        html.append("</tr>");

        html.append("<tr>");
        html.append("<td style=\"background: #FAFAFA; padding: 40px 50px; text-align: center; border-top: 1px solid #E0E0E0;\">");
        html.append("<p style=\"color: #757575; font-size: 14px; margin: 0 0 16px 0; line-height: 1.5;\">");
        html.append("궁금한 사항이 있으시면 언제든 고객센터로 문의해주세요");
        html.append("</p>");
        html.append("<div style=\"display: inline-block; padding: 8px 16px; background: #E8F5E8; border-radius: 20px;\">");
        html.append("<span style=\"color: #2E7D32; font-weight: 600; font-size: 14px;\">RE:FOOD 팀 🌱</span>");
        html.append("</div>");
        html.append("</td>");
        html.append("</tr>");

        html.append("</table>");
        html.append("</td>");
        html.append("</tr>");
        html.append("</table>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    /**
     * 승인 성공 페이지
     */
    public String createSuccessPage() {
        return """
    <!DOCTYPE html>
    <html lang="ko">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>RE:FOOD 승인 완료</title>
        <style>
            * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
            }
            body {
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Malgun Gothic', sans-serif;
                background: linear-gradient(135deg, #7CB342 0%, #558B2F 100%);
                height: 100vh;
                display: flex;
                justify-content: center;
                align-items: center;
                margin: 0;
                overflow: hidden;
            }
            .container {
                background: white;
                padding: 3.5rem 3rem;
                border-radius: 24px;
                box-shadow: 0 30px 80px rgba(0,0,0,0.15);
                text-align: center;
                max-width: 520px;
                width: 90%;
                animation: fadeInUp 0.8s ease-out;
                position: relative;
                overflow: hidden;
            }
            .container::before {
                content: '';
                position: absolute;
                top: 0;
                left: 0;
                right: 0;
                height: 4px;
                background: linear-gradient(135deg, #7CB342, #558B2F);
            }
            @keyframes fadeInUp {
                from {
                    opacity: 0;
                    transform: translateY(40px);
                }
                to {
                    opacity: 1;
                    transform: translateY(0);
                }
            }
            .success-icon {
                width: 100px;
                height: 100px;
                background: linear-gradient(135deg, #7CB342, #558B2F);
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
                margin: 0 auto 2rem;
                animation: bounce 1s ease-out 0.5s both;
                box-shadow: 0 12px 32px rgba(124, 179, 66, 0.3);
            }
            @keyframes bounce {
                0%, 20%, 53%, 80%, 100% {
                    transform: translate3d(0,0,0);
                }
                40%, 43% {
                    transform: translate3d(0, -25px, 0);
                }
                70% {
                    transform: translate3d(0, -12px, 0);
                }
                90% {
                    transform: translate3d(0, -4px, 0);
                }
            }
            .checkmark {
                color: white;
                font-size: 3rem;
                font-weight: bold;
            }
            .welcome-text {
                color: #7CB342;
                font-size: 18px;
                font-weight: 600;
                margin-bottom: 16px;
                opacity: 0;
                animation: fadeIn 0.6s ease-out 1s both;
            }
            h1 {
                color: #2E7D32;
                font-size: 2rem;
                margin-bottom: 1.5rem;
                font-weight: 800;
                line-height: 1.2;
                opacity: 0;
                animation: fadeIn 0.6s ease-out 1.2s both;
            }
            p {
                color: #546E7A;
                font-size: 1.2rem;
                line-height: 1.7;
                margin-bottom: 2.5rem;
                opacity: 0;
                animation: fadeIn 0.6s ease-out 1.4s both;
            }
            .brand {
                color: #7CB342;
                font-weight: 800;
            }
            .features-box {
                background: linear-gradient(135deg, #E8F5E8, #C8E6C9);
                padding: 24px;
                border-radius: 16px;
                margin: 32px 0;
                border: 1px solid #A5D6A7;
                opacity: 0;
                animation: fadeIn 0.6s ease-out 1.6s both;
            }
            .features-title {
                color: #2E7D32;
                font-weight: 700;
                font-size: 16px;
                margin-bottom: 12px;
            }
            .features-text {
                color: #388E3C;
                font-size: 14px;
                line-height: 1.5;
            }
            .close-info {
                background: #FFF3E0;
                padding: 20px;
                border-radius: 16px;
                color: #E65100;
                font-size: 1rem;
                border: 1px solid #FFB74D;
                opacity: 0;
                animation: fadeIn 0.6s ease-out 1.8s both;
            }
            .countdown {
                color: #FF6F00;
                font-weight: bold;
                font-size: 1.2rem;
            }
            @keyframes fadeIn {
                from {
                    opacity: 0;
                    transform: translateY(20px);
                }
                to {
                    opacity: 1;
                    transform: translateY(0);
                }
            }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="success-icon">
                <div class="checkmark">✓</div>
            </div>
            <div class="welcome-text">환경과 함께하는 똑똑한 한 끼</div>
            <h1>사업자 등록이<br>승인되었습니다!</h1>
            <p>
                <span class="brand">RE:FOOD</span> 파트너로 함께하게 되어 기쁩니다.<br>
                이제 모든 서비스를 이용하실 수 있습니다.
            </p>
            
            <div class="features-box">
                <div class="features-title">🌱 지금 바로 시작하세요</div>
                <div class="features-text">
                    Walk, Eat, Save! 고객들과 함께 환경을 생각하는<br>
                    지속 가능한 비즈니스를 만들어가세요.
                </div>
            </div>
            
            <div class="close-info">
                🎉 <span class="countdown">10</span>초 후 자동으로 창이 닫힙니다.
            </div>
        </div>
        
        <script>
            let countdown = 10;
            const countdownElement = document.querySelector('.countdown');
            
            const timer = setInterval(() => {
                countdown--;
                if (countdown > 0) {
                    countdownElement.textContent = countdown;
                } else {
                    clearInterval(timer);
                    window.close();
                    setTimeout(() => {
                        document.body.innerHTML = '<div style="display:flex;justify-content:center;align-items:center;height:100vh;font-family:sans-serif;color:#666;">창을 닫아주세요.</div>';
                    }, 100);
                }
            }, 1000);
        </script>
    </body>
    </html>
    """;
    }

    /**
     * 이미 승인된 경우 페이지
     */
    public String createAlreadyApprovedPage() {
        return """
    <!DOCTYPE html>
    <html lang="ko">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>승인 상태 확인</title>
        <style>
            * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
            }
            body {
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Malgun Gothic', sans-serif;
                background: linear-gradient(135deg, #E3F2FD 0%, #BBDEFB 100%);
                height: 100vh;
                display: flex;
                justify-content: center;
                align-items: center;
                margin: 0;
            }
            .container {
                background: white;
                padding: 3rem 2.5rem;
                border-radius: 24px;
                box-shadow: 0 20px 60px rgba(0,0,0,0.1);
                text-align: center;
                max-width: 500px;
                width: 90%;
                animation: fadeInUp 0.6s ease-out;
                border: 1px solid #E1F5FE;
            }
            @keyframes fadeInUp {
                from {
                    opacity: 0;
                    transform: translateY(30px);
                }
                to {
                    opacity: 1;
                    transform: translateY(0);
                }
            }
            .info-icon {
                width: 88px;
                height: 88px;
                background: linear-gradient(135deg, #42A5F5, #1E88E5);
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
                margin: 0 auto 2rem;
                color: white;
                font-size: 2.8rem;
                box-shadow: 0 8px 24px rgba(66, 165, 245, 0.3);
            }
            .status-badge {
                background: linear-gradient(135deg, #E8F5E8, #C8E6C9);
                color: #2E7D32;
                padding: 8px 20px;
                border-radius: 20px;
                font-size: 14px;
                font-weight: 700;
                margin-bottom: 1.5rem;
                display: inline-block;
                border: 1px solid #A5D6A7;
            }
            h1 {
                color: #1565C0;
                font-size: 1.9rem;
                margin-bottom: 1rem;
                font-weight: 700;
            }
            p {
                color: #546E7A;
                font-size: 1.1rem;
                line-height: 1.7;
                margin-bottom: 2rem;
            }
            .brand {
                color: #7CB342;
                font-weight: 700;
            }
            .info-section {
                background: linear-gradient(135deg, #F3E5F5, #E1BEE7);
                padding: 20px;
                border-radius: 16px;
                margin: 24px 0;
                border: 1px solid #CE93D8;
            }
            .info-title {
                color: #7B1FA2;
                font-weight: 700;
                font-size: 16px;
                margin-bottom: 8px;
            }
            .info-text {
                color: #4A148C;
                font-size: 14px;
                line-height: 1.5;
            }
            .close-info {
                background: #FFF3E0;
                padding: 16px;
                border-radius: 12px;
                color: #E65100;
                font-size: 0.9rem;
                border-left: 4px solid #FF9800;
                border: 1px solid #FFB74D;
            }
            .countdown {
                color: #FF6F00;
                font-weight: bold;
                font-size: 1.1rem;
            }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="info-icon">ℹ️</div>
            <h1>이미 승인이 완료되었습니다</h1>
            <p>
                <span class="brand">RE:FOOD</span> 사업자 등록이 이미 승인되어 있습니다.<br>
                모든 서비스를 정상적으로 이용하실 수 있습니다.
            </p>
            
            <div class="info-section">
                <div class="info-title">📋 현재 상태</div>
                <div class="info-text">
                    사업자 등록이 완료되어 RE:FOOD의 모든 기능을<br>
                     이용하실 수 있습니다.<br>
                    추가 승인 절차는 필요하지 않습니다.
                </div>
            </div>
            
            <div class="close-info">
                ⏰ <span class="countdown">10</span>초 후 자동으로 창이 닫힙니다.
            </div>
        </div>
        
        <script>
            let countdown = 10;
            const countdownElement = document.querySelector('.countdown');
            
            const timer = setInterval(() => {
                countdown--;
                if (countdown > 0) {
                    countdownElement.textContent = countdown;
                } else {
                    clearInterval(timer);
                    window.close();
                    setTimeout(() => {
                        document.body.innerHTML = '<div style="display:flex;justify-content:center;align-items:center;height:100vh;font-family:sans-serif;color:#666;">창을 닫아주세요.</div>';
                    }, 100);
                }
            }, 1000);
        </script>
    </body>
    </html>
    """;
    }

    /**
     * 에러 페이지 HTML
     */
    public String createErrorPage(String errorMessage) {
        return String.format("""
        <!DOCTYPE html>
        <html lang="ko">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>승인 실패</title>
            <style>
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                }
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Malgun Gothic', sans-serif;
                    background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
                    height: 100vh;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    margin: 0;
                }
                .container {
                    background: white;
                    padding: 3rem 2rem;
                    border-radius: 20px;
                    box-shadow: 0 20px 60px rgba(0,0,0,0.2);
                    text-align: center;
                    max-width: 500px;
                    width: 90%;
                    animation: fadeInUp 0.6s ease-out;
                }
                @keyframes fadeInUp {
                    from {
                        opacity: 0;
                        transform: translateY(30px);
                    }
                    to {
                        opacity: 1;
                        transform: translateY(0);
                    }
                }
                .error-icon {
                    width: 80px;
                    height: 80px;
                    background: linear-gradient(135deg, #e74c3c, #c0392b);
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    margin: 0 auto 2rem;
                    color: white;
                    font-size: 2.5rem;
                }
                h1 {
                    color: #2c3e50;
                    font-size: 1.8rem;
                    margin-bottom: 1rem;
                    font-weight: 600;
                }
                p {
                    color: #7f8c8d;
                    font-size: 1.1rem;
                    line-height: 1.6;
                    margin-bottom: 1rem;
                }
                .error-detail {
                    background: #ffeaea;
                    padding: 1rem;
                    border-radius: 10px;
                    color: #d32f2f;
                    font-size: 0.9rem;
                    margin-bottom: 2rem;
                    border-left: 4px solid #e74c3c;
                }
                .contact-info {
                    background: #f1f8e9;
                    padding: 1rem;
                    border-radius: 10px;
                    color: #2E7D32;
                    font-size: 0.9rem;
                    border-left: 4px solid #4CAF50;
                }
                .brand-text {
                    color: #4CAF50;
                    font-weight: bold;
                }
                .countdown {
                    color: #e74c3c;
                    font-weight: bold;
                    font-size: 1.1rem;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="error-icon">✕</div>
                <h1>승인 처리에 실패했습니다</h1>
                <p>사업자 등록 승인 중 문제가 발생했습니다.</p>
                <div class="error-detail">%s</div>
                <div class="contact-info">
                    🌿 <span class="brand-text">RE:FOOD</span> 고객센터로 문의해주세요.<br>
                    <span class="countdown">5</span>초 후 자동으로 창이 닫힙니다.
                </div>
            </div>
            
            <script>
                let countdown = 5;
                const countdownElement = document.querySelector('.countdown');
                
                const timer = setInterval(() => {
                    countdown--;
                    if (countdown > 0) {
                        countdownElement.textContent = countdown;
                    } else {
                        clearInterval(timer);
                        window.close();
                        setTimeout(() => {
                            document.body.innerHTML = '<div style="display:flex;justify-content:center;align-items:center;height:100vh;font-family:sans-serif;color:#666;">창을 닫아주세요.</div>';
                        }, 100);
                    }
                }, 1000);
            </script>
        </body>
        </html>
        """, errorMessage);
    }
}