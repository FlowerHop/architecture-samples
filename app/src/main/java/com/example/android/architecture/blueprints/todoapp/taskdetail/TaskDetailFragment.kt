/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.architecture.blueprints.todoapp.taskdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.android.architecture.blueprints.todoapp.EventObserver
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.databinding.TaskdetailFragBinding
import com.example.android.architecture.blueprints.todoapp.tasks.DELETE_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.util.getViewModelFactory
import com.example.android.architecture.blueprints.todoapp.util.setupRefreshLayout
import com.example.android.architecture.blueprints.todoapp.util.setupSnackbar
import com.google.android.material.snackbar.Snackbar

/**
 * Main UI for the task detail screen.
 */
class TaskDetailFragment : Fragment() {
    private lateinit var viewDataBinding: TaskdetailFragBinding

    private val args: TaskDetailFragmentArgs by navArgs()

    private val viewModel by viewModels<TaskDetailViewModel> { getViewModelFactory() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFab()
        view.setupSnackbar(this, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
        setupNavigation()
        this.setupRefreshLayout(viewDataBinding.refreshLayout)
    }

    private fun setupNavigation() {
        viewModel.deleteTaskEvent.observe(
            this,
            EventObserver {
                val action = TaskDetailFragmentDirections
                    .actionTaskDetailFragmentToTasksFragment(DELETE_RESULT_OK)
                findNavController().navigate(action)
            }
        )
        viewModel.editTaskEvent.observe(
            this,
            EventObserver {
                val action = TaskDetailFragmentDirections
                    .actionTaskDetailFragmentToAddEditTaskFragment(
                        args.taskId,
                        resources.getString(R.string.edit_task)
                    )
                findNavController().navigate(action)
            }
        )
    }

    private fun setupFab() {
        activity?.findViewById<View>(R.id.edit_task_fab)?.setOnClickListener {
            viewModel.editTask()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.taskdetail_frag, container, false)
        viewDataBinding = TaskdetailFragBinding.bind(view)
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner

        viewModel.start(args.taskId)
        viewModel.dataLoading.observe(viewLifecycleOwner) {
            viewDataBinding.refreshLayout.isRefreshing = it
        }
        viewDataBinding.refreshLayout.setOnRefreshListener { viewModel.refresh() }
        viewModel.isDataAvailable.observe(viewLifecycleOwner) {
            viewDataBinding.noDataLayout.visibility = if (it) View.GONE else View.VISIBLE
            viewDataBinding.dataLayout.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.dataLoading.observe(viewLifecycleOwner) {
            viewDataBinding.noDataText.visibility = if (it) View.GONE else View.VISIBLE
        }
        viewModel.completed.observe(viewLifecycleOwner) {
            viewDataBinding.taskDetailCompleteCheckbox.isChecked = it
        }

        viewModel.task.observe(viewLifecycleOwner) {
            viewDataBinding.taskDetailTitleText.text = it?.title
            viewDataBinding.taskDetailDescriptionText.text = it?.description
        }

        viewDataBinding.taskDetailCompleteCheckbox.setOnClickListener { viewModel.setCompleted((it as CompoundButton).isChecked) }

        setHasOptionsMenu(true)
        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete -> {
                viewModel.deleteTask()
                true
            }
            else -> false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.taskdetail_fragment_menu, menu)
    }
}
