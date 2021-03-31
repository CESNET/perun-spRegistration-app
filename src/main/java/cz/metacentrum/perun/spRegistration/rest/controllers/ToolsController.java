package cz.metacentrum.perun.spRegistration.rest.controllers;

import cz.metacentrum.perun.spRegistration.common.configs.AppBeansContainer;
import cz.metacentrum.perun.spRegistration.rest.ApiUtils;
import cz.metacentrum.perun.spRegistration.service.ServiceUtils;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.util.Collections;
import java.util.Map;

/**
 * Tools controller
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@RestController
public class ToolsController {

	@NonNull private final AppBeansContainer appBeansContainer;

	@Autowired
	public ToolsController(@NonNull AppBeansContainer appBeansContainer) {
		this.appBeansContainer = appBeansContainer;
	}

	@PostMapping(path = "/api/tools/encrypt")
	public Map<String, String> encrypt(@NonNull  @RequestBody String toEncrypt)
			throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException
	{

		toEncrypt = ApiUtils.normalizeRequestBodyString(toEncrypt);

		String val = ServiceUtils.encrypt(toEncrypt, appBeansContainer.getSecretKeySpec());
		return Collections.singletonMap("value", val);
	}

	@PostMapping(path = "/api/tools/decrypt")
	public Map<String, String> decrypt(@NonNull @RequestBody String toDecrypt)
			throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException
	{

		toDecrypt = ApiUtils.normalizeRequestBodyString(toDecrypt);

		String val = ServiceUtils.decrypt(toDecrypt, appBeansContainer.getSecretKeySpec());
		return Collections.singletonMap("value", val);
	}

}
