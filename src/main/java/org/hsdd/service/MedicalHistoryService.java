
package org.hsdd.service;

import org.hsdd.domain.MedicalHistoryEntry;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MedicalHistoryService {

    void uploadHistoryFile(String patientId, MultipartFile file) throws IOException;
    MedicalHistoryEntry save(MedicalHistoryEntry entry);

    List<MedicalHistoryEntry> listHistory(String patientId);
}
