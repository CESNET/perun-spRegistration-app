package cz.metacentrum.perun.spRegistration.service;

import cz.metacentrum.perun.spRegistration.persistence.enums.RequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;

@SuppressWarnings("Duplicates")
public class Mails {

	private static final Logger log = LoggerFactory.getLogger(Mails.class);

	private static final String ADMINS_MAILS = "admins.emails";
	private static final String HOST_KEY = "host";
	private static final String FROM_KEY = "from";
	private static final String CREATE_SUBJECT_KEY = "create.subject";
	private static final String CREATE_MESSAGE_KEY = "create.message";
	private static final String UPDATE_SUBJECT_KEY = "update.subject";
	private static final String UPDATE_MESSAGE_KEY = "update.message";
	private static final String PRODUCTION_USER_SUBJECT_KEY = "production.user.subject";
	private static final String PRODUCTION_USER_MESSAGE_KEY = "production.user.message";
	private static final String APPROVAL_SUBJECT_KEY = "approval.subject";
	private static final String APPROVAL_MESSAGE_KEY = "approval.message";
	private static final String PRODUCTION_AUTHORITIES_MESSAGE_KEY = "production.authorities.subject";
	private static final String PRODUCTION_AUTHORITIES_SUBJECT_KEY = "production.authorities.message";
	private static final String FOOTER_KEY = "footer";

	private static final String REQUEST_ID_FIELD = "%REQUEST_ID%";
	private static final String NEW_STATUS_FIELD = "%NEW_STATUS%";
	private static final String SERVICE_NAME_FIELD = "%SERVICE_NAME%";
	private static final String APPROVAL_LINK_FIELD = "%APPROVAL_LINK%";
	private static final String USER_INFO_FIELD = "%USER_INFO%";

	public static boolean requestStatusUpdateUserNotify(Long requestId, RequestStatus status, List<String> recipients,
														Properties props) {
		return requestStatusUpdateUserNotify(requestId, status, recipients, null, props);
	}

	public static boolean requestStatusUpdateUserNotify(Long requestId, RequestStatus status, List<String> recipients,
														String additionalMessage, Properties props) {
		log.debug("requestStatusUpdateUserNotify(requestId: {}, status: {}, recipients: {}, additionalMessage: {}, props: {})",
				requestId, status, recipients, additionalMessage, props);
		String host = props.getProperty(HOST_KEY);
		String from = props.getProperty(FROM_KEY);

		String subject = props.getProperty(UPDATE_SUBJECT_KEY);
		if (subject.contains(REQUEST_ID_FIELD)) {
			subject = subject.replaceAll(REQUEST_ID_FIELD, requestId.toString());
		}

		String message = props.getProperty(UPDATE_MESSAGE_KEY);
		if (message.contains(REQUEST_ID_FIELD)) {
			message = message.replaceAll(REQUEST_ID_FIELD, requestId.toString());
		}
		if (message.contains(NEW_STATUS_FIELD)) {
			message = message.replaceAll(NEW_STATUS_FIELD, status.toString());
		}
		if (additionalMessage != null && !additionalMessage.isEmpty()) {
			message = message.concat("\n").concat(additionalMessage);
		}

		message = message.concat("\n").concat(props.getProperty(FOOTER_KEY));

		boolean res = sendMail(host, from, recipients, subject, message);
		log.debug("requestStatusUpdateUserNotify() returns: {}", res);
		return res;
	}

	public static boolean userCreateRequestNotify(Long requestId, String serviceName, List<String> recipients, Properties props) {
		log.debug("userCreateRequestNotify(requestId: {}, serviceName: {}, recipients: {}, props: {})",
				requestId, serviceName, recipients, props);
		String host = props.getProperty(HOST_KEY);
		String from = props.getProperty(FROM_KEY);

		String subject = props.getProperty(CREATE_SUBJECT_KEY);
		if (subject.contains(REQUEST_ID_FIELD)) {
			subject = subject.replaceAll(REQUEST_ID_FIELD, requestId.toString());
		}

		String message = props.getProperty(CREATE_MESSAGE_KEY);
		if (message.contains(REQUEST_ID_FIELD)) {
			message = message.replaceAll(REQUEST_ID_FIELD, requestId.toString());
		}

		if (message.contains(SERVICE_NAME_FIELD)) {
			message = message.replaceAll(SERVICE_NAME_FIELD, serviceName);
		}
		message = message.concat("\n").concat(props.getProperty(FOOTER_KEY));

		boolean res = sendMail(host, from, recipients, subject, message);
		log.debug("userCreateRequestNotify() returns: {}", res);
		return res;
	}

	public static boolean transferToProductionUserNotify(Long requestId, String serviceName, List<String> recipients, Properties props) {
		log.debug("transferToProductionUserNotify(requestId: {}, serviceName: {}, recipients: {}, props: {})",
				requestId, serviceName, recipients, props);
		String host = props.getProperty(HOST_KEY);
		String from = props.getProperty(FROM_KEY);

		String subject = props.getProperty(PRODUCTION_USER_SUBJECT_KEY);
		if (subject.contains(REQUEST_ID_FIELD)) {
			subject = subject.replaceAll(REQUEST_ID_FIELD, requestId.toString());
		}

		String message = props.getProperty(PRODUCTION_USER_MESSAGE_KEY);
		if (message.contains(REQUEST_ID_FIELD)) {
			message = message.replaceAll(REQUEST_ID_FIELD, requestId.toString());
		}

		if (message.contains(SERVICE_NAME_FIELD)) {
			message = message.replaceAll(SERVICE_NAME_FIELD, serviceName);
		}
		message = message.concat("\n").concat(props.getProperty(FOOTER_KEY));

		boolean res = sendMail(host, from, recipients, subject, message);
		log.debug("transferToProductionUserNotify() returns: {}", res);
		return res;
	}

	public static boolean authoritiesApproveProductionTransferNotify(String approvalLink, String serviceName, String recipient, Properties props) {
		log.debug("authoritiesApproveProductionTransferNotify(approvalLink: {}, serviceName: {}, recipient: {}, props: {})",
				approvalLink, serviceName, recipient, props);
		String host = props.getProperty(HOST_KEY);
		String from = props.getProperty(FROM_KEY);

		String subject = props.getProperty(PRODUCTION_AUTHORITIES_SUBJECT_KEY);
		if (subject.contains(SERVICE_NAME_FIELD)) {
			subject = subject.replaceAll(SERVICE_NAME_FIELD, serviceName);
		}

		String message = props.getProperty(PRODUCTION_AUTHORITIES_MESSAGE_KEY);
		if (message.contains(SERVICE_NAME_FIELD)) {
			message = message.replaceAll(SERVICE_NAME_FIELD, serviceName);
		}
		if (message.contains(APPROVAL_LINK_FIELD)) {
			message = message.replaceAll(APPROVAL_LINK_FIELD, approvalLink);
		}
		message = message.concat("\n").concat(props.getProperty(FOOTER_KEY));

		boolean res = sendMail(host, from, Collections.singletonList(recipient), subject, message);
		log.debug("authoritiesApproveProductionTransferNotify() returns: {}", res);
		return res;
	}

	public static boolean requestApprovalAdminNotify(Long userId, Long requestId, Properties props) {
		log.debug("requestApprovalAdminNotify(userId: {}, requestId: {}, props: {})", userId, requestId, props);
		String host = props.getProperty(HOST_KEY);
		String from = props.getProperty(FROM_KEY);

		String subject = props.getProperty(APPROVAL_SUBJECT_KEY);
		if (subject.contains(REQUEST_ID_FIELD)) {
			subject = subject.replaceAll(REQUEST_ID_FIELD, requestId.toString());
		}

		String message = props.getProperty(APPROVAL_MESSAGE_KEY);
		if (message.contains(REQUEST_ID_FIELD)) {
			message = message.replaceAll(REQUEST_ID_FIELD, requestId.toString());
		}
		if (message.contains(USER_INFO_FIELD)) {
			message = message.replaceAll(USER_INFO_FIELD, userId.toString());
		}
		message = message.concat("\n").concat(props.getProperty(FOOTER_KEY));
		List<String> admins = Arrays.asList(props.getProperty(ADMINS_MAILS).split(","));

		boolean res = sendMail(host, from, admins, subject, message);
		log.debug("requestApprovalAdminNotify() returns: {}", res);
		return res;
	}

	private static boolean sendMail(String host, String from, List<String> to, String subject, String msg) {
		log.debug("sendMail(host: {}, from: {}, to: {}, subject: {}, msg: {})", host, from, to, subject, msg);
		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		Session session = Session.getDefaultInstance(props);
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));

			StringJoiner recipients = new StringJoiner(",");
			for (String recipient: to) {
				recipients.add(recipient);
			}
			message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients.toString()));

			message.setSubject(subject);

			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			mimeBodyPart.setContent(msg, "text/html");

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(mimeBodyPart);

			message.setContent(multipart);

			log.debug("sending message");
			Transport.send(message);
		} catch (MessagingException e) {
			log.debug("sendMail() returns: FALSE");
		}

		log.debug("sendMail() returns: TRUE");
		return true;
	}
}
