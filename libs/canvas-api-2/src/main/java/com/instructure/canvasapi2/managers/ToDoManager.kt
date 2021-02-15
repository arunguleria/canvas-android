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
 *
 */
package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.ToDoAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ToDo
import java.util.*
import kotlin.collections.HashSet

object ToDoManager {

    fun getUserTodos(callback: StatusCallback<List<ToDo>>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        ToDoAPI.getUserTodos(adapter, params, callback)
    }

    fun getUserTodosWithUngradedQuizzes(callback: StatusCallback<List<ToDo>>, forceNetwork: Boolean) {
        val adapter = RestBuilder(callback)
        val params = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
        ToDoAPI.getUserTodosWithUngradedQuizzes(adapter, params, callback)
    }

    fun getTodosWithUngradedQuizzes(callback: StatusCallback<List<ToDo>>, forceNetwork: Boolean) {
        getUserTodosWithUngradedQuizzes(callback, forceNetwork)
    }

    fun dismissTodo(toDo: ToDo, callback: StatusCallback<Void>) {
        val adapter = RestBuilder(callback)
        val params = RestParams()
        ToDoAPI.dismissTodo(toDo, adapter, params, callback)
    }

    fun getTodosSynchronous(canvasContext: CanvasContext, forceNetwork: Boolean): List<ToDo>? {
        return if (canvasContext.type == CanvasContext.Type.USER) {
            getUserTodosSynchronous(forceNetwork)
        } else {
            getCourseTodosSynchronous(canvasContext, forceNetwork)
        }
    }

    fun getUserTodosSynchronous(forceNetwork: Boolean): List<ToDo>? {
        val adapter = RestBuilder()
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return ToDoAPI.getUserTodosSynchronous(adapter, params)
    }

    fun getCourseTodosSynchronous(canvasContext: CanvasContext, forceNetwork: Boolean): List<ToDo>? {
        val adapter = RestBuilder()
        val params = RestParams(isForceReadFromNetwork = forceNetwork)
        return ToDoAPI.getCourseTodosSynchronous(canvasContext, adapter, params)
    }

    fun mergeToDoUpcoming(todoList: List<ToDo>?, eventList: List<ToDo>?): List<ToDo> {
        val todos = todoList ?: emptyList()
        var events = eventList ?: emptyList()

        // Add all Assignment ids from todos
        val assignmentIds =
            HashSet(todos.asSequence().filter { it.assignment != null }.map { it.assignment?.id }.toList())

        // If the set contains any assignment ids from Upcoming, it's a duplicate
        events = events.filter { it.assignment?.id ?: -1 !in assignmentIds }

        // Return combined list, sorted by date
        val defaultDate = Date(0)
        return (todos + events).sortedBy{ it.comparisonDate ?: defaultDate }
    }

}
