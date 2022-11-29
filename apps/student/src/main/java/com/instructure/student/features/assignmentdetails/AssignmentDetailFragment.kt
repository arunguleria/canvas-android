/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.student.features.assignmentdetails

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Assignment.SubmissionType
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.interactions.Navigation
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.analytics.SCREEN_VIEW_ASSIGNMENT_DETAILS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.features.discussion.router.DiscussionRouterFragment
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.activity.InternalWebViewActivity
import com.instructure.student.databinding.FragmentAssignmentDetailBinding
import com.instructure.student.fragment.BasicQuizViewFragment
import com.instructure.student.fragment.LtiLaunchFragment
import com.instructure.student.fragment.StudioWebViewFragment
import com.instructure.student.mobius.assignmentDetails.submission.annnotation.AnnotationSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionMode
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.text.ui.TextSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.url.ui.UrlSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsFragment
import com.instructure.student.router.RouteMatcher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.course_module_progression.*
import kotlinx.android.synthetic.main.dialog_submission_picker.*
import kotlinx.android.synthetic.main.dialog_submission_picker_media.*
import kotlinx.android.synthetic.main.fragment_assignment_details.*

@ScreenView(SCREEN_VIEW_ASSIGNMENT_DETAILS)
@PageView(url = "{canvasContext}/assignments/{assignmentId}")
@AndroidEntryPoint
class AssignmentDetailFragment : Fragment(), Bookmarkable {

    @get:PageViewUrlParam(name = "assignmentId")
    val assignmentId by LongArg(key = Const.ASSIGNMENT_ID)
    val canvasContext by ParcelableArg<Course>(key = Const.CANVAS_CONTEXT)

    private var binding: FragmentAssignmentDetailBinding? = null
    private val viewModel: AssignmentDetailViewModel by viewModels()

    override val bookmark: Bookmarker by lazy { viewModel.bookmarker }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAssignmentDetailBinding.inflate(inflater, container, false)
        binding?.lifecycleOwner = this
        binding?.viewModel = viewModel
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupScreen()

        viewModel.events.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        }

        viewModel.data.observe(viewLifecycleOwner) {

        }
    }

    private fun setupScreen() {
        binding?.toolbar?.apply {
            setupAsBackButton {
                activity?.onBackPressed()
            }

            title = context?.getString(R.string.assignmentDetails)
            subtitle = viewModel.course?.name

            val navigation = context as? Navigation
            if (navigation?.canBookmark().orDefault(true)) {
                setMenu(R.menu.bookmark_menu) {
                    navigation?.addBookmark()
                }
            }

            ViewStyler.themeToolbarColored(requireActivity(), this, viewModel.course)
        }
    }

    private fun handleAction(action: AssignmentDetailAction) {
        val canvasContext = canvasContext as? CanvasContext ?: return

        when (action) {
            is AssignmentDetailAction.NavigateToLtiScreen -> {
                LtiLaunchFragment.routeLtiLaunchFragment(requireContext(), viewModel.course, action.url)
            }
            is AssignmentDetailAction.NavigateToSubmissionScreen -> {
                RouteMatcher.route(
                    requireContext(),
                    SubmissionDetailsFragment.makeRoute(canvasContext, assignmentId, action.isObserver)
                )
            }
            is AssignmentDetailAction.NavigateToQuizScreen -> {
                RouteMatcher.route(requireContext(), BasicQuizViewFragment.makeRoute(canvasContext, action.quiz, action.quiz.url.orEmpty()))
            }
            is AssignmentDetailAction.NavigateToDiscussionScreen -> {
                RouteMatcher.route(requireContext(), DiscussionRouterFragment.makeRoute(canvasContext, action.discussionTopicHeaderId))
            }
            is AssignmentDetailAction.NavigateByUrl -> {
                if (!RouteMatcher.canRouteInternally(requireContext(), action.url, ApiPrefs.domain, true)) {
                    val intent = Intent(requireContext(), InternalWebViewActivity::class.java) //TODO WHAT IS THIS
                    requireContext().startActivity(intent)
                }
            }
            is AssignmentDetailAction.NavigateToUploadScreen -> navigateToUploadScreen(action.assignment)
            is AssignmentDetailAction.NavigateToTextEntryScreen -> navigateToTextEntryScreen(
                action.assignmentName,
                action.submittedText,
                action.isFailure
            )
            is AssignmentDetailAction.NavigateToUrlSubmissionScreen -> navigateToUrlSubmissionScreen(
                action.assignmentName,
                action.submittedUrl,
                action.isFailure
            )
            is AssignmentDetailAction.NavigateToAnnotationSubmissionScreen -> navigateToAnnotationSubmissionScreen(action.assignment)
            is AssignmentDetailAction.NavigateToLtiLaunchScreen -> {
                RouteMatcher.route(
                    requireContext(), LtiLaunchFragment.makeRoute(
                        canvasContext,
                        action.ltiTool?.url.orEmpty(),
                        action.title,
                        isAssignmentLTI = true,
                        ltiTool = action.ltiTool
                    )
                )
            }
            is AssignmentDetailAction.ShowMediaDialog -> {
                showMediaDialog()
            }
            is AssignmentDetailAction.ShowSubmitDialog -> {
                showSubmitDialogView(action.assignment)
            }
        }
    }

    private fun navigateToTextEntryScreen(assignmentName: String?, submittedText: String? = null, isFailure: Boolean = false) {
        Analytics.logEvent(AnalyticsEventConstants.SUBMIT_TEXTENTRY_SELECTED)
        RouteMatcher.route(
            requireContext(),
            TextSubmissionUploadFragment.makeRoute(canvasContext, assignmentId, assignmentName, submittedText, isFailure)
        )
    }

    private fun navigateToUrlSubmissionScreen(assignmentName: String?, submittedUrl: String? = null, isFailure: Boolean = false) {
        Analytics.logEvent(AnalyticsEventConstants.SUBMIT_ONLINEURL_SELECTED)
        RouteMatcher.route(
            requireContext(),
            UrlSubmissionUploadFragment.makeRoute(canvasContext, assignmentId, assignmentName, submittedUrl, isFailure)
        )
    }

    private fun navigateToUploadScreen(assignment: Assignment) {
        Analytics.logEvent(AnalyticsEventConstants.SUBMIT_FILEUPLOAD_SELECTED)
        RouteMatcher.route(
            requireContext(),
            PickerSubmissionUploadFragment.makeRoute(canvasContext, assignment, PickerSubmissionMode.FileSubmission)
        )
    }

    private fun navigateToAnnotationSubmissionScreen(assignment: Assignment) {
        assignment.submission?.id?.let {
            Analytics.logEvent(AnalyticsEventConstants.SUBMIT_STUDENT_ANNOTATION_SELECTED)
            RouteMatcher.route(
                requireContext(),
                AnnotationSubmissionUploadFragment.makeRoute(
                    canvasContext,
                    assignment.annotatableAttachmentId,
                    it,
                    assignment.id,
                    assignment.name.orEmpty()
                )
            )
        }
    }

    private fun navigateToStudioScreen(assignment: Assignment) {
        viewModel.data.value?.ltiTool?.let {
            Analytics.logEvent(AnalyticsEventConstants.SUBMIT_STUDIO_SELECTED)
            RouteMatcher.route(
                requireContext(),
                StudioWebViewFragment.makeRoute(
                    canvasContext,
                    it.url.orEmpty(),
                    it.name.orEmpty(),
                    true,
                    assignment
                )
            )
        }
    }

    private fun setupDialogRow(dialog: Dialog, view: View, visibility: Boolean, onClick: () -> Unit) {
        view.setVisible(visibility)
        view.setOnClickListener {
            onClick()
            dialog.cancel()
        }
    }

    private fun showSubmitDialogView(assignment: Assignment) {
        val builder = AlertDialog.Builder(requireContext())
        val dialog = builder.setView(R.layout.dialog_submission_picker).create()
        val submissionTypes = assignment.getSubmissionTypes()

        dialog.setOnShowListener {
            setupDialogRow(dialog, dialog.submissionEntryText, submissionTypes.contains(SubmissionType.ONLINE_TEXT_ENTRY)) {
                navigateToTextEntryScreen(assignment.name)
            }
            setupDialogRow(dialog, dialog.submissionEntryWebsite, submissionTypes.contains(SubmissionType.ONLINE_URL)) {
                navigateToUrlSubmissionScreen(assignment.name)
            }
            setupDialogRow(dialog, dialog.submissionEntryFile, submissionTypes.contains(SubmissionType.ONLINE_UPLOAD)) {
                navigateToUploadScreen(assignment)
            }
            setupDialogRow(dialog, dialog.submissionEntryMedia, submissionTypes.contains(SubmissionType.MEDIA_RECORDING)) {
                showMediaDialog()
            }
            setupDialogRow(
                dialog,
                dialog.submissionEntryStudio,
                (submissionTypes.contains(SubmissionType.ONLINE_UPLOAD) && assignment.isStudioEnabled)
            ) {
                navigateToStudioScreen(assignment)
            }
            setupDialogRow(dialog, dialog.submissionEntryStudentAnnotation, submissionTypes.contains(SubmissionType.STUDENT_ANNOTATION)) {
                navigateToAnnotationSubmissionScreen(assignment)
            }
        }
        dialog.show()
    }

    private fun showMediaDialog() {
        Analytics.logEvent(AnalyticsEventConstants.SUBMIT_MEDIARECORDING_SELECTED)
        val builder = AlertDialog.Builder(requireContext())
        val dialog = builder.setView(R.layout.dialog_submission_picker_media).create()

        dialog.setOnShowListener {
            setupDialogRow(dialog, dialog.submissionEntryAudio, true) {

            }
            setupDialogRow(dialog, dialog.submissionEntryVideo, true) {

            }

            setupDialogRow(dialog, dialog.submissionEntryMediaFile, true) {

            }
        }
        dialog.show()
    }

    companion object {
        fun makeRoute(course: CanvasContext, assignmentId: Long): Route {
            val bundle = course.makeBundle { putLong(Const.ASSIGNMENT_ID, assignmentId) }
            return Route(null, AssignmentDetailFragment::class.java, course, bundle)
        }

        fun validRoute(route: Route): Boolean {
            return route.canvasContext is Course && (route.arguments.containsKey(Const.ASSIGNMENT_ID) || route.paramsHash.containsKey(RouterParams.ASSIGNMENT_ID))
        }

        fun newInstance(route: Route): AssignmentDetailFragment? {
            if (!validRoute(route)) return null

            // If routed from a URL, set the bundle's assignment ID from the url value
            if (route.paramsHash.containsKey(RouterParams.ASSIGNMENT_ID)) {
                val assignmentId = route.paramsHash[RouterParams.ASSIGNMENT_ID]?.toLong().orDefault()
                route.arguments.putLong(Const.ASSIGNMENT_ID, assignmentId)
                // Clear API cache when routing from a URL so we fetch fresh data from the network
                CanvasRestAdapter.clearCacheUrls("assignments/$assignmentId")
            }

            if (route.paramsHash.containsKey(RouterParams.SUBMISSION_ID)) {
                // Indicate that we want to route to the Submission Details page - this will give us a small backstack, allowing the user to hit back and go to Assignment Details instead
                // of closing the app (in the case of when the app isn't running and the user hits a push notification that takes them to Submission Details)
                route.arguments.putString(Const.SUBMISSION_ID, route.paramsHash[RouterParams.SUBMISSION_ID])
            }

            return AssignmentDetailFragment().withArgs(route.arguments)
        }
    }
}
