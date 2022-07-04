package cz.metacentrum.perun.spRegistration.web.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
public class RouteToAngular {

    @RequestMapping(value = { "/", "/auth/**"})
    public String error() {
        return "forward:/index.html";
    }

}
