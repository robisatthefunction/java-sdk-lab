package com.optimizely.ab.examples;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.optimizely.ab.Optimizely;
import com.optimizely.ab.annotations.OptimizelyFeature;
import com.optimizely.ab.annotations.OptimizelyVariable;
import com.optimizely.ab.annotations.OptimizelyVariation;
import com.optimizely.ab.client.*;
import com.optimizely.ab.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.net.URL;
import java.util.UUID;


/**
 * Created by mdavis on 4/18/18.
 */
public class EmailApplication {
    private static final Logger LOG = LoggerFactory.getLogger(EmailApplication.class);

    private void sendEmail(String userId) {
        OptimizelyClient optimizelyClient = OptimizelyClientFactory.getClient();

        EmailModel model = optimizelyClient.getFeature(EmailModel.class);
        model.recipient = userId;

        EmailSender emailSender = optimizelyClient.getFeature(EmailSender.class);
        emailSender.send(model);
    }

    @OptimizelyFeature(name = "welcome_email")
    public static class EmailModel {

        @OptimizelyVariable(name = "subject_key")
        public String subject = "Welcome to Optimizely!!";

        @OptimizelyVariable(name = "body_key")
        public String body = "Click here.";

        public String recipient;
    }

    @OptimizelyFeature(name = "email_sender")
    public interface EmailSender {
        void send(EmailModel model);
    }

    @OptimizelyVariation(name = "info")
    public static class EmailSenderInfo implements EmailSender {
        public void send(EmailModel model) {
            LOG.info("Sent email to: {}", model.recipient);
            LOG.info("Sent email subject: {}", model.subject);
            LOG.info("Sent email body: {}", model.body);

            OptimizelyClientFactory.getClient().track("success");
        }
    }

    @OptimizelyVariation(name = "warn")
    public static class EmailSenderWarn implements EmailSender {
        public void send(EmailModel model) {
            LOG.warn("Sent email to: {}", model.recipient);
            LOG.warn("Sent email subject: {}", model.subject);
            LOG.warn("Sent email body: {}", model.body);

            OptimizelyClientFactory.getClient().track("success");
        }
    }

    public static void main(String[] args) throws Exception {
        URL url = Resources.getResource("email_datafile.json");
        String datafile = Resources.toString(url, Charsets.UTF_8);

        EventHandler eventHandler = logEvent -> LOG.debug(logEvent.toString());
        Optimizely optimizely = Optimizely.builder(datafile, eventHandler).build();

        OptimizelyRegistry registry = new OptimizelyRegistry();
        registry.register(EmailModel.class);
        registry.register(EmailSender.class);
        registry.register(EmailSenderWarn.class);
        registry.register(EmailSenderInfo.class);

        OptimizelyValidator validator = new OptimizelyValidator(registry);

        if (!validator.validate(optimizely)) {
            System.out.println("Datafile failed validation!");
            System.exit(1);
        }

        OptimizelyClientFactory.setProvider(() -> new OptimizelyMDCClient(optimizely, registry));
        EmailApplication emailApplication = new EmailApplication();

        for (int i = 0; i < 1000; i++) {
            String userId = UUID.randomUUID().toString();
            MDC.put("optimizelyEndUserId", userId);
            emailApplication.sendEmail(userId);

            Thread.sleep(1000);
            System.out.println();
            System.out.println();
        }
    }
}
