package com.open.spring.mvc.person.Email;


// Java program to send email 
  
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;


//dot env for email username/password
import io.github.cdimascio.dotenv.Dotenv;
  
public class Email  
{ 
  
   public static void sendEmail(String recipient, String subject, Multipart multipart){
      // email ID of Recipient. 
  
      // email ID of  Sender. 
      String sender = "sender@gmail.com";
  
      // Getting system properties 
      Properties properties = System.getProperties(); 
  
      // Setting up mail server 
      properties.put("mail.smtp.auth", "true");
      properties.put("mail.smtp.starttls.enable", "true");
      properties.put("mail.smtp.host", "smtp.gmail.com");
      properties.put("mail.smtp.port", 587);
      properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
  
      // creating session object to get properties 

      final Dotenv dotenv = Dotenv.load();
      final String emailUsername = dotenv.get("EMAIL_USERNAME");
      final String emailPassword = dotenv.get("EMAIL_PASSWORD");
      Session session = Session.getDefaultInstance(properties,new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(emailUsername,emailPassword); // email and password, see this for app passwords https://support.google.com/accounts/answer/185833?visit_id=638748419667916449-2613033234&p=InvalidSecondFactor&rd=1
        }
    }); 
  
      try 
      { 
         // MimeMessage object. 
         MimeMessage message = new MimeMessage(session); 
  
         // Set From Field: adding senders email to from field. 
         message.setFrom(new InternetAddress(sender)); 
  
         // Set To Field: adding recipient's email to from field. 
         message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient)); 
  
         // Set Subject: subject of the email 
         message.setSubject(subject); 
  
         // SetContent: content (Multipart) of the email
         message.setContent(multipart);

  
         // Send email. 
         Transport.send(message); 
         System.out.println("Mail successfully sent"); 
      } 
      catch (MessagingException mex)  
      { 
         mex.printStackTrace(); 
      } 
   }

   public static void sendEmail(String recipient, String subject, String content){

      try{
         MimeMultipart emailContent = new MimeMultipart();
         MimeBodyPart body1 = new MimeBodyPart();
         body1.setContent("<p>"+content+"</p>","text/html");

         emailContent.addBodyPart(body1);

         sendEmail(recipient, subject, emailContent);
      }
      catch (MessagingException mex)  
      { 
         mex.printStackTrace(); 
      } 
   }

   public static void sendPasswordResetEmail(String recipient,String code){

      try{
         MimeMultipart emailContent = new MimeMultipart();

         MimeBodyPart body1 = new MimeBodyPart();
         body1.setContent("<h1>To reset your password use the following code:</h1>","text/html");
         MimeBodyPart body2 = new MimeBodyPart();
         body2.setContent("<code style=\"background-color: lightblue; font-size: 50px; border-radius: 15px;\">"+code+"</code>","text/html");

         emailContent.addBodyPart(body1);
         emailContent.addBodyPart(body2);

         sendEmail(recipient, "Password Reset", emailContent);
      }
      catch (MessagingException mex)  
      { 
         mex.printStackTrace(); 
      } 
   }

   public static void sendVerificationEmail(String recipient,String code){

      try{
         MimeMultipart emailContent = new MimeMultipart();

         MimeBodyPart body1 = new MimeBodyPart();
         body1.setContent("<h1>Thank you for signing up for DNHS Computer Science. Use the following code to verify your email:</h1>","text/html");
         MimeBodyPart body2 = new MimeBodyPart();
         body2.setContent("<code style=\"background-color: lightblue; font-size: 50px; border-radius: 15px;\">"+code+"</code>","text/html");

         emailContent.addBodyPart(body1);
         emailContent.addBodyPart(body2);

         sendEmail(recipient, "Email Verification", emailContent);
      }
      catch (MessagingException mex)  
      { 
         mex.printStackTrace(); 
      } 
   }
} 
