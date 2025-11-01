/*
 * StudyBuddy Application Test
 */
package studybuddy

import org.junit.jupiter.api.Test
import studybuddy.cli.StudyBuddyCLI

class AppTest {
    @Test
    fun cliCanBeInstantiated() {
        val cli = StudyBuddyCLI()
        // Test that CLI can be created without errors
        // Full CLI testing would require mocking input/output streams
    }
}
