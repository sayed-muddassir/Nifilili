package com.nifilili.business.service.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class FlatPathStrategyTest {

    private FlatPathStrategy flatPathStrategy;

    @BeforeEach
    public void setUp() {
        flatPathStrategy = new FlatPathStrategy();
    }

    @Test
    public void generatePath_WithVersion0_ReturnFileName() {
        String result = flatPathStrategy.generatePath("registration.pdf", 0);
        assertEquals("registration.pdf", result);
    }

    @Test
    public void generatePath_WithVersion1_InsertVersionBeforeExtension() {
        String result = flatPathStrategy.generatePath("registration.pdf", 1);
        assertEquals("registration_v1.pdf", result);
    }

    @Test
    public void generatePath_WithVersion5_InsertCorrectVersionNumber() {
        String result = flatPathStrategy.generatePath("document.xlsx", 5);
        assertEquals("document_v5.xlsx", result);
    }

    @Test
    public void generatePath_FileWithoutExtension_AppendVersion() {
        String result = flatPathStrategy.generatePath("README", 1);
        assertEquals("README_v1", result);
    }

    @Test
    public void generatePath_MultipleDotsInFileName_VersionBeforeLastDot() {
        String result = flatPathStrategy.generatePath("backup.archive.tar.gz", 2);
        assertEquals("backup.archive.tar_v2.gz", result);
    }

    @Test
    public void generatePath_SingleCharacterExtension_HandleCorrectly() {
        String result = flatPathStrategy.generatePath("file.x", 1);
        assertEquals("file_v1.x", result);
    }

    @Test
    public void generatePath_FileNameEndsWithDot_VersionAfterName() {
        String result = flatPathStrategy.generatePath("config.", 1);
        assertEquals("config_v1.", result);
    }

    @Test
    public void generatePath_LargeVersionNumber_HandleCorrectly() {
        String result = flatPathStrategy.generatePath("data.json", 999);
        assertEquals("data_v999.json", result);
    }
}
