/*
 * Copyright (C) 2018 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.student.ui.interaction

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addSubmissionForAssignment
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.student.ui.utils.*
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class AssignmentListInteractionTest : StudentTest() {

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    override fun displaysPageObjects() {
        setUpData(0)
        goToAssignmentsPage()
        assignmentListPage.assertPageObjects()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun displaysNoAssignmentsView() {
        setUpData(0)
        goToAssignmentsPage()
        assignmentListPage.assertDisplaysNoAssignmentsView()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun displaysAssignment() {
        val assignment = setUpData()[0]
        goToAssignmentsPage()
        assignmentListPage.assertHasAssignment(assignment)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun sortAssignmentsByTimeByDefault() {
        val assignment = setUpData()[0]
        goToAssignmentsPage()
        assignmentListPage.assertHasAssignment(assignment)
        assignmentListPage.assertSortByButtonShowsSortByTime()
        assignmentListPage.assertFindsUndatedAssignmentLabel()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION)
    fun sortAssignmentsByTypeWhenTypeIsSelectedInTheDialog() {
        val assignment = setUpData()[0]
        goToAssignmentsPage()

        assignmentListPage.selectSortByType()

        assignmentListPage.assertHasAssignment(assignment)
        assignmentListPage.assertSortByButtonShowsSortByType()
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION, false)
    fun testLetterGradeAssignmentWithoutQuantitativeRestriction() {
        setUpData()
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.LETTER_GRADE, "B", 90.0, 100)
        goToAssignmentsPage()

        assignmentListPage.assertAssignmentDisplayedWithGrade(assignment.name!!, "90/100 (B)")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION, false)
    fun testGpaScaleAssignmentWithoutQuantitativeRestriction() {
        setUpData()
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.GPA_SCALE, "3.7", 90.0, 100)
        goToAssignmentsPage()

        assignmentListPage.assertAssignmentDisplayedWithGrade(assignment.name!!, "90/100 (3.7)")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION, false)
    fun testPointsAssignmentWithoutQuantitativeRestriction() {
        setUpData()
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.POINTS, "90", 90.0, 100)
        goToAssignmentsPage()

        assignmentListPage.assertAssignmentDisplayedWithGrade(assignment.name!!, "90/100")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION, false)
    fun testPointsAssignmentExcusedWithoutQuantitativeRestriction() {
        setUpData()
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.POINTS, null, 90.0, 100, excused = true)
        goToAssignmentsPage()

        assignmentListPage.assertAssignmentDisplayedWithGrade(assignment.name!!, "EX/100")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION, false)
    fun testPercentageAssignmentWithoutQuantitativeRestriction() {
        setUpData()
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.PERCENT, "90%", 90.0, 100)
        goToAssignmentsPage()

        assignmentListPage.assertAssignmentDisplayedWithGrade(assignment.name!!, "90%")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION, false)
    fun testPassFailAssignmentWithoutQuantitativeRestriction() {
        setUpData()
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.PASS_FAIL, "complete", 0.0, 0)
        goToAssignmentsPage()

        assignmentListPage.assertAssignmentDisplayedWithGrade(assignment.name!!, "Complete")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION, false)
    fun testLetterGradeAssignmentWithQuantitativeRestriction() {
        setUpData(restrictQuantitativeData = true)
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.LETTER_GRADE, "B", 90.0, 100)
        goToAssignmentsPage()

        assignmentListPage.assertAssignmentDisplayedWithGrade(assignment.name!!, "B")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION, false)
    fun testGpaScaleAssignmentWithQuantitativeRestriction() {
        setUpData(restrictQuantitativeData = true)
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.GPA_SCALE, "3.7", 90.0, 100)
        goToAssignmentsPage()

        assignmentListPage.assertAssignmentDisplayedWithGrade(assignment.name!!, "3.7")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION, false)
    fun testPointsAssignmentWithQuantitativeRestriction() {
        setUpData(restrictQuantitativeData = true)
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.POINTS, "90", 90.0, 100)
        goToAssignmentsPage()

        assignmentListPage.assertAssignmentDisplayedWithoutGrade(assignment.name!!)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION, false)
    fun testPointsAssignmentExcusedWithQuantitativeRestriction() {
        setUpData(restrictQuantitativeData = true)
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.POINTS, null, 90.0, 100, excused = true)
        goToAssignmentsPage()

        assignmentListPage.assertAssignmentDisplayedWithGrade(assignment.name!!, "Excused")
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION, false)
    fun testPercentageAssignmentWithQuantitativeRestriction() {
        setUpData(restrictQuantitativeData = true)
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.PERCENT, "90%", 90.0, 100)
        goToAssignmentsPage()

        assignmentListPage.assertAssignmentDisplayedWithoutGrade(assignment.name!!)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ASSIGNMENTS, TestCategory.INTERACTION, false)
    fun testPassFailAssignmentWithQuantitativeRestriction() {
        setUpData(restrictQuantitativeData = true)
        val assignment = addAssignment(MockCanvas.data, Assignment.GradingType.PASS_FAIL, "complete", 0.0, 0)
        goToAssignmentsPage()

        assignmentListPage.assertAssignmentDisplayedWithGrade(assignment.name!!, "Complete")
    }

    private fun setUpData(assignmentCount: Int = 1, restrictQuantitativeData: Boolean = false): List<Assignment> {
        val data = MockCanvas.init(
            courseCount = 1,
            favoriteCourseCount = 1,
            studentCount = 1,
            teacherCount = 1
        )

        val course = data.courses.values.first()

        val newCourse = course
            .copy(settings = CourseSettings(restrictQuantitativeData = restrictQuantitativeData))
        data.courses[course.id] = newCourse

        val assignmentList = mutableListOf<Assignment>()
        repeat(assignmentCount) {
            val assignment = data.addAssignment(
                courseId = course.id,
                submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY
            )
            assignmentList.add(assignment)
        }

        return assignmentList
    }

    private fun goToAssignmentsPage() {
        val data = MockCanvas.data

        val course = data.courses.values.first()
        val student = data.students[0]

        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)
        dashboardPage.waitForRender()

        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAssignments()
    }

    private fun addAssignment(data: MockCanvas, gradingType: Assignment.GradingType, grade: String?, score: Double?, maxScore: Int, excused: Boolean = false): Assignment {
        val course = data.courses.values.first()
        val student = data.students.first()

        val assignment = data.addAssignment(
            courseId = course.id,
            submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY,
            gradingType = Assignment.gradingTypeToAPIString(gradingType) ?: "",
            pointsPossible = maxScore,
        )

        data.addSubmissionForAssignment(assignment.id, student.id, Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString, grade = grade, score = score, excused = excused)

        return assignment
    }

}

