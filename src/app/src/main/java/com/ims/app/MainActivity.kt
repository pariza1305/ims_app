package com.ims.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ims.app.navigation.IMSNavGraph
import com.ims.app.ui.attendance.AttendanceViewModel
import com.ims.app.ui.academy.AcademyViewModel
import com.ims.app.ui.dashboard.DashboardViewModel
import com.ims.app.ui.examination.ExamViewModel
import com.ims.app.ui.theme.IMSTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as IMSApplication

        setContent {
            IMSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val dashboardViewModel: DashboardViewModel = viewModel(
                        factory = DashboardViewModel.Factory(
                            app.userRepository,
                            app.studentRepository,
                            app.batchRepository,
                            app.attendanceRepository,
                            app.examRepository,
                            app.newsRepository,
                            app.notificationRepository
                        )
                    )

                    val attendanceViewModel: AttendanceViewModel = viewModel(
                        factory = AttendanceViewModel.Factory(
                            app.batchRepository,
                            app.subjectRepository,
                            app.studentRepository,
                            app.attendanceRepository,
                            app.notificationRepository
                        )
                    )

                    val examViewModel: ExamViewModel = viewModel(
                        factory = ExamViewModel.Factory(
                            app.examRepository,
                            app.batchRepository,
                            app.subjectRepository,
                            app.studentRepository
                        )
                    )

                    val notificationViewModel: com.ims.app.ui.notification.NotificationViewModel = viewModel(
                        factory = com.ims.app.ui.notification.NotificationViewModel.Factory(
                            app.notificationRepository,
                            app.attendanceRepository,
                            app.studentRepository,
                            app.batchRepository,
                            app.userRepository
                        )
                    )

                    val academyViewModel: AcademyViewModel = viewModel(
                        factory = AcademyViewModel.Factory(
                            app.courseRepository,
                            app.batchRepository,
                            app.subjectRepository,
                            app.studentRepository,
                            app.studentCategoryRepository,
                            app.graduationRepository
                        )
                    )

                    IMSNavGraph(
                        dashboardViewModel = dashboardViewModel,
                        attendanceViewModel = attendanceViewModel,
                        examViewModel = examViewModel,
                        notificationViewModel = notificationViewModel,
                        academyViewModel = academyViewModel
                    )
                }
            }
        }
    }
}
