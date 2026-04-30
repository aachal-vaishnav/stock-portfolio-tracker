package com.portfolio.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Authentication auth) {
        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal().toString())) {
            return "redirect:/dashboard";
        }
        return "index";
    }
}
