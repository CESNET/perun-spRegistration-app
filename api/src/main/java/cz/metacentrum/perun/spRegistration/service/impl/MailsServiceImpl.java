package cz.metacentrum.perun.spRegistration.service.impl;

import cz.metacentrum.perun.spRegistration.common.configs.ApplicationProperties;
import cz.metacentrum.perun.spRegistration.common.configs.AttributesProperties;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.Request;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.service.MailsService;
import cz.metacentrum.perun.spRegistration.service.mails.MailProperties;
import cz.metacentrum.perun.spRegistration.service.mails.MailTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Utility class for sending email notifications.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@Service("mailsService")
@Slf4j
public class MailsServiceImpl implements MailsService {

	public static final String REQUEST_CREATED = "REQUEST_CREATED";
	public static final String REQUEST_MODIFIED = "REQUEST_MODIFIED";
	public static final String REQUEST_STATUS_UPDATED = "REQUEST_STATUS_UPDATED";
	public static final String REQUEST_SIGNED = "REQUEST_SIGNED";
	
	public static final String ROLE_ADMIN = "ADMIN";
	public static final String ROLE_USER = "USER";

	private static final String PRODUCTION_AUTHORITIES_KEY = "productionAuthorities";
	private static final String CLIENT_SECRET_CHANGED_KEY = "clientSecretChanged";
	private static final String ADD_ADMIN_KEY = "adminsAdd";

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

	private final JavaMailSender mailSender;
	private final ApplicationProperties applicationProperties;
	private final AttributesProperties attributesProperties;
	private final MailProperties mailProperties;
	private final Map<String, MailTemplate> templates;

	@Autowired
	public MailsServiceImpl(JavaMailSender mailSender,
							ApplicationProperties applicationProperties,
							MailProperties mailProperties,
							AttributesProperties attributesProperties) 
	{
		this.mailSender = mailSender;
		this.applicationProperties = applicationProperties;
		this.mailProperties = mailProperties;
		this.templates = mailProperties.getTemplates();
		this.attributesProperties = attributesProperties;
	}

	@Override
	public void notifyAuthorities(Request req, Map<String, String> authoritiesLinksMap) {
		for (String email: authoritiesLinksMap.keySet()) {
			String link = authoritiesLinksMap.get(email);
			if (!this.authoritiesApproveProductionTransferNotify(link, req, email)) {
				log.warn("Failed to send approval notification to {} for req id: {}, link: {}",
						email, req.getReqId(), link);
			}
		}
	}

	@Override
	public boolean notifyNewAdmins(Facility facility, Map<String, String> adminsLinksMap, User user) {
		for (String email: adminsLinksMap.keySet()) {
			String link = adminsLinksMap.get(email);
			if (!this.adminAddRemoveNotify(link, facility, email, user)) {
				log.warn("Failed to send approval notification to {} for facility id: {}, link: {}, user: {}",
						email, facility.getId(), link, user);
			}
		}

		return true;
	}

	@Override
	public boolean authoritiesApproveProductionTransferNotify(String approvalLink, Request req, String recipient) {
		MailTemplate template = getTemplate(PRODUCTION_AUTHORITIES_KEY);
		String message = this.constructMessage(template);
		String subject = this.constructSubject(template);

		subject = this.replacePlaceholders(subject, req);
		message = this.replacePlaceholders(message, req);
		message = this.replaceApprovalLink(message, approvalLink);

		boolean res = this.sendMail(recipient, subject, message);
		log.debug("authoritiesApproveProductionTransferNotify() returns: {}", res);
		return res;
	}

	@Override
	public boolean adminAddRemoveNotify(String approvalLink, Facility facility, String recipient, User user) {
		MailTemplate template = getTemplate(ADD_ADMIN_KEY);
		String message = this.constructMessage(template);
		String subject = this.constructSubject(template);

		subject = this.replacePlaceholders(subject, facility);

		message = this.replacePlaceholders(message, facility);
		message = this.replacePlaceholder(message, INVITER_NAME, user.getName(), "");
		message = this.replacePlaceholder(message, INVITER_EMAIL, user.getEmail(), "");
		message = this.replaceApprovalLink(message, approvalLink);

		boolean res = this.sendMail(recipient, subject, message);
		log.debug("authoritiesApproveProductionTransferNotify() returns: {}", res);
		return res;
	}

	@Override
	public void notifyUser(Request req, String action) {
		MailTemplate template = getTemplate(action, ROLE_USER);
		
		String message = this.constructMessage(template);
		String subject = this.constructSubject(template);

		subject = this.replacePlaceholders(subject, req);
		message = this.replacePlaceholders(message, req);

		String userMail = req.getAdminContact(attributesProperties.getAdministratorContactAttrName());

		boolean res = this.sendMail(userMail, subject, message);
		if (!res) {
			log.warn("Failed to send notification ({}, {}) to {}", subject, message, userMail);
		}
	}

	@Override
	public void notifyAppAdmins(Request req, String action) {
		MailTemplate template = this.getTemplate(action, ROLE_ADMIN);
		String subject = this.constructSubject(template);
		String message = this.constructMessage(template);

		subject = this.replacePlaceholders(subject, req);
		message = this.replacePlaceholders(message, req);

		for (String adminMail: mailProperties.getAppAdminEmails()) {
			if (this.sendMail(adminMail, subject, message)) {
				log.trace("Sent mail to admin: {}", adminMail);
			} else {
				log.warn("Failed to send admin notification to: {}", adminMail);
			}
		}
	}

	@Override
	public void notifyClientSecretChanged(Facility facility) {
		log.debug("notifyClientSecretChanged(facility: {})", facility);
		MailTemplate template = this.getTemplate(CLIENT_SECRET_CHANGED_KEY);
		String subject = this.constructSubject(template);
		String message = this.constructMessage(template);

		subject = this.replacePlaceholders(subject, facility);
		message = this.replacePlaceholders(message, facility);

		List<String> emails = facility.getAdmins()
				.stream()
				.map(User::getEmail)
				.collect(Collectors.toList());
		int sent = 0;
		for (String email: emails) {
			if (sendMail(email, subject, message)) {
				sent++;
			};
		}

		log.debug("notifyClientSecretChanged() has sent {} notifications out of {}", sent, emails.size());
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
		containerString = this.replacePlaceholder(containerString, EN_SERVICE_NAME_FIELD,
				fac.getName().get("en"), "");
		containerString = this.replacePlaceholder(containerString, EN_SERVICE_DESCRIPTION_FIELD,
				fac.getDescription().get("en"), "");
		if (applicationProperties.getLanguagesEnabled().contains("cs")) {
			containerString = this.replacePlaceholder(containerString, CS_SERVICE_NAME_FIELD,
					fac.getName().get("cs"), "");
			containerString = this.replacePlaceholder(containerString, CS_SERVICE_DESCRIPTION_FIELD,
					fac.getDescription().get("cs"), "");
		}

		return containerString;
	}

	private String replacePlaceholders(String containerString, Request req) {
		log.trace("replacePlaceholders({}, {})", containerString, req);
		String requestLink = applicationProperties.getHostUrl() + "/auth/requests/detail/" + req.getReqId();

		containerString = this.replacePlaceholder(containerString, REQUEST_ID_FIELD,
				req.getReqId().toString(), "");
		containerString = this.replacePlaceholder(containerString, EN_NEW_STATUS_FIELD,
				req.getStatus().toString("en"), "");
		containerString = this.replacePlaceholder(containerString, EN_SERVICE_NAME_FIELD,
				req.getFacilityName(attributesProperties.getServiceNameAttrName()).get("en"), "");
		containerString = this.replacePlaceholder(containerString, EN_SERVICE_DESCRIPTION_FIELD,
				req.getFacilityDescription(attributesProperties.getServiceDescAttrName()).get("en"), "");
		containerString = this.replacePlaceholder(containerString, REQUEST_DETAIL_LINK_FIELD,
				wrapInAnchorElement(requestLink), "-");
		containerString = this.replacePlaceholder(containerString, EN_ACTION_FIELD,
				req.getAction().toString("en"), "");
		containerString = this.replacePlaceholder(containerString, USER_INFO_FIELD,
				req.getReqUserId().toString(), "");

		if (applicationProperties.getLanguagesEnabled().contains("cs")) {
			containerString = this.replacePlaceholder(containerString, CS_NEW_STATUS_FIELD,
					req.getStatus().toString("cs"), "");
			containerString = this.replacePlaceholder(containerString, CS_SERVICE_NAME_FIELD,
					req.getFacilityName(attributesProperties.getServiceNameAttrName()).get("cs"), "");
			containerString = this.replacePlaceholder(containerString, CS_SERVICE_DESCRIPTION_FIELD,
					req.getFacilityDescription(attributesProperties.getServiceDescAttrName()).get("cs"), "");
			containerString = this.replacePlaceholder(containerString, CS_ACTION_FIELD,
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

	private MailTemplate getTemplate(String action, String role) {
		String key = getMailTemplateKey(role, action);
		MailTemplate template = templates.getOrDefault(key, null);
		if (template == null) {
			log.error("Could not fetch mail template for key {} ", key);
			throw new IllegalArgumentException("Unrecognized property for mail");
		}

		return template;
	}

	private MailTemplate getTemplate(String key) {
		MailTemplate template = templates.getOrDefault(key, null);
		if (template == null) {
			log.error("Could not fetch mail template for key {} ", key);
			throw new IllegalArgumentException("Unrecognized property for mail");
		}

		return template;
	}

	private String getMailTemplateKey(String role, String action) {
		if (ROLE_ADMIN.equalsIgnoreCase(role)) {
			return getMailTemplateKeyAdmin(action);
		} else if (ROLE_USER.equalsIgnoreCase(role)){
			return getMailTemplateKeyUser(action);
		}
		
		log.error("Cannot recognize role {}", role);
		throw new IllegalArgumentException("Unrecognized role");
	}
	

	private String getMailTemplateKeyUser(String action) {
		switch (action) {
			case REQUEST_CREATED:
				return "createUser";
			case REQUEST_MODIFIED:
				return "statusActualizedUser";
			case REQUEST_STATUS_UPDATED:
				return "statusUpdatedUser";
			case REQUEST_SIGNED:
				return "signedUser";
			default:
				log.error("Unrecognized action {}", action);
				throw new IllegalArgumentException("Unrecognized action");
		}
	}

	private String getMailTemplateKeyAdmin(String action) {
		switch (action) {
			case REQUEST_CREATED:
				return "createAdmin";
			case REQUEST_MODIFIED:
				return "statusActualizedAdmin";
			case REQUEST_STATUS_UPDATED:
				return "statusUpdatedAdmin";
			case REQUEST_SIGNED:
				return "signedAdmin";
			default:
				log.error("Unrecognized action {}", action);
				throw new IllegalArgumentException("Unrecognized action");
		}
	}

	private String constructSubject(MailTemplate template) {
		StringJoiner joiner = new StringJoiner(" / ");
		for (String lang: applicationProperties.getLanguagesEnabled()) {
			String subj = template.getSubjectInLang(lang);
			if (subj != null && !NULL_KEY.equals(subj)) {
				joiner.add(subj);
			}
		}

		return mailProperties.getSubjectPrefix() + joiner.toString();
	}

	private String constructMessage(MailTemplate template) {
		StringJoiner joiner = new StringJoiner("<br/><br/><hr/><br/>");
		for (String lang: applicationProperties.getLanguagesEnabled()) {
			String msg = template.getMessageInLang(lang);
			if (msg != null && !NULL_KEY.equals(msg)) {
				joiner.add(msg);
			}
		}
		joiner.add(mailProperties.getFooter());
		return joiner.toString();
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
			helper.setFrom(mailProperties.getFrom());
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

}
