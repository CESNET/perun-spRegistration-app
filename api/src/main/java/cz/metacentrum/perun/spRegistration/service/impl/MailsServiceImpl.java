package cz.metacentrum.perun.spRegistration.service.impl;

import cz.metacentrum.perun.spRegistration.common.configs.AppConfig;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.Request;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.service.MailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Utility class for sending email notifications.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@Service("mailsService")
public class MailsServiceImpl implements MailsService {

	private static final Logger log = LoggerFactory.getLogger(MailsServiceImpl.class);

	public static final String REQUEST_CREATED = "REQUEST_CREATED";
	public static final String REQUEST_MODIFIED = "REQUEST_MODIFIED";
	public static final String REQUEST_STATUS_UPDATED = "REQUEST_STATUS_UPDATED";
	public static final String REQUEST_SIGNED = "REQUEST_SIGNED";

	private static final String PRODUCTION_AUTHORITIES_MESSAGE_KEY = "production.authorities.message";
	private static final String PRODUCTION_AUTHORITIES_SUBJECT_KEY = "production.authorities.subject";

	private static final String CLIENT_SECRET_CHANGED_MESSAGE_KEY = "client.secret.changed.message";
	private static final String CLIENT_SECRET_CHANGED_SUBJECT_KEY = "client.secret.changed.subject";

	private static final String ADD_ADMIN_SUBJECT_KEY = "admins.add.subject";
	private static final String ADD_ADMIN_MESSAGE_KEY = "admins.add.message";

	private static final String REQUEST_ID_FIELD = "%REQUEST_ID%";
	private static final String EN_NEW_STATUS_FIELD = "%EN_NEW_STATUS%";
	private static final String CS_NEW_STATUS_FIELD = "%CS_NEW_STATUS%";
	private static final String EN_SERVICE_NAME_FIELD = "%EN_SERVICE_NAME%";
	private static final String CS_SERVICE_NAME_FIELD = "%CS_SERVICE_NAME%";
	private static final String EN_SERVICE_DESCRIPTION_FIELD = "%EN_SERVICE_DESCRIPTION%";
	private static final String CS_SERVICE_DESCRIPTION_FIELD = "%CS_SERVICE_DESCRIPTION%";
	private static final String APPROVAL_LINK_FIELD = "%APPROVAL_LINK%";
	private static final String REQUEST_DETAIL_LINK_FIELD = "%REQUEST_DETAIL_LINK%";
	private static final String EN_ACTION_FIELD = "%EN_ACTION%";
	private static final String CS_ACTION_FIELD = "%CS_ACTION%";
	private static final String USER_INFO_FIELD = "%USER_INFO%";
	private static final String INVITER_NAME = "%INVITER_NAME%";
	private static final String INVITER_EMAIL = "%INVITER_EMAIL%";
	private static final String NULL_KEY = "@null";

	@Value("${host.url}")
	private String hostUrl;

	@Value("${mail.from}")
	private String from;

	@Value("${mail.subject.prefix}")
	private String subjectPrefix;

	@Value("${mail.footer}")
	private String footer;

	@Value("#{'${mail.app.admins.mails}'.split(',')}")
	private List<String> appAdminEmails;

	private final JavaMailSender mailSender;
	private final Properties messagesProperties;
	private final AppConfig appConfig;

	@Autowired
	public MailsServiceImpl(JavaMailSender mailSender, Properties messagesProperties, AppConfig appConfig) {
		this.mailSender = mailSender;
		this.messagesProperties = messagesProperties;
		this.appConfig = appConfig;
	}

	@Override
	public void notifyAuthorities(Request req, Map<String, String> authoritiesLinksMap) {
		for (String email: authoritiesLinksMap.keySet()) {
			String link = authoritiesLinksMap.get(email);
			if (!authoritiesApproveProductionTransferNotify(link, req, email)) {
				log.warn("Failed to send approval notification to {} for req id: {}, link: {}",
						email, req.getReqId(), link);
			}
		}
	}

	@Override
	public boolean notifyNewAdmins(Facility facility, Map<String, String> adminsLinksMap, User user) {
		for (String email: adminsLinksMap.keySet()) {
			String link = adminsLinksMap.get(email);
			if (!adminAddRemoveNotify(link, facility, email, user)) {
				log.warn("Failed to send approval notification to {} for facility id: {}, link: {}, user: {}",
						email, facility.getId(), link, user);
			}
		}

		return true;
	}

	@Override
	public boolean authoritiesApproveProductionTransferNotify(String approvalLink, Request req, String recipient) {
		log.debug("authoritiesApproveProductionTransferNotify(approvalLink: {}, req: {}, recipient: {})",
				approvalLink, req, recipient);

		StringJoiner subject = new StringJoiner(" / ");
		for (String lang : appConfig.getAvailableLanguages()) {
			String subj = messagesProperties.getProperty(PRODUCTION_AUTHORITIES_SUBJECT_KEY + '.' + lang);
			if (subj != null && !NULL_KEY.equals(subj)) {
				subject.add(subj);
			}
		}

		StringJoiner message = new StringJoiner("<br/><hr/><br/>");
		for (String lang : appConfig.getAvailableLanguages()) {
			String msg = messagesProperties.getProperty(PRODUCTION_AUTHORITIES_MESSAGE_KEY + '.' + lang);
			if (msg != null && ! NULL_KEY.equals(msg)) {
				message.add(msg);
			}
		}
		message.add(footer);

		String mailSubject = subjectPrefix + subject.toString();
		mailSubject = replacePlaceholders(mailSubject, req);

		String mailMessage = message.toString();
		mailMessage = replacePlaceholders(mailMessage, req);
		mailMessage = replaceApprovalLink(mailMessage, approvalLink);

		boolean res = sendMail(recipient, mailSubject, mailMessage);
		log.debug("authoritiesApproveProductionTransferNotify() returns: {}", res);
		return res;
	}

	@Override
	public boolean adminAddRemoveNotify(String approvalLink, Facility facility, String recipient, User user) {
		log.debug("authoritiesApproveProductionTransferNotify(approvalLink: {}, facility: {}, recipient: {})",
				approvalLink, facility, recipient);

		StringJoiner subject = new StringJoiner(" / ");
		for (String lang : appConfig.getAvailableLanguages()) {
			String subj = messagesProperties.getProperty(ADD_ADMIN_SUBJECT_KEY + '.' + lang);
			if (null != subj && !NULL_KEY.equals(subj)) {
				subject.add(subj);
			}
		}

		StringJoiner message = new StringJoiner("<br/><hr/><br/>");
		for (String lang : appConfig.getAvailableLanguages()) {
			String msg = messagesProperties.getProperty(ADD_ADMIN_MESSAGE_KEY + '.' + lang);
			if (null != msg && !NULL_KEY.equals(msg)) {
				message.add(msg);
			}
		}
		message.add(footer);

		String mailSubject = subjectPrefix + subject.toString();
		mailSubject = replacePlaceholders(mailSubject, facility);

		String mailMessage = message.toString();
		mailMessage = replacePlaceholders(mailMessage, facility);
		mailMessage = replacePlaceholder(mailMessage, INVITER_NAME, user.getName(), "");
		mailMessage = replacePlaceholder(mailMessage, INVITER_EMAIL, user.getEmail(), "");
		mailMessage = replaceApprovalLink(mailMessage, approvalLink);

		boolean res = sendMail(recipient, mailSubject, mailMessage);
		log.debug("authoritiesApproveProductionTransferNotify() returns: {}", res);
		return res;
	}

	private boolean sendMail(String to, String subject, String msg) {
		log.debug("sendMail(to: {}, subject: {}, msg: {})", to, subject, msg);
		if (to == null) {
			log.error("Could not send mail, to == null");
			return false;
		}

		try {
			MimeMessage message = mailSender.createMimeMessage();

			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom(from);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(msg, true);

			log.debug("sending message");
			mailSender.send(message);
		} catch (MessagingException e) {
			log.debug("sendMail() returns: FALSE");
			return false;
		}

		log.debug("sendMail() returns: TRUE");
		return true;
	}

	@Override
	public void notifyUser(Request req, String action) {
		String subject = getSubject(action, "USER");
		String message = getMessage(action, "USER");

		subject = replacePlaceholders(subject, req);
		message = replacePlaceholders(message, req);

		String userMail = req.getAdminContact(appConfig.getAdminsAttributeName());

		boolean res = sendMail(userMail, subject, message);
		if (!res) {
			log.warn("Failed to send notification ({}, {}) to {}", subject, message, userMail);
		}
	}

	@Override
	public void notifyAppAdmins(Request req, String action) {
		String subject = getSubject(action, "ADMIN");
		String message = getMessage(action, "ADMIN");

		subject = replacePlaceholders(subject, req);
		message = replacePlaceholders(message, req);

		for (String adminMail: appAdminEmails) {
			if (sendMail(adminMail, subject, message)) {
				log.trace("Sent mail to admin: {}", adminMail);
			} else {
				log.warn("Failed to send admin notification to: {}", adminMail);
			}
		}
	}

	private String replaceApprovalLink(String containerString, String link) {
		log.trace("replaceApprovalLink({}, {})", containerString, link);
		if (containerString.contains(APPROVAL_LINK_FIELD)) {
			return containerString.replaceAll(APPROVAL_LINK_FIELD, wrapInAnchorElement(link));
		}

		return containerString;
	}

	private String replacePlaceholders(String containerString, Facility fac) {
		log.trace("replacePlaceholders({}, {})", containerString, fac);
		containerString = replacePlaceholder(containerString, EN_SERVICE_NAME_FIELD,
				fac.getName().get("en"), "");
		containerString = replacePlaceholder(containerString, EN_SERVICE_DESCRIPTION_FIELD,
				fac.getDescription().get("en"), "");
		if (appConfig.getAvailableLanguages().contains("cs")) {
			containerString = replacePlaceholder(containerString, CS_SERVICE_NAME_FIELD,
					fac.getName().get("cs"), "");
			containerString = replacePlaceholder(containerString, CS_SERVICE_DESCRIPTION_FIELD,
					fac.getDescription().get("cs"), "");
		}

		return containerString;
	}

	private String replacePlaceholders(String containerString, Request req) {
		log.trace("replacePlaceholders({}, {})", containerString, req);
		String requestLink = hostUrl + "/auth/requests/detail/" + req.getReqId();

		containerString = replacePlaceholder(containerString, REQUEST_ID_FIELD,
				req.getReqId().toString(), "");
		containerString = replacePlaceholder(containerString, EN_NEW_STATUS_FIELD,
				req.getStatus().toString("en"), "");
		containerString = replacePlaceholder(containerString, EN_SERVICE_NAME_FIELD,
				req.getFacilityName(appConfig.getServiceNameAttributeName()).get("en"), "");
		containerString = replacePlaceholder(containerString, EN_SERVICE_DESCRIPTION_FIELD,
				req.getFacilityDescription(appConfig.getServiceDescAttributeName()).get("en"), "");
		containerString = replacePlaceholder(containerString, REQUEST_DETAIL_LINK_FIELD,
				wrapInAnchorElement(requestLink), "-");
		containerString = replacePlaceholder(containerString, EN_ACTION_FIELD,
				req.getAction().toString("en"), "");
		containerString = replacePlaceholder(containerString, USER_INFO_FIELD,
				req.getReqUserId().toString(), "");

		if (appConfig.getAvailableLanguages().contains("cs")) {
			containerString = replacePlaceholder(containerString, CS_NEW_STATUS_FIELD,
					req.getStatus().toString("cs"), "");
			containerString = replacePlaceholder(containerString, CS_SERVICE_NAME_FIELD,
					req.getFacilityName(appConfig.getServiceNameAttributeName()).get("cs"), "");
			containerString = replacePlaceholder(containerString, CS_SERVICE_DESCRIPTION_FIELD,
					req.getFacilityDescription(appConfig.getServiceDescAttributeName()).get("cs"), "");
			containerString = replacePlaceholder(containerString, CS_ACTION_FIELD,
					req.getAction().toString("cs"), "");
		}

		return containerString;
	}

	private String wrapInAnchorElement(String link) {
		return "<a href=\"" + link + "\">" + link + "</a>";
	}

	private String replacePlaceholder(String container, String replaceKey, String replaceWith, String def) {
		log.trace("replacePlaceholder({}, {}, {})", container, replaceKey, replaceWith);
		if (container.contains(replaceKey)) {
			if (replaceWith != null) {
				return container.replace(replaceKey, replaceWith);
			} else {
				return container.replace(replaceKey, def);
			}
		}

		return container;
	}

	private String getSubject(String action, String role) {
		log.trace("getSubject({}, {})", action, role);
		StringJoiner joiner = new StringJoiner(" / ");
		for (String lang : appConfig.getAvailableLanguages()) {
			String subj = getSingleEntry(action, role, lang, "subject");
			if (subj != null && !NULL_KEY.equals(subj)) {
				joiner.add(subj);
			}
		}

		return subjectPrefix + joiner.toString();
	}

	private String getMessage(String action, String role) {
		log.trace("getMessage({}, {})", action, role);
		StringJoiner joiner = new StringJoiner("<br/><br/><hr/><br/>");
		for (String lang : appConfig.getAvailableLanguages()) {
			String msg = getSingleEntry(action, role, lang, "message");
			if (msg != null && !NULL_KEY.equals(msg)) {
				joiner.add(msg);
			}
		}

		joiner.add(footer);

		return joiner.toString();
	}

	private String getSingleEntry(String action, String role, String lang, String type) {
		String propkey = getPropertyPrefix(action) + '.' + role.toLowerCase() + '.' + type + '.' + lang;
		return messagesProperties.getProperty(propkey);
	}

	private String getPropertyPrefix(String action) {
		switch (action) {
			case REQUEST_CREATED:
				return "create";
			case REQUEST_MODIFIED:
				return "update";
			case REQUEST_STATUS_UPDATED:
				return "status_updated";
			case REQUEST_SIGNED:
				return "signed";
			default:
				return "";
		}
	}

	@Override
	public void notifyClientSecretChanged(Facility facility) {
		log.debug("notifyClientSecretChanged(facility: {})", facility);

		StringJoiner subject = new StringJoiner(" / ");
		for (String lang : appConfig.getAvailableLanguages()) {
			String subj = messagesProperties.getProperty(CLIENT_SECRET_CHANGED_SUBJECT_KEY + '.' + lang);
			if (subj != null && ! NULL_KEY.equals(subj)) {
				subject.add(subj);
			}
		}

		StringJoiner message = new StringJoiner("<br/><hr/><br/>");
		for (String lang : appConfig.getAvailableLanguages()) {
			String msg = messagesProperties.getProperty(CLIENT_SECRET_CHANGED_MESSAGE_KEY + '.' + lang);
			if (msg != null && !NULL_KEY.equals(msg)) {
				message.add(msg);
			}
		}
		message.add(footer);

		String mailSubject = subjectPrefix + subject.toString();
		mailSubject = replacePlaceholders(mailSubject, facility);

		String mailMessage = message.toString();
		mailMessage = replacePlaceholders(mailMessage, facility);

		List<String> emails = facility.getAdmins().stream().map(User::getEmail).collect(Collectors.toList());
		int sent = 0;
		for (String email: emails) {
			if (sendMail(email, mailSubject, mailMessage)) {
				sent++;
			};
		}

		log.debug("notifyClientSecretChanged() has sent {} notifications out of {}", sent, emails.size());
	}
}
