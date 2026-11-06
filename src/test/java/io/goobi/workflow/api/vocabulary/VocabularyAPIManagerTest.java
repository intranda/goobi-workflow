package io.goobi.workflow.api.vocabulary;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.goobi.vocabulary.monitoring.MonitoringResult;
import io.goobi.vocabulary.monitoring.VersionInformation;
import io.goobi.vocabulary.monitoring.VersionsCollection;

public class VocabularyAPIManagerTest {
    @Test
    public void retrieveAllAPIs() {
        VocabularyAPIManager.getInstance().fieldTypes();
        VocabularyAPIManager.getInstance().vocabularies();
        VocabularyAPIManager.getInstance().vocabularySchemas();
        VocabularyAPIManager.getInstance().vocabularyRecords();
        VocabularyAPIManager.getInstance().languages();
    }

    private static MonitoringResult resultWithVersion(String version) {
        return new MonitoringResult(null, new VersionsCollection(new VersionInformation(version, null)), null, null);
    }

    @Test
    public void versionCheckSkipsUnfilteredMavenPlaceholder() {
        assertDoesNotThrow(() -> VocabularyAPIManager.checkVersion(resultWithVersion("@project.version@")));
    }

    @ParameterizedTest
    @ValueSource(strings = { "unknown", "", "1.2", "1.2.x", "1.2.3.4", "v1.2.8", "99999999999.0.0" })
    public void versionCheckSkipsUnparseableVersions(String version) {
        assertDoesNotThrow(() -> VocabularyAPIManager.checkVersion(resultWithVersion(version)));
    }

    @Test
    public void versionCheckSkipsMissingVersionInformation() {
        assertDoesNotThrow(() -> VocabularyAPIManager.checkVersion(null));
        assertDoesNotThrow(() -> VocabularyAPIManager.checkVersion(new MonitoringResult(null, null, null, null)));
        assertDoesNotThrow(() -> VocabularyAPIManager.checkVersion(new MonitoringResult(null, new VersionsCollection(null), null, null)));
        assertDoesNotThrow(() -> VocabularyAPIManager.checkVersion(resultWithVersion(null)));
    }

    @ParameterizedTest
    @ValueSource(strings = { "1.1.9", "1.1.10", "1.2.0", "2.0.0", "1.2.8 \n", "1.1.10-SNAPSHOT", "1.1.10-rc1", "1.2.0-SNAPSHOT", "1.2.0-rc1",
            "01.02.00" })
    public void versionCheckAcceptsSufficientVersions(String version) {
        assertDoesNotThrow(() -> VocabularyAPIManager.checkVersion(resultWithVersion(version)));
    }

    @ParameterizedTest
    @ValueSource(strings = { "1.1.8", "1.0.99", "0.9.9", "1.1.9-SNAPSHOT", "1.1.9-rc1" })
    public void versionCheckRejectsTooOldVersions(String version) {
        assertThrows(IllegalStateException.class, () -> VocabularyAPIManager.checkVersion(resultWithVersion(version)));
    }
}
