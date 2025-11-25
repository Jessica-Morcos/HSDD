package org.hsdd.service.impl;

import org.hsdd.domain.MedicalHistoryEntry;
import org.hsdd.repo.MedicalHistoryRepository;
import org.hsdd.service.MedicalHistoryService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MedicalHistoryServiceImplTest {

    private final MedicalHistoryRepository repo = mock(MedicalHistoryRepository.class);
    private final MedicalHistoryService service = new MedicalHistoryServiceImpl(repo);

    // ------------------------------------------------------
    // 1) uploadHistoryFile() tests
    // ------------------------------------------------------

    @Test
    void uploadHistoryFile_savesParsedEntries() throws Exception {

        String text = """
                Asthma: Uses inhaler daily
                Diabetes: Chronic condition
                High BP
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file", "history.txt",
                "text/plain", text.getBytes()
        );

        service.uploadHistoryFile("12345678", file);

        ArgumentCaptor<List<MedicalHistoryEntry>> captor = ArgumentCaptor.forClass(List.class);
        verify(repo).saveAll(captor.capture());

        List<MedicalHistoryEntry> saved = captor.getValue();

        assertEquals(3, saved.size());

        assertEquals("Asthma", saved.get(0).getTitle());
        assertEquals("Uses inhaler daily", saved.get(0).getDetails());

        assertEquals("Diabetes", saved.get(1).getTitle());
        assertEquals("Chronic condition", saved.get(1).getDetails());

        assertEquals("High BP", saved.get(2).getTitle());
        assertEquals("High BP", saved.get(2).getDetails());
    }

    @Test
    void uploadHistoryFile_emptyFile_doesNotSave() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.txt",
                "text/plain", "".getBytes()
        );

        service.uploadHistoryFile("12345678", file);

        verify(repo, never()).saveAll(any());
    }

    @Test
    void uploadHistoryFile_throwsIOException() throws Exception {

        MultipartFile badFile = mock(MultipartFile.class);
        when(badFile.getBytes()).thenThrow(new IOException("fail"));

        assertThrows(IOException.class, () ->
                service.uploadHistoryFile("12345678", badFile)
        );

        verify(repo, never()).saveAll(any());
    }

    // ------------------------------------------------------
    // 2) listHistory() tests
    // ------------------------------------------------------

    @Test
    void listHistory_returnsRepoResults() {

        MedicalHistoryEntry e = new MedicalHistoryEntry();
        e.setPatientId("12345678");
        e.setTitle("Test");

        when(repo.findByPatientIdOrderByDiagnosedAtDesc("12345678"))
                .thenReturn(List.of(e));

        List<MedicalHistoryEntry> out = service.listHistory("12345678");

        assertEquals(1, out.size());
        assertEquals("Test", out.get(0).getTitle());
    }

    // ------------------------------------------------------
    // 3) save() tests
    // ------------------------------------------------------

    @Test
    void save_delegatesToRepo() {

        MedicalHistoryEntry e = new MedicalHistoryEntry();
        e.setTitle("X");

        when(repo.save(e)).thenReturn(e);

        MedicalHistoryEntry out = service.save(e);

        assertEquals("X", out.getTitle());
        verify(repo).save(e);
    }
}
