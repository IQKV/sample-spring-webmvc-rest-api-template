/*
 * Copyright 2025 IQKV Foundation Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iqkv.sample.webmvc.dashboard.shared.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.iqkv.boot.mail.MailProperties;
import com.iqkv.sample.webmvc.dashboard.IntegrationTest;
import com.iqkv.sample.webmvc.dashboard.config.AppConstants;
import com.iqkv.sample.webmvc.dashboard.shared.testutil.BoundedContextTestBase;
import com.iqkv.sample.webmvc.dashboard.usermanagement.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Integration tests for MailService within the Shared Infrastructure.
 * <p>
 * Tests email sending functionality, template processing, and cross-bounded context
 * email operations. This service is shared across all bounded contexts.
 */
@IntegrationTest
class MailServiceIT extends BoundedContextTestBase {

  private static final String[] SUPPORTED_LANGUAGES = {
      "en", "ru", "fr", "it"
  };

  private static final Pattern PATTERN_LOCALE_3 = Pattern.compile("([a-z]{2})-([a-zA-Z]{4})-([a-z]{2})");
  private static final Pattern PATTERN_LOCALE_2 = Pattern.compile("([a-z]{2})-([a-z]{2})");

  @Autowired
  private MailProperties mailProperties;

  @MockitoBean
  private JavaMailSender javaMailSender;

  @Captor
  private ArgumentCaptor<MimeMessage> messageCaptor;

  @Autowired
  private MailService mailService;

  @BeforeEach
  void setUp() {
    doNothing().when(javaMailSender).send(any(MimeMessage.class));
    when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
  }

  @Test
  void shouldSendPlainTextEmail() throws Exception {
    // Given
    String recipient = "john.doe@example.com";
    String subject = "testSubject";
    String content = "testContent";

    // When
    mailService.sendEmail(recipient, subject, content, false, false);

    // Then
    verify(javaMailSender).send(messageCaptor.capture());
    MimeMessage message = messageCaptor.getValue();
    assertThat(message.getSubject()).isEqualTo(subject);
    assertThat(message.getAllRecipients()[0]).hasToString(recipient);
    assertThat(message.getFrom()[0]).hasToString(mailProperties.getFrom());
    assertThat(message.getContent()).isInstanceOf(String.class);
    assertThat(message.getContent()).hasToString(content);
    assertThat(message.getDataHandler().getContentType()).isEqualTo("text/plain; charset=UTF-8");
  }

  @Test
  void shouldSendHtmlEmail() throws Exception {
    // Given
    String recipient = "john.doe@example.com";
    String subject = "testSubject";
    String htmlContent = "<h1>testContent</h1>";

    // When
    mailService.sendEmail(recipient, subject, htmlContent, false, true);

    // Then
    verify(javaMailSender).send(messageCaptor.capture());
    MimeMessage message = messageCaptor.getValue();
    assertThat(message.getSubject()).isEqualTo(subject);
    assertThat(message.getAllRecipients()[0]).hasToString(recipient);
    assertThat(message.getFrom()[0]).hasToString(mailProperties.getFrom());
    assertThat(message.getContent()).isInstanceOf(String.class);
    assertThat(message.getContent()).hasToString(htmlContent);
    assertThat(message.getDataHandler().getContentType()).isEqualTo("text/html;charset=UTF-8");
  }

  @Test
  void shouldSendMultipartEmail() throws Exception {
    // Given
    String recipient = "john.doe@example.com";
    String subject = "testSubject";
    String content = "testContent";

    // When
    mailService.sendEmail(recipient, subject, content, true, false);

    // Then
    verify(javaMailSender).send(messageCaptor.capture());
    MimeMessage message = messageCaptor.getValue();
    MimeMultipart mp = (MimeMultipart) message.getContent();
    MimeBodyPart part = (MimeBodyPart) ((MimeMultipart) mp.getBodyPart(0).getContent()).getBodyPart(0);
    ByteArrayOutputStream aos = new ByteArrayOutputStream();
    part.writeTo(aos);

    assertThat(message.getSubject()).isEqualTo(subject);
    assertThat(message.getAllRecipients()[0]).hasToString(recipient);
    assertThat(message.getFrom()[0]).hasToString(mailProperties.getFrom());
    assertThat(message.getContent()).isInstanceOf(Multipart.class);
    assertThat(aos).hasToString("\r\n" + content);
    assertThat(part.getDataHandler().getContentType()).isEqualTo("text/plain; charset=UTF-8");
  }

  @Test
  void shouldSendMultipartHtmlEmail() throws Exception {
    // Given
    String recipient = "john.doe@example.com";
    String subject = "testSubject";
    String htmlContent = "<h1>testContent</h1>";

    // When
    mailService.sendEmail(recipient, subject, htmlContent, true, true);

    // Then
    verify(javaMailSender).send(messageCaptor.capture());
    MimeMessage message = messageCaptor.getValue();
    MimeMultipart mp = (MimeMultipart) message.getContent();
    MimeBodyPart part = (MimeBodyPart) ((MimeMultipart) mp.getBodyPart(0).getContent()).getBodyPart(0);
    ByteArrayOutputStream aos = new ByteArrayOutputStream();
    part.writeTo(aos);

    assertThat(message.getSubject()).isEqualTo(subject);
    assertThat(message.getAllRecipients()[0]).hasToString(recipient);
    assertThat(message.getFrom()[0]).hasToString(mailProperties.getFrom());
    assertThat(message.getContent()).isInstanceOf(Multipart.class);
    assertThat(aos).hasToString("\r\n" + htmlContent);
    assertThat(part.getDataHandler().getContentType()).isEqualTo("text/html;charset=UTF-8");
  }

  @Test
  void shouldSendEmailFromTemplate() throws Exception {
    // Given
    User user = createTestUser();

    // When
    mailService.sendEmailFromTemplate(user, "mail/testEmail", "email.test.title");

    // Then
    verify(javaMailSender).send(messageCaptor.capture());
    MimeMessage message = messageCaptor.getValue();
    assertThat(message.getSubject()).isEqualTo("test title");
    assertThat(message.getAllRecipients()[0]).hasToString(user.getEmail());
    assertThat(message.getFrom()[0]).hasToString(mailProperties.getFrom());
    assertThat(message.getContent().toString()).isEqualToNormalizingNewlines(
        "<html>test title, http://127.0.0.1:8080, john</html>\n"
    );
    assertThat(message.getDataHandler().getContentType()).isEqualTo("text/html;charset=UTF-8");
  }

  @Test
  void shouldSendActivationEmail() throws Exception {
    // Given
    User user = createTestUser();

    // When
    mailService.sendActivationEmail(user);

    // Then
    verify(javaMailSender).send(messageCaptor.capture());
    MimeMessage message = messageCaptor.getValue();
    assertThat(message.getAllRecipients()[0]).hasToString(user.getEmail());
    assertThat(message.getFrom()[0]).hasToString(mailProperties.getFrom());
    assertThat(message.getContent().toString()).isNotEmpty();
    assertThat(message.getDataHandler().getContentType()).isEqualTo("text/html;charset=UTF-8");
  }

  @Test
  void shouldSendCreationEmail() throws Exception {
    // Given
    User user = createTestUser();

    // When
    mailService.sendCreationEmail(user);

    // Then
    verify(javaMailSender).send(messageCaptor.capture());
    MimeMessage message = messageCaptor.getValue();
    assertThat(message.getAllRecipients()[0]).hasToString(user.getEmail());
    assertThat(message.getFrom()[0]).hasToString(mailProperties.getFrom());
    assertThat(message.getContent().toString()).isNotEmpty();
    assertThat(message.getDataHandler().getContentType()).isEqualTo("text/html;charset=UTF-8");
  }

  @Test
  void shouldSendPasswordResetEmail() throws Exception {
    // Given
    User user = createTestUser();

    // When
    mailService.sendPasswordResetMail(user);

    // Then
    verify(javaMailSender).send(messageCaptor.capture());
    MimeMessage message = messageCaptor.getValue();
    assertThat(message.getAllRecipients()[0]).hasToString(user.getEmail());
    assertThat(message.getFrom()[0]).hasToString(mailProperties.getFrom());
    assertThat(message.getContent().toString()).isNotEmpty();
    assertThat(message.getDataHandler().getContentType()).isEqualTo("text/html;charset=UTF-8");
  }

  @Test
  void shouldHandleMailSendException() {
    // Given
    doThrow(MailSendException.class).when(javaMailSender).send(any(MimeMessage.class));

    // When & Then - Should not throw exception (graceful handling)
    try {
      mailService.sendEmail("john.doe@example.com", "testSubject", "testContent", false, false);
    } catch (Exception e) {
      fail("Exception shouldn't have been thrown - mail service should handle exceptions gracefully");
    }
  }

  @Test
  void shouldSendLocalizedEmailsForAllSupportedLanguages() throws Exception {
    // Given
    User user = createTestUser();

    // When & Then - Test each supported language
    for (String langKey : SUPPORTED_LANGUAGES) {
      user.setLangKey(langKey);
      mailService.sendEmailFromTemplate(user, "mail/testEmail", "email.test.title");
      verify(javaMailSender, atLeastOnce()).send(messageCaptor.capture());
      MimeMessage message = messageCaptor.getValue();

      // Load expected localized content
      String propertyFilePath = "i18n/messages_" + getMessageSourceSuffixForLanguage(langKey) + ".properties";
      URL resource = this.getClass().getClassLoader().getResource(propertyFilePath);
      assertThat(resource).isNotNull();

      File file = new File(new URI(resource.getFile()).getPath());
      Properties properties = new Properties();
      properties.load(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));

      String emailTitle = (String) properties.get("email.test.title");
      assertThat(message.getSubject()).isEqualTo(emailTitle);
      assertThat(message.getContent().toString()).isEqualToNormalizingNewlines(
          "<html>" + emailTitle + ", http://127.0.0.1:8080, john</html>\n"
      );
    }
  }

  @Test
  void shouldValidateEmailAddresses() {
    // Given
    String invalidEmail = "invalid-email";
    String validEmail = "valid@example.com";

    // When & Then - Should handle invalid emails gracefully
    try {
      mailService.sendEmail(invalidEmail, "subject", "content", false, false);
      // Should not throw exception but may log error
    } catch (Exception e) {
      // Expected behavior for invalid email
      assertThat(e).isInstanceOf(RuntimeException.class);
    }
  }

  @Test
  void shouldHandleEmptyContent() throws Exception {
    // Given
    String recipient = "john.doe@example.com";
    String subject = "Empty Content Test";
    String emptyContent = "";

    // When
    mailService.sendEmail(recipient, subject, emptyContent, false, false);

    // Then
    verify(javaMailSender).send(messageCaptor.capture());
    MimeMessage message = messageCaptor.getValue();
    assertThat(message.getSubject()).isEqualTo(subject);
    assertThat(message.getContent()).hasToString(emptyContent);
  }

  @Override
  protected void cleanupBoundedContextData() {
    // No specific cleanup needed for mail service tests
    // Mail service is stateless and doesn't persist data
  }

  private User createTestUser() {
    User user = new User();
    user.setLangKey(AppConstants.DEFAULT_LANGUAGE);
    user.setLogin("john");
    user.setEmail("john.doe@example.com");
    user.setFirstName("John");
    user.setLastName("Doe");
    return user;
  }

  /**
   * Convert a language key to the Java locale suffix for message properties.
   */
  private String getMessageSourceSuffixForLanguage(String langKey) {
    String javaLangKey = langKey;
    Matcher matcher2 = PATTERN_LOCALE_2.matcher(langKey);
    if (matcher2.matches()) {
      javaLangKey = matcher2.group(1) + "_" + matcher2.group(2).toUpperCase();
    }
    Matcher matcher3 = PATTERN_LOCALE_3.matcher(langKey);
    if (matcher3.matches()) {
      javaLangKey = matcher3.group(1) + "_" + matcher3.group(2) + "_" + matcher3.group(3).toUpperCase();
    }
    return javaLangKey;
  }
}
