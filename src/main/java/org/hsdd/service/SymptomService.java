package org.hsdd.service;

import org.hsdd.dto.SubmitSymptomRequest;
import org.hsdd.dto.SubmitSymptomResponse;

public interface SymptomService {
    SubmitSymptomResponse submit(SubmitSymptomRequest req, String actor, String ip);
}
