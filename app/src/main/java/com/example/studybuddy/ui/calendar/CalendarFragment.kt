package com.example.studybuddy.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studybuddy.data.Task
import com.example.studybuddy.databinding.CalendarDayLayoutBinding
import com.example.studybuddy.databinding.FragmentCalendarBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.DayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CalendarViewModel by viewModels()
    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    private val selectionFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
    private val tasks = mutableListOf<Task>()
    private val taskAdapter = TaskAdapter(tasks)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tasksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)
        val endMonth = currentMonth.plusMonths(100)
        val firstDayOfWeek = firstDayOfWeekFromLocale()

        binding.calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        binding.calendarView.scrollToMonth(currentMonth)

        binding.calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.textView
                val dotView = container.dotView
                textView.text = day.date.dayOfMonth.toString()

                if (day.position == DayPosition.MonthDate) {
                    textView.visibility = View.VISIBLE
                    if (day.date == selectedDate) {
                        textView.setBackgroundResource(com.example.studybuddy.R.drawable.selected_day_bg)
                    } else {
                        textView.background = null
                    }

                    val tasksForDate = viewModel.tasks.value?.filter { it.date == day.date } ?: emptyList()
                    dotView.isVisible = tasksForDate.isNotEmpty()
                } else {
                    textView.visibility = View.INVISIBLE
                    dotView.visibility = View.INVISIBLE
                }
            }
        }

        binding.calendarView.monthScrollListener = {
            binding.monthYearText.text = monthTitleFormatter.format(it.yearMonth)
        }

        binding.nextMonthButton.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let {
                binding.calendarView.smoothScrollToMonth(it.yearMonth.next)
            }
        }

        binding.previousMonthButton.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let {
                binding.calendarView.smoothScrollToMonth(it.yearMonth.previous)
            }
        }

        viewModel.tasks.observe(viewLifecycleOwner) {
            updateAdapterForDate(selectedDate ?: today)
            binding.calendarView.notifyCalendarChanged()
        }
    }

    private fun updateAdapterForDate(date: LocalDate) {
        val selectedTasks = viewModel.tasks.value?.filter { it.date == date } ?: emptyList()
        tasks.clear()
        tasks.addAll(selectedTasks)
        taskAdapter.notifyDataSetChanged()
        binding.selectedDateText.text = selectionFormatter.format(date)
    }

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        lateinit var day: CalendarDay
        val textView: TextView = CalendarDayLayoutBinding.bind(view).calendarDayText
        val dotView: View = CalendarDayLayoutBinding.bind(view).dotView

        init {
            view.setOnClickListener {
                if (day.position == DayPosition.MonthDate) {
                    if (selectedDate != day.date) {
                        val oldDate = selectedDate
                        selectedDate = day.date
                        binding.calendarView.notifyDateChanged(day.date)
                        oldDate?.let { binding.calendarView.notifyDateChanged(it) }
                        updateAdapterForDate(day.date)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}