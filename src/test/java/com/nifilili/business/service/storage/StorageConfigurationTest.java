package com.nifilili.business.service.storage;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class StorageConfigurationTest {

    @Test
    public void getAllowedExtensionsSet_ParseCommaSeparatedExtensions() {
        StorageConfiguration config = new StorageConfiguration();
        config.setAllowedExtensions(".pdf,.doc,.xlsx");

        Set<String> extensions = config.getAllowedExtensionsSet();

        assertEquals(3, extensions.size());
        assertTrue(extensions.contains(".pdf"));
        assertTrue(extensions.contains(".doc"));
        assertTrue(extensions.contains(".xlsx"));
    }

    @Test
    public void getAllowedExtensionsSet_CaseInsensitiveAndTrimsWhitespace() {
        StorageConfiguration config = new StorageConfiguration();
        config.setAllowedExtensions(" .PDF , .DOC , .XlSx ");

        Set<String> extensions = config.getAllowedExtensionsSet();

        assertEquals(3, extensions.size());
        assertTrue(extensions.contains(".pdf"));
        assertTrue(extensions.contains(".doc"));
        assertTrue(extensions.contains(".xlsx"));
    }

    @Test
    public void getAllowedExtensionsSet_HandlesMissingDot() {
        StorageConfiguration config = new StorageConfiguration();
        config.setAllowedExtensions("pdf,doc");

        Set<String> extensions = config.getAllowedExtensionsSet();

        assertEquals(2, extensions.size());
        assertTrue(extensions.contains(".pdf"));
        assertTrue(extensions.contains(".doc"));
    }

    @Test
    public void isExtensionAllowed_AllowedExtension_ReturnsTrue() {
        StorageConfiguration config = new StorageConfiguration();
        config.setAllowedExtensions(".pdf,.doc,.xlsx");

        assertTrue(config.isExtensionAllowed("document.pdf"));
        assertTrue(config.isExtensionAllowed("report.doc"));
        assertTrue(config.isExtensionAllowed("spreadsheet.xlsx"));
    }

    @Test
    public void isExtensionAllowed_DisallowedExtension_ReturnsFalse() {
        StorageConfiguration config = new StorageConfiguration();
        config.setAllowedExtensions(".pdf,.doc");

        assertFalse(config.isExtensionAllowed("script.exe"));
        assertFalse(config.isExtensionAllowed("archive.zip"));
    }

    @Test
    public void isExtensionAllowed_CaseInsensitive() {
        StorageConfiguration config = new StorageConfiguration();
        config.setAllowedExtensions(".pdf,.doc");

        assertTrue(config.isExtensionAllowed("document.PDF"));
        assertTrue(config.isExtensionAllowed("file.DoC"));
    }

    @Test
    public void isExtensionAllowed_NoExtension_ReturnsFalse() {
        StorageConfiguration config = new StorageConfiguration();
        config.setAllowedExtensions(".pdf,.doc");

        assertFalse(config.isExtensionAllowed("README"));
    }

    @Test
    public void isExtensionAllowed_NullFileName_ReturnsFalse() {
        StorageConfiguration config = new StorageConfiguration();
        config.setAllowedExtensions(".pdf,.doc");

        assertFalse(config.isExtensionAllowed(null));
    }

    @Test
    public void isFileSizeAllowed_WithinLimit_ReturnsTrue() {
        StorageConfiguration config = new StorageConfiguration();
        config.setMaxFileSize(1024L);

        assertTrue(config.isFileSizeAllowed(512L));
        assertTrue(config.isFileSizeAllowed(1024L));
    }

    @Test
    public void isFileSizeAllowed_ExceedsLimit_ReturnsFalse() {
        StorageConfiguration config = new StorageConfiguration();
        config.setMaxFileSize(1024L);

        assertFalse(config.isFileSizeAllowed(1025L));
        assertFalse(config.isFileSizeAllowed(2048L));
    }

    @Test
    public void isFileSizeAllowed_ZeroSize_ReturnsFalse() {
        StorageConfiguration config = new StorageConfiguration();
        config.setMaxFileSize(1024L);

        assertFalse(config.isFileSizeAllowed(0L));
    }

    @Test
    public void isFileSizeAllowed_NegativeSize_ReturnsFalse() {
        StorageConfiguration config = new StorageConfiguration();
        config.setMaxFileSize(1024L);

        assertFalse(config.isFileSizeAllowed(-1L));
    }
}
