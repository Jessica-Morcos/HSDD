package org.hsdd.controller;

import lombok.RequiredArgsConstructor;
import org.hsdd.dto.SubmitSymptomRequest;
import org.hsdd.dto.SubmitSymptomResponse;
import org.hsdd.service.SymptomService;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class SymptomController {

    private final SymptomService symptomService;

    @PostMapping("/symptoms") // <-- plural matches your HTTP file
    public SubmitSymptomResponse submit(@RequestBody SubmitSymptomRequest req,
                                        Principal principal,
                                        HttpServletRequest http) {

        var actor = principal != null ? principal.getName() : "anonymous";
        var ip = http.getRemoteAddr();
        return symptomService.submit(req, actor, ip);
    }
}
