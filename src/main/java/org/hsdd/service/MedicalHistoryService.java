
package org.hsdd.service;

import org.hsdd.model.MedicalHistory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MedicalHistoryService {

    void uploadHistoryFile(String patientId, MultipartFile file) throws IOException;
    MedicalHistory save(MedicalHistory entry);

    List<MedicalHistory> listHistory(String patientId);
}
