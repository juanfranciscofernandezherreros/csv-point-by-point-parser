import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class RepositoryPolicyTest {
    @Test
    void kafkaParserAndReleaseFilesStayAligned() throws Exception {
        String pom = Files.readString(Path.of("pom.xml"));
        String readme = Files.readString(Path.of("README.md"));
        assertTrue(pom.contains("<version>${revision}${sha1}${changelist}</version>"));
        assertTrue(pom.contains("<revision>2.0.1</revision>"));
        assertFalse(pom.contains("spring-boot-starter-data-jpa"));
        assertFalse(pom.contains("postgresql"));
        assertFalse(pom.contains("flyway-core"));
        assertTrue(readme.contains("point-by-point.parsed"));
        assertTrue(Files.exists(Path.of("AGENTS.md")));
    }
}
