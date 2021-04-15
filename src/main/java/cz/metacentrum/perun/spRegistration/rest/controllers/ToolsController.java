package cz.metacentrum.perun.spRegistration.rest.controllers;

import cz.metacentrum.perun.spRegistration.common.configs.AppBeansContainer;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.common.models.User;
import cz.metacentrum.perun.spRegistration.rest.ApiUtils;
import cz.metacentrum.perun.spRegistration.service.ServiceUtils;
import cz.metacentrum.perun.spRegistration.service.UtilsService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;

/**
 * Tools controller
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@RestController
@RequestMapping("/api/tools")
public class ToolsController {

	@NonNull private final AppBeansContainer appBeansContainer;
	@NonNull private final UtilsService utilsService;

	@Autowired
	public ToolsController(@NonNull AppBeansContainer appBeansContainer,
						   @NonNull UtilsService utilsService) {
		this.appBeansContainer = appBeansContainer;
		this.utilsService = utilsService;
	}

	@PostMapping(path = "/encrypt")
	public String encrypt(@NonNull  @RequestBody String toEncrypt,
						  @SessionAttribute("user") User user)
			throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException, UnauthorizedActionException
	{
		if (!utilsService.isAppAdmin(user)) {
			throw new UnauthorizedActionException("Action cannot be performed");
		}
		toEncrypt = ApiUtils.normalizeRequestBodyString(toEncrypt);

		return ServiceUtils.encrypt(toEncrypt, appBeansContainer.getSecretKeySpec());
	}

	@PostMapping(path = "/decrypt")
	public String decrypt(@NonNull @RequestBody String toDecrypt,
						  @SessionAttribute("user") User user)
			throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException, UnauthorizedActionException
	{
		if (!utilsService.isAppAdmin(user)) {
			throw new UnauthorizedActionException("Action cannot be performed");
		}
		toDecrypt = ApiUtils.normalizeRequestBodyString(toDecrypt);

		return ServiceUtils.decrypt(toDecrypt, appBeansContainer.getSecretKeySpec());
	}

}
