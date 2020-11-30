package cz.metacentrum.perun.spRegistration.service.impl;

import cz.metacentrum.perun.spRegistration.common.configs.ApplicationProperties;
import cz.metacentrum.perun.spRegistration.common.configs.AttributesProperties;
import cz.metacentrum.perun.spRegistration.common.models.Facility;
import cz.metacentrum.perun.spRegistration.common.models.ProvidedService;
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
	public static final String REQUEST_CANCELED = "REQUEST_CANCELED";

	// roles
	public static final String ROLE_ADMIN = "ADMIN";
	public static final String ROLE_USER = "USER";

	// template keys
	public static final String PRODUCTION_AUTHORITIES_KEY = "production_authorities";
	public static final String CLIENT_SECRET_CHANGED_KEY = "client_secret_changed";
	public static final String ADD_ADMIN_KEY = "admins_add";
	public static final String REQUEST_SIGNED_USER_KEY = "signed_user";
	public static final String REQUEST_STATUS_UPDATED_USER_KEY = "update_user";
	public static final String REQUEST_MODIFIED_USER_KEY = "status_actualized_user";
	public static final String REQUEST_CREATED_USER_KEY = "create_user";
	public static final String REQUEST_CANCEL_USER_KEY = "cancel_user";
	public static final String REQUEST_SIGNED_ADMIN_KEY = "signed_admin";
	public static final String REQUEST_STATUS_UPDATED_ADMIN_KEY = "update_admin";
	public static final String REQUEST_MODIFIED_ADMIN_KEY = "status_actualized_admin";
	public static final String REQUEST_CREATED_ADMIN_KEY = "create_admin";
	public static final String REQUEST_CANCEL_ADMIN_KEY = "cancel_admin";

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

	@NonNull private final JavaMailSender mailSender;
	@NonNull private final ApplicationProperties applicationProperties;
	@NonNull private final AttributesProperties attributesProperties;
	@NonNull private final MailProperties mailProperties;
	@NonNull private final Map<String, MailTemplate> templates;

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
		if (!mailProperties.isEnabled()) {
			return;
		}
		for (String email: authoritiesLinksMap.keySet()) {
			String link = authoritiesLinksMap.get(email);
			if (!authoritiesApproveProductionTransferNotify(link, req, email)) {
				log.warn("Failed to send approval notification to {} for req id: {}, link: {}",
						email, req.getReqId(), link);
			}
		}
	}

	@Override
	public boolean notifyNewAdmins(@NonNull ProvidedService service,
								   @NonNull Map<String, String> adminsLinksMap,
								   @NonNull User user)
	{
		if (!mailProperties.isEnabled()) {
			return true;
		}
		for (String email: adminsLinksMap.keySet()) {
			String link = adminsLinksMap.get(email);
			if (!adminAddRemoveNotify(link, service, email, user)) {
				log.warn("Failed to send approval notification to {} for facility id: {}, link: {}, user: {}",
						email, service.getFacilityId(), link, user);
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
		if (!mailProperties.isEnabled()) {
			return true;
		}

		MailTemplate template = getTemplate(PRODUCTION_AUTHORITIES_KEY);
		String message = constructMessage(template);
		String subject = constructSubject(template);

		message = replaceApprovalLink(message, approvalLink);
		message = replacePlaceholders(message, req);
		subject = replacePlaceholders(subject, req);

		return sendMail(recipient, subject, message);
	}

	@Override
	public boolean adminAddRemoveNotify(@NonNull String approvalLink, @NonNull ProvidedService service,
										@NonNull String recipient, @NonNull User user)
	{
		if (!mailProperties.isEnabled()) {
			return true;
		}

		MailTemplate template = getTemplate(ADD_ADMIN_KEY);
		String message = constructMessage(template);
		String subject = constructSubject(template);

		subject = replacePlaceholders(subject, service);

		message = replacePlaceholders(message, service);
		message = replaceApprovalLink(message, approvalLink);
		message = replacePlaceholder(message, INVITER_NAME, user.getName(), STR_EMPTY);
		message = replacePlaceholder(message, INVITER_EMAIL, user.getEmail(), STR_EMPTY);

		return sendMail(recipient, subject, message);
	}

	@Override
	public void notifyUser(@NonNull Request req, @NonNull String action) {
		if (!mailProperties.isEnabled()) {
			return;
		}
		MailTemplate template = getTemplate(action, ROLE_USER);
		
		String message = constructMessage(template);
		String subject = constructSubject(template);

		subject = replacePlaceholders(subject, req);
		message = replacePlaceholders(message, req);

		String userMail = req.getAdminContact(attributesProperties.getNames().getAdministratorContact());

		boolean sent = sendMail(userMail, subject, message);
		if (sent) {
			log.debug("Sent mail to user: {}", userMail);
		} else {
			log.warn("Failed to send notification ({}, {}) to {}", subject, message, userMail);
			//todo: reschedule this sending
		}
	}

	@Override
	public void notifyAppAdmins(@NonNull Request req, @NonNull String action) {
		if (!mailProperties.isEnabled()) {
			return;
		}
		MailTemplate template = getTemplate(action, ROLE_ADMIN);
		String subject = constructSubject(template);
		String message = constructMessage(template);

		subject = replacePlaceholders(subject, req);
		message = replacePlaceholders(message, req);

		for (String adminMail: mailProperties.getAppAdminEmails()) {
			boolean sent = sendMail(adminMail, subject, message);
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
		if (!mailProperties.isEnabled()) {
			return;
		}
		MailTemplate template = getTemplate(CLIENT_SECRET_CHANGED_KEY);
		String subject = constructSubject(template);
		String message = constructMessage(template);

		subject = replacePlaceholders(subject, facility);
		message = replacePlaceholders(message, facility);

		List<String> emails = facility.getManagers()
				.stream()
				.map(User::getEmail)
				.collect(Collectors.toList());
		int sentCount = 0;
		for (String email: emails) {
			boolean sent = sendMail(email, subject, message);
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

	private String replacePlaceholders(String containerString, ProvidedService service) {
		return replacePlaceholders(containerString, service.getName(), service.getDescription());
	}

	private String replacePlaceholders(String containerString, Facility facility) {
		return replacePlaceholders(containerString, facility.getName(), facility.getDescription());
	}

	private String replacePlaceholders(String containerString, Map<String, String> name, Map<String, String> desc) {
		containerString = replacePlaceholder(containerString, EN_SERVICE_NAME_FIELD,
				name.get(LANG_EN), STR_EMPTY);
		containerString = replacePlaceholder(containerString, EN_SERVICE_DESCRIPTION_FIELD,
				desc.get(LANG_EN), STR_EMPTY);
		if (applicationProperties.getLanguagesEnabled().contains(LANG_CS)) {
			containerString = replacePlaceholder(containerString, CS_SERVICE_NAME_FIELD,
					name.get(LANG_CS), STR_EMPTY);
			containerString = replacePlaceholder(containerString, CS_SERVICE_DESCRIPTION_FIELD,
					desc.get(LANG_CS), STR_EMPTY);
		}

		return containerString;
	}

	private String replacePlaceholders(String containerString, Request req) {
		String requestLink = applicationProperties.getHostUrl() + "/auth/requests/detail/" + req.getReqId();

		containerString = replacePlaceholder(containerString, REQUEST_ID_FIELD,
				req.getReqId().toString(), STR_EMPTY);
		containerString = replacePlaceholder(containerString, EN_NEW_STATUS_FIELD,
				req.getStatus().toString(LANG_EN), STR_EMPTY);
		containerString = replacePlaceholder(containerString, EN_SERVICE_NAME_FIELD,
				req.getFacilityName(attributesProperties.getNames().getServiceName()).get(LANG_EN), STR_EMPTY);
		containerString = replacePlaceholder(containerString, EN_SERVICE_DESCRIPTION_FIELD,
				req.getFacilityDescription(attributesProperties.getNames().getServiceDesc()).get(LANG_EN), STR_EMPTY);
		containerString = replacePlaceholder(containerString, REQUEST_DETAIL_LINK_FIELD,
				wrapInAnchorElement(requestLink), "-");
		containerString = replacePlaceholder(containerString, EN_ACTION_FIELD,
				req.getAction().toString(LANG_EN), STR_EMPTY);
		containerString = replacePlaceholder(containerString, USER_INFO_FIELD,
				req.getReqUserId().toString(), STR_EMPTY);

		if (applicationProperties.getLanguagesEnabled().contains(LANG_CS)) {
			containerString = replacePlaceholder(containerString, CS_NEW_STATUS_FIELD,
					req.getStatus().toString(LANG_CS), STR_EMPTY);
			containerString = replacePlaceholder(containerString, CS_SERVICE_NAME_FIELD,
					req.getFacilityName(attributesProperties.getNames().getServiceName()).get(LANG_CS), STR_EMPTY);
			containerString = replacePlaceholder(containerString, CS_SERVICE_DESCRIPTION_FIELD,
					req.getFacilityDescription(attributesProperties.getNames().getServiceDesc()).get(LANG_CS), STR_EMPTY);
			containerString = replacePlaceholder(containerString, CS_ACTION_FIELD,
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
			return getString(action, REQUEST_CREATED_ADMIN_KEY, REQUEST_MODIFIED_ADMIN_KEY,
					REQUEST_STATUS_UPDATED_ADMIN_KEY, REQUEST_SIGNED_ADMIN_KEY, REQUEST_CANCEL_ADMIN_KEY);
		} else if (ROLE_USER.equalsIgnoreCase(role)){
			return getString(action, REQUEST_CREATED_USER_KEY, REQUEST_MODIFIED_USER_KEY,
					REQUEST_STATUS_UPDATED_USER_KEY, REQUEST_SIGNED_USER_KEY, REQUEST_CANCEL_USER_KEY);
		}

		log.error("Cannot recognize role {}", role);
		throw new IllegalArgumentException("Unrecognized role");
	}

	private String getString(String action, String requestCreatedKey, String requestModifiedKey,
							 String requestStatusUpdatedKey, String requestSignedKey, String requestCanceledKey)
	{
		switch (action) {
			case REQUEST_CREATED:
				return requestCreatedKey;
			case REQUEST_MODIFIED:
				return requestModifiedKey;
			case REQUEST_STATUS_UPDATED:
				return requestStatusUpdatedKey;
			case REQUEST_SIGNED:
				return requestSignedKey;
			case REQUEST_CANCELED:
				return requestCanceledKey;
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
