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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringJoiner;

/**
 * Utility class for sending email notifications.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@SuppressWarnings("Duplicates")
public class Mails {

	private static final Logger log = LoggerFactory.getLogger(Mails.class);

	private static final String NEW_LINE = "<br/>";

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
	private static final String PRODUCTION_AUTHORITIES_MESSAGE_KEY = "production.authorities.message";
	private static final String PRODUCTION_AUTHORITIES_SUBJECT_KEY = "production.authorities.subject";
	private static final String ADD_ADMIN_SUBJECT_KEY = "admins.add.subject";
	private static final String ADD_ADMIN_MESSAGE_KEY = "admins.add.message";
	private static final String FOOTER_KEY = "footer";

	private static final String REQUEST_ID_FIELD = "%REQUEST_ID%";
	private static final String NEW_STATUS_FIELD = "%NEW_STATUS%";
	private static final String SERVICE_NAME_FIELD = "%SERVICE_NAME%";
	private static final String APPROVAL_LINK_FIELD = "%APPROVAL_LINK%";
	private static final String USER_INFO_FIELD = "%USER_INFO%";

	public static boolean requestStatusUpdateUserNotify(Long requestId, RequestStatus status, String administratorContact,
														Properties props) {
		return requestStatusUpdateUserNotify(requestId, status, administratorContact, null, props);
	}

	public static boolean requestStatusUpdateUserNotify(Long requestId, RequestStatus status, String administratorContact,
														String additionalMessage, Properties props) {
		log.debug("requestStatusUpdateUserNotify(requestId: {}, status: {}, administratorContact: {}, additionalMessage: {}, props: {})",
				requestId, status, administratorContact, additionalMessage, props);
		String host = props.getProperty(HOST_KEY);
		String from = props.getProperty(FROM_KEY);

		String subject = props.getProperty(UPDATE_SUBJECT_KEY);
		Map<String, String> subjectMap = new HashMap<>();
		subjectMap.put(REQUEST_ID_FIELD, requestId.toString());
		subject = getSubstitutedMessage(subject, subjectMap);

		String message = props.getProperty(UPDATE_MESSAGE_KEY);
		Map<String, String> messageMap = new HashMap<>();
		messageMap.put(REQUEST_ID_FIELD, requestId.toString());
		messageMap.put(NEW_STATUS_FIELD, status.toString());
		message = getSubstitutedMessage(message, messageMap);

		if (additionalMessage != null && !additionalMessage.isEmpty()) {
			message = message.concat(NEW_LINE).concat(additionalMessage);
		}

		message = message.concat(NEW_LINE).concat(props.getProperty(FOOTER_KEY));

		boolean res = sendMail(host, from, Collections.singletonList(administratorContact), subject, message);
		log.debug("requestStatusUpdateUserNotify() returns: {}", res);
		return res;
	}

	public static boolean userCreateRequestNotify(Long requestId, String serviceName, String recipient, Properties props) {
		log.debug("userCreateRequestNotify(requestId: {}, serviceName: {}, recipient: {}, props: {})",
				requestId, serviceName, recipient, props);
		String host = props.getProperty(HOST_KEY);
		String from = props.getProperty(FROM_KEY);

		String subject = props.getProperty(CREATE_SUBJECT_KEY);
		Map<String, String> subjectMap = new HashMap<>();
		subjectMap.put(REQUEST_ID_FIELD, requestId.toString());
		subject = getSubstitutedMessage(subject, subjectMap);

		String message = props.getProperty(CREATE_MESSAGE_KEY);
		Map<String, String> messageMap = new HashMap<>();
		messageMap.put(REQUEST_ID_FIELD, requestId.toString());
		messageMap.put(SERVICE_NAME_FIELD, serviceName);
		message = getSubstitutedMessage(message, messageMap);

		message = message.concat(NEW_LINE).concat(props.getProperty(FOOTER_KEY));

		boolean res = sendMail(host, from, Collections.singletonList(recipient), subject, message);
		log.debug("userCreateRequestNotify() returns: {}", res);
		return res;
	}

	public static boolean transferToProductionUserNotify(Long requestId, String serviceName, String recipient, Properties props) {
		log.debug("transferToProductionUserNotify(requestId: {}, serviceName: {}, recipient: {}, props: {})",
				requestId, serviceName, recipient, props);
		String host = props.getProperty(HOST_KEY);
		String from = props.getProperty(FROM_KEY);

		String subject = props.getProperty(PRODUCTION_USER_SUBJECT_KEY);
		Map<String, String> subjectMap = new HashMap<>();
		subjectMap.put(REQUEST_ID_FIELD, requestId.toString());
		subject = getSubstitutedMessage(subject, subjectMap);

		String message = props.getProperty(PRODUCTION_USER_MESSAGE_KEY);
		Map<String, String> messageMap = new HashMap<>();
		messageMap.put(REQUEST_ID_FIELD, requestId.toString());
		messageMap.put(SERVICE_NAME_FIELD, serviceName);
		message = getSubstitutedMessage(message, messageMap);

		message = message.concat(NEW_LINE).concat(props.getProperty(FOOTER_KEY));

		boolean res = sendMail(host, from, Collections.singletonList(recipient), subject, message);
		log.debug("transferToProductionUserNotify() returns: {}", res);
		return res;
	}

	public static boolean authoritiesApproveProductionTransferNotify(String approvalLink, String serviceName, String recipient, Properties props) {
		log.debug("authoritiesApproveProductionTransferNotify(approvalLink: {}, serviceName: {}, recipient: {}, props: {})",
				approvalLink, serviceName, recipient, props);
		String host = props.getProperty(HOST_KEY);
		String from = props.getProperty(FROM_KEY);

		String subject = props.getProperty(PRODUCTION_AUTHORITIES_SUBJECT_KEY);
		Map<String, String> subjectMap = new HashMap<>();
		subjectMap.put(SERVICE_NAME_FIELD, serviceName);
		subject = getSubstitutedMessage(subject, subjectMap);

		String message = props.getProperty(PRODUCTION_AUTHORITIES_MESSAGE_KEY);
		Map<String, String> messageMap = new HashMap<>();
		messageMap.put(SERVICE_NAME_FIELD, serviceName);
		messageMap.put(APPROVAL_LINK_FIELD, approvalLink);
		message = getSubstitutedMessage(message, messageMap);

		message = message.concat(NEW_LINE).concat(props.getProperty(FOOTER_KEY));

		boolean res = sendMail(host, from, Collections.singletonList(recipient), subject, message);
		log.debug("authoritiesApproveProductionTransferNotify() returns: {}", res);
		return res;
	}

	public static boolean requestApprovalAdminNotify(Long userId, Long requestId, Properties props) {
		log.debug("requestApprovalAdminNotify(userId: {}, requestId: {}, props: {})", userId, requestId, props);
		String host = props.getProperty(HOST_KEY);
		String from = props.getProperty(FROM_KEY);

		String subject = props.getProperty(APPROVAL_SUBJECT_KEY);
		Map<String, String> subjectMap = new HashMap<>();
		subjectMap.put(REQUEST_ID_FIELD, requestId.toString());
		subject = getSubstitutedMessage(subject, subjectMap);

		String message = props.getProperty(APPROVAL_MESSAGE_KEY);
		Map<String, String> messageMap = new HashMap<>();
		messageMap.put(REQUEST_ID_FIELD, requestId.toString());
		messageMap.put(USER_INFO_FIELD, userId.toString());
		message = getSubstitutedMessage(message, messageMap);

		message = message.concat(NEW_LINE).concat(props.getProperty(FOOTER_KEY));
		List<String> admins = Arrays.asList(props.getProperty(ADMINS_MAILS).split(","));

		boolean res = sendMail(host, from, admins, subject, message);
		log.debug("requestApprovalAdminNotify() returns: {}", res);
		return res;
	}

	public static boolean adminAddRemoveNotify(String approvalLink, String serviceName, String recipient, Properties props) {
		log.debug("adminAddRemoveNotify(approvalLink: {}, recipient: {}, props: {})",
				approvalLink, recipient, props);
		String host = props.getProperty(HOST_KEY);
		String from = props.getProperty(FROM_KEY);

		String subject = props.getProperty(ADD_ADMIN_SUBJECT_KEY);

		String message = props.getProperty(ADD_ADMIN_MESSAGE_KEY);
		Map<String, String> messageMap = new HashMap<>();
		messageMap.put(SERVICE_NAME_FIELD, serviceName);
		messageMap.put(APPROVAL_LINK_FIELD, approvalLink);
		message = getSubstitutedMessage(message, messageMap);

		message = message.concat(NEW_LINE).concat(props.getProperty(FOOTER_KEY));

		boolean res = sendMail(host, from, Collections.singletonList(recipient), subject, message);
		log.debug("adminAddRemoveNotify() returns: {}", res);
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
			message.setContent( msg, "text/html; charset=utf-8" );

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
			return false;
		}

		log.debug("sendMail() returns: TRUE");
		return true;
	}

	private static String getSubstitutedMessage(String message, Map<String, String> map) {
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (message.contains(entry.getKey())) {
				message = message.replaceAll(entry.getKey(), entry.getValue());
			}
		}
		return message;
	}
}
