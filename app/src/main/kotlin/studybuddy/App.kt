/*
 * StudyBuddy - Your Academic Task Companion
 * 
 * A Kotlin application to help students organize their academic tasks
 * and build productive study habits.
 */
package studybuddy

import studybuddy.cli.StudyBuddyCLI

fun main() {
    val cli = StudyBuddyCLI()
    cli.start()
}
