package cz.metacentrum.perun.spRegistration.service.impl;

import cz.metacentrum.perun.spRegistration.common.configs.ApplicationProperties;
import cz.metacentrum.perun.spRegistration.common.configs.AttributesProperties;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.Request;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.service.MailsService;
import cz.metacentrum.perun.spRegistration.service.mails.MailProperties;
import cz.metacentrum.perun.spRegistration.service.mails.MailTemplate;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Utility class for sending email notifications.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>;
 */
@Service("mailsService")
@Slf4j
public class MailsServiceImpl implements MailsService {

	public static final String LANG_EN = "en";
	public static final String LANG_CS = "cs";

	// actions
	public static final String REQUEST_CREATED = "REQUEST_CREATED";
	public static final String REQUEST_MODIFIED = "REQUEST_MODIFIED";
	public static final String REQUEST_STATUS_UPDATED = "REQUEST_STATUS_UPDATED";
	public static final String REQUEST_SIGNED = "REQUEST_SIGNED";

	// roles
	public static final String ROLE_ADMIN = "ADMIN";
	public static final String ROLE_USER = "USER";

	// template keys
	public static final String PRODUCTION_AUTHORITIES_KEY = "productionAuthorities";
	public static final String CLIENT_SECRET_CHANGED_KEY = "clientSecretChanged";
	public static final String ADD_ADMIN_KEY = "adminsAdd";
	public static final String REQUEST_SIGNED_USER_KEY = "signedUser";
	public static final String REQUEST_STATUS_UPDATED_USER_KEY = "statusUpdatedUser";
	public static final String REQUEST_MODIFIED_USER_KEY = "statusActualizedUser";
	public static final String REQUEST_CREATED_USER_KEY = "createUser";
	public static final String REQUEST_SIGNED_ADMIN_KEY = "signedAdmin";
	public static final String REQUEST_STATUS_UPDATED_ADMIN_KEY = "statusUpdatedAdmin";
	public static final String REQUEST_MODIFIED_ADMIN_KEY = "statusActualizedAdmin";
	public static final String REQUEST_CREATED_ADMIN_KEY = "createAdmin";

	// placeholders
	public static final String REQUEST_ID_FIELD = "%REQUEST_ID%";
	public static final String EN_NEW_STATUS_FIELD = "%EN_NEW_STATUS%";
	public static final String CS_NEW_STATUS_FIELD = "%CS_NEW_STATUS%";
	public static final String EN_SERVICE_NAME_FIELD = "%EN_SERVICE_NAME%";
	public static final String CS_SERVICE_NAME_FIELD = "%CS_SERVICE_NAME%";
	public static final String EN_SERVICE_DESCRIPTION_FIELD = "%EN_SERVICE_DESCRIPTION%";
	public static final String CS_SERVICE_DESCRIPTION_FIELD = "%CS_SERVICE_DESCRIPTION%";
	public static final String APPROVAL_LINK_FIELD = "%APPROVAL_LINK%";
	public static final String REQUEST_DETAIL_LINK_FIELD = "%REQUEST_DETAIL_LINK%";
	public static final String EN_ACTION_FIELD = "%EN_ACTION%";
	public static final String CS_ACTION_FIELD = "%CS_ACTION%";
	public static final String USER_INFO_FIELD = "%USER_INFO%";
	public static final String INVITER_NAME = "%INVITER_NAME%";
	public static final String INVITER_EMAIL = "%INVITER_EMAIL%";
	public static final String NULL_KEY = "@null";
	public static final String STR_EMPTY = "";

	private final JavaMailSender mailSender;
	private final ApplicationProperties applicationProperties;
	private final AttributesProperties attributesProperties;
	private final MailProperties mailProperties;
	private final Map<String, MailTemplate> templates;

	@Autowired
	public MailsServiceImpl(@NonNull JavaMailSender mailSender,
							@NonNull ApplicationProperties applicationProperties,
							@NonNull MailProperties mailProperties,
							@NonNull AttributesProperties attributesProperties)
	{
		this.mailSender = mailSender;
		this.applicationProperties = applicationProperties;
		this.mailProperties = mailProperties;
		this.templates = mailProperties.getTemplates();
		this.attributesProperties = attributesProperties;
	}

	@Override
	public void notifyAuthorities(@NonNull Request req,
								  @NonNull Map<String, String> authoritiesLinksMap)
	{
		for (String email: authoritiesLinksMap.keySet()) {
			String link = authoritiesLinksMap.get(email);
			if (!this.authoritiesApproveProductionTransferNotify(link, req, email)) {
				log.warn("Failed to send approval notification to {} for req id: {}, link: {}",
						email, req.getReqId(), link);
			}
		}
	}

	@Override
	public boolean notifyNewAdmins(@NonNull Facility facility,
								   @NonNull Map<String, String> adminsLinksMap,
								   @NonNull User user)
	{
		for (String email: adminsLinksMap.keySet()) {
			String link = adminsLinksMap.get(email);
			if (!this.adminAddRemoveNotify(link, facility, email, user)) {
				log.warn("Failed to send approval notification to {} for facility id: {}, link: {}, user: {}",
						email, facility.getId(), link, user);
				//todo: reschedule this sending
			}
		}

		return true;
	}

	@Override
	public boolean authoritiesApproveProductionTransferNotify(@NonNull String approvalLink,
															  @NonNull Request req,
															  @NonNull String recipient)
	{
		MailTemplate template = getTemplate(PRODUCTION_AUTHORITIES_KEY);
		String message = this.constructMessage(template);
		String subject = this.constructSubject(template);

		subject = this.replacePlaceholders(subject, req);
		message = this.replacePlaceholders(message, req);
		message = this.replaceApprovalLink(message, approvalLink);

		return this.sendMail(recipient, subject, message);
	}

	@Override
	public boolean adminAddRemoveNotify(@NonNull String approvalLink, @NonNull Facility facility,
										@NonNull String recipient, @NonNull User user)
	{
		MailTemplate template = getTemplate(ADD_ADMIN_KEY);
		String message = this.constructMessage(template);
		String subject = this.constructSubject(template);

		subject = this.replacePlaceholders(subject, facility);

		message = this.replacePlaceholders(message, facility);
		message = this.replacePlaceholder(message, INVITER_NAME, user.getName(), STR_EMPTY);
		message = this.replacePlaceholder(message, INVITER_EMAIL, user.getEmail(), STR_EMPTY);
		message = this.replaceApprovalLink(message, approvalLink);

		return this.sendMail(recipient, subject, message);
	}

	@Override
	public void notifyUser(@NonNull Request req, @NonNull String action) {
		MailTemplate template = getTemplate(action, ROLE_USER);
		
		String message = this.constructMessage(template);
		String subject = this.constructSubject(template);

		subject = this.replacePlaceholders(subject, req);
		message = this.replacePlaceholders(message, req);

		String userMail = req.getAdminContact(attributesProperties.getNames().getAdministratorContact());

		boolean sent = this.sendMail(userMail, subject, message);
		if (sent) {
			log.debug("Sent mail to user: {}", userMail);
		} else {
			log.warn("Failed to send notification ({}, {}) to {}", subject, message, userMail);
			//todo: reschedule this sending
		}
	}

	@Override
	public void notifyAppAdmins(@NonNull Request req, @NonNull String action) {
		MailTemplate template = this.getTemplate(action, ROLE_ADMIN);
		String subject = this.constructSubject(template);
		String message = this.constructMessage(template);

		subject = this.replacePlaceholders(subject, req);
		message = this.replacePlaceholders(message, req);

		for (String adminMail: mailProperties.getAppAdminEmails()) {
			boolean sent = this.sendMail(adminMail, subject, message);
			if (sent) {
				log.debug("Sent mail to admin: {}", adminMail);
			} else {
				log.warn("Failed to send admin notification to: {}", adminMail);
				//todo: reschedule this sending
			}
		}
	}

	@Override
	public void notifyClientSecretChanged(@NonNull Facility facility) {
		MailTemplate template = this.getTemplate(CLIENT_SECRET_CHANGED_KEY);
		String subject = this.constructSubject(template);
		String message = this.constructMessage(template);

		subject = this.replacePlaceholders(subject, facility);
		message = this.replacePlaceholders(message, facility);

		List<String> emails = facility.getAdmins()
				.stream()
				.map(User::getEmail)
				.collect(Collectors.toList());
		int sentCount = 0;
		for (String email: emails) {
			boolean sent = this.sendMail(email, subject, message);
			if (sent) {
				log.debug("Sent mail to admin: {}", email);
			} else {
				log.warn("Failed to send client secret changed notification to: {}", email);
				//todo: reschedule this sending
			}
		}

		log.debug("notifyClientSecretChanged() has sent {} notifications out of {}", sentCount, emails.size());
	}

	// private methods

	private String replaceApprovalLink(String containerString, String link) {
		if (containerString.contains(APPROVAL_LINK_FIELD)) {
			return containerString.replaceAll(APPROVAL_LINK_FIELD, wrapInAnchorElement(link));
		}

		return containerString;
	}

	private String replacePlaceholders(String containerString, Facility fac) {
		containerString = this.replacePlaceholder(containerString, EN_SERVICE_NAME_FIELD,
				fac.getName().get(LANG_EN), STR_EMPTY);
		containerString = this.replacePlaceholder(containerString, EN_SERVICE_DESCRIPTION_FIELD,
				fac.getDescription().get(LANG_EN), STR_EMPTY);
		if (applicationProperties.getLanguagesEnabled().contains(LANG_CS)) {
			containerString = this.replacePlaceholder(containerString, CS_SERVICE_NAME_FIELD,
					fac.getName().get(LANG_CS), STR_EMPTY);
			containerString = this.replacePlaceholder(containerString, CS_SERVICE_DESCRIPTION_FIELD,
					fac.getDescription().get(LANG_CS), STR_EMPTY);
		}

		return containerString;
	}

	private String replacePlaceholders(String containerString, Request req) {
		String requestLink = applicationProperties.getHostUrl() + "/auth/requests/detail/" + req.getReqId();

		containerString = this.replacePlaceholder(containerString, REQUEST_ID_FIELD,
				req.getReqId().toString(), STR_EMPTY);
		containerString = this.replacePlaceholder(containerString, EN_NEW_STATUS_FIELD,
				req.getStatus().toString(LANG_EN), STR_EMPTY);
		containerString = this.replacePlaceholder(containerString, EN_SERVICE_NAME_FIELD,
				req.getFacilityName(attributesProperties.getNames().getServiceName()).get(LANG_EN), STR_EMPTY);
		containerString = this.replacePlaceholder(containerString, EN_SERVICE_DESCRIPTION_FIELD,
				req.getFacilityDescription(attributesProperties.getNames().getServiceDesc()).get(LANG_EN), STR_EMPTY);
		containerString = this.replacePlaceholder(containerString, REQUEST_DETAIL_LINK_FIELD,
				wrapInAnchorElement(requestLink), "-");
		containerString = this.replacePlaceholder(containerString, EN_ACTION_FIELD,
				req.getAction().toString(LANG_EN), STR_EMPTY);
		containerString = this.replacePlaceholder(containerString, USER_INFO_FIELD,
				req.getReqUserId().toString(), STR_EMPTY);

		if (applicationProperties.getLanguagesEnabled().contains(LANG_CS)) {
			containerString = this.replacePlaceholder(containerString, CS_NEW_STATUS_FIELD,
					req.getStatus().toString(LANG_CS), STR_EMPTY);
			containerString = this.replacePlaceholder(containerString, CS_SERVICE_NAME_FIELD,
					req.getFacilityName(attributesProperties.getNames().getServiceName()).get(LANG_CS), STR_EMPTY);
			containerString = this.replacePlaceholder(containerString, CS_SERVICE_DESCRIPTION_FIELD,
					req.getFacilityDescription(attributesProperties.getNames().getServiceDesc()).get(LANG_CS), STR_EMPTY);
			containerString = this.replacePlaceholder(containerString, CS_ACTION_FIELD,
					req.getAction().toString(LANG_CS), STR_EMPTY);
		}

		return containerString;
	}

	private String wrapInAnchorElement(String link) {
		return "<a href=\"" + link + "\">" + link + "</a>";
	}

	private String replacePlaceholder(String container, String replaceKey, String replaceWith, String def) {
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
			switch (action) {
				case REQUEST_CREATED:
					return REQUEST_CREATED_ADMIN_KEY;
				case REQUEST_MODIFIED:
					return REQUEST_MODIFIED_ADMIN_KEY;
				case REQUEST_STATUS_UPDATED:
					return REQUEST_STATUS_UPDATED_ADMIN_KEY;
				case REQUEST_SIGNED:
					return REQUEST_SIGNED_ADMIN_KEY;
				default:
					log.error("Unrecognized action {}", action);
					throw new IllegalArgumentException("Unrecognized action");
			}
		} else if (ROLE_USER.equalsIgnoreCase(role)){
			switch (action) {
				case REQUEST_CREATED:
					return REQUEST_CREATED_USER_KEY;
				case REQUEST_MODIFIED:
					return REQUEST_MODIFIED_USER_KEY;
				case REQUEST_STATUS_UPDATED:
					return REQUEST_STATUS_UPDATED_USER_KEY;
				case REQUEST_SIGNED:
					return REQUEST_SIGNED_USER_KEY;
				default:
					log.error("Unrecognized action {}", action);
					throw new IllegalArgumentException("Unrecognized action");
			}
		}

		log.error("Cannot recognize role {}", role);
		throw new IllegalArgumentException("Unrecognized role");
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
		try {
			MimeMessage message = mailSender.createMimeMessage();

			MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8.toString());
			helper.setFrom(mailProperties.getFrom());
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(msg, true);

			mailSender.send(message);
		} catch (MessagingException e) {
			return false;
		}

		return true;
	}

}
