package com.sukasrana.notesapp.view.presentation.notes

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.sukasrana.notesapp.data.local.entity.NotesEntity
import com.sukasrana.notesapp.ui.theme.NotesAppTheme
import com.sukasrana.notesapp.utils.Converter.changeMillisToDateString
import com.sukasrana.notesapp.utils.ViewModelFactory
import com.sukasrana.notesapp.view.presentation.notes.component.NotesDatePicker
import com.sukasrana.notesapp.view.presentation.notes.component.TopAppBarNotes
import java.time.Instant

@SuppressLint("NewApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    id: Int,
    navController: NavController,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current
) {
    val notesViewModel: NotesViewModel =
        viewModel(factory = ViewModelFactory.getInstance(context = context))
    val state by notesViewModel.state.collectAsStateWithLifecycle()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Instant.now().toEpochMilli()
    )

    LaunchedEffect(id) {
        notesViewModel.onEvent(NotesEvent.OnGetNotesById(id))
    }

    NotesDatePicker(
        state = datePickerState,
        isOpen = state.isDatePickerDialogOpen,
        onDismissRequest = { notesViewModel.isDatePickerDialogClosed() },
        onConfirmButtonClicked = {
            notesViewModel.onEvent(NotesEvent.OnDateChange(datePickerState.selectedDateMillis))
            notesViewModel.isDatePickerDialogClosed()
        }
    )

    NotesContent(
        isTaskExist = state.currentNoteskId != null,
        onBackClick = { navController.navigateUp() },
        onDeleteClick = {
            state.currentNoteskId?.let { notesViewModel.deleteNotes(it) }
            navController.navigateUp()
        },
        title = state.title,
        onTitleChange = { notesViewModel.onEvent(NotesEvent.OnTitleChange(it)) },
        description = state.description,
        onDescriptionChange = { notesViewModel.onEvent(NotesEvent.OnDescriptionChange(it)) },
        dueDate = state.dueDate,
        isDatePickerDialogOpen = { notesViewModel.isDatePickerDialogOpen() },
        onSaveClick = {
            val notes = NotesEntity(
                notesId = state.currentNoteskId,
                title = state.title,
                description = state.description,
                dueDate = state.dueDate ?: Instant.now().toEpochMilli()
            )

            if (state.title.isNotEmpty() && state.description.isNotEmpty() && state.dueDate != null) {
                notesViewModel.saveNotes(notes)
                navController.navigateUp()
            } else
                Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
        },
        modifier = modifier
    )
}

@Composable
fun NotesContent(
    isTaskExist: Boolean,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    dueDate: Long?,
    isDatePickerDialogOpen: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBarNotes(
                isTaskExist = isTaskExist,
                onBackButtonClick = onBackClick,
                onDeleteButtonClick = onDeleteClick
            )
        },
        modifier = modifier
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .verticalScroll(state = rememberScrollState())
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text(text = "Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text(text = "Description") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Due Date",
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dueDate.changeMillisToDateString(),
                    style = MaterialTheme.typography.bodyLarge
                )
                IconButton(onClick = isDatePickerDialogOpen) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select Due Date"
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onSaveClick,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = "Save", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Preview
@Composable
private fun PreviewNotesScreen() {
    NotesAppTheme {
        NotesScreen(id = 1, navController = rememberNavController())
    }

}
