package cz.metacentrum.perun.spRegistration.web.controllers;

import cz.metacentrum.perun.spRegistration.common.configs.AppBeansContainer;
import cz.metacentrum.perun.spRegistration.common.exceptions.UnauthorizedActionException;
import cz.metacentrum.perun.spRegistration.service.ServiceUtils;
import cz.metacentrum.perun.spRegistration.web.ApiUtils;
import java.security.InvalidKeyException;
import java.util.Collections;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tools controller
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@RestController
@RequestMapping("/api/tools")
public class ToolsController {

	@NonNull private final AppBeansContainer appBeansContainer;

	@Autowired
	public ToolsController(@NonNull AppBeansContainer appBeansContainer) {
		this.appBeansContainer = appBeansContainer;
	}

	@PostMapping(path = "/encrypt")
	public Map<String, String> encrypt(@NonNull  @RequestBody String toEncrypt)
			throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException, UnauthorizedActionException
	{
		if (!ApiUtils.isAppAdmin()) {
			throw new UnauthorizedActionException("Action cannot be performed");
		}
		toEncrypt = ApiUtils.normalizeRequestBodyString(toEncrypt);
		String encrypted = ServiceUtils.encrypt(toEncrypt, appBeansContainer.getSecretKeySpec());
		return Collections.singletonMap("value", encrypted);
	}

	@PostMapping(path = "/decrypt")
	public Map<String, String> decrypt(@NonNull @RequestBody String toDecrypt)
			throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException, UnauthorizedActionException
	{
		if (!ApiUtils.isAppAdmin()) {
			throw new UnauthorizedActionException("Action cannot be performed");
		}
		toDecrypt = ApiUtils.normalizeRequestBodyString(toDecrypt);
		String decrypted = ServiceUtils.decrypt(toDecrypt, appBeansContainer.getSecretKeySpec());
		return Collections.singletonMap("value", decrypted);
	}

}
