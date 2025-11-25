
package org.hsdd.service.impl;

import org.hsdd.domain.MedicalHistoryEntry;
import org.hsdd.repo.MedicalHistoryRepository;
import org.hsdd.service.MedicalHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class MedicalHistoryServiceImpl implements MedicalHistoryService {

    private final MedicalHistoryRepository historyRepo;

    public MedicalHistoryServiceImpl(MedicalHistoryRepository historyRepo) {
        this.historyRepo = historyRepo;
    }

    @Override
    public void uploadHistoryFile(String patientId, MultipartFile file) throws IOException {
        // read whole text file
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);

        List<MedicalHistoryEntry> toSave = new ArrayList<>();

        for (String rawLine : content.split("\\R")) {   // split on any newline
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            String title;
            String details;

            int colon = line.indexOf(':');
            if (colon >= 0) {
                title = line.substring(0, colon).trim();
                details = line.substring(colon + 1).trim();
            } else {
                // no colon → use whole line as both
                title = line.length() > 100 ? line.substring(0, 100) : line;
                details = line;
            }

            MedicalHistoryEntry entry = new MedicalHistoryEntry();
            entry.setPatientId(patientId);
            entry.setTitle(title);
            entry.setDetails(details);

            toSave.add(entry);
        }

        if (!toSave.isEmpty()) {
            historyRepo.saveAll(toSave);
        }
    }

    @Override
    public List<MedicalHistoryEntry> listHistory(String patientId) {
        return historyRepo.findByPatientIdOrderByDiagnosedAtDesc(patientId);
    }
    @Override
    public MedicalHistoryEntry save(MedicalHistoryEntry entry) {
        return historyRepo.save(entry);
    }

}
