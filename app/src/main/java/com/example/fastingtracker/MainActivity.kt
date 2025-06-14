package com.example.fastingtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fastingtracker.ui.theme.FastingTrackerTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.delay
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.ui.Alignment

data class FastingSession(val start: LocalDateTime, val end: LocalDateTime?)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FastingTrackerTheme {
                FastingTrackerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastingTrackerApp() {
    var sessions by remember { mutableStateOf(listOf<FastingSession>()) }
    var currentStart by remember { mutableStateOf<LocalDateTime?>(null) }
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    // Temporizador do jejum atual
    var elapsedSeconds by remember { mutableStateOf(0L) }

    LaunchedEffect(currentStart) {
        while (currentStart != null) {
            elapsedSeconds = ChronoUnit.SECONDS.between(currentStart, LocalDateTime.now())
            delay(1000)
        }
        elapsedSeconds = 0L
    }

    fun formatDuration(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60

        return buildString {
            if (h > 0) append("$h hour${if (h > 1) "s" else ""}")
            if (h > 0 && m > 0) append(" ")
            if (m > 0) append("$m min${if (m > 1) "s" else ""}")
            if (h == 0L && m == 0L) append("less than a minute")
        }
    }

    val totalFastingSeconds = sessions.fold(0L) { acc, session ->
        acc + (session.end?.let { ChronoUnit.SECONDS.between(session.start, it) } ?: 0L)
    }

    // Para edição dos logs
    var editingSessionIndex by remember { mutableStateOf<Int?>(null) }
    var editStartText by remember { mutableStateOf("") }
    var editEndText by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }

    fun openEditDialog(index: Int) {
        editingSessionIndex = index
        editStartText = sessions[index].start.format(formatter)
        editEndText = sessions[index].end?.format(formatter) ?: ""
        showEditDialog = true
    }

    fun saveEdit() {
        val index = editingSessionIndex ?: return
        try {
            val newStart = LocalDateTime.parse(editStartText, formatter)
            val newEnd = if (editEndText.isBlank()) null else LocalDateTime.parse(editEndText, formatter)

            sessions = sessions.toMutableList().also {
                it[index] = FastingSession(newStart, newEnd)
            }
            showEditDialog = false
        } catch (e: Exception) {
            // Erros
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Fasting Tracker") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            // Temporizador do jejum atual
            if (currentStart != null) {
                Text(
                    text = "Fasting in progress since: ${currentStart!!.format(formatter)}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                Button(
                    enabled = currentStart == null,
                    onClick = { currentStart = LocalDateTime.now() }
                ) {
                    Text("Start Fast")
                }
                Button(
                    enabled = currentStart != null,
                    onClick = {
                        currentStart?.let {
                            sessions = sessions + FastingSession(it, LocalDateTime.now())
                            currentStart = null
                        }
                    }
                ) {
                    Text("End Fast")
                }
            }

            // Total acumulado de jejum
            Text(
                text = "Total fasting time: ${formatDuration(totalFastingSeconds)}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Text("Fasting Logs", style = MaterialTheme.typography.titleMedium)

            if (sessions.isEmpty() && currentStart == null) {
                Text("No fasting logged yet.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    // Mostrar logs do mais recente para o mais antigo
                    itemsIndexed(sessions.asReversed()) { index, session ->
                        val originalIndex = sessions.size - 1 - index

                        val durationText = session.end?.let { end ->
                            val seconds = ChronoUnit.SECONDS.between(session.start, end)
                            formatDuration(seconds)
                        } ?: "(in progress)"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Start: ${session.start.format(formatter)} - End: ${session.end?.format(formatter) ?: "In Progress"} $durationText",
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { openEditDialog(originalIndex) }) {
                                Text("Edit")
                            }
                        }
                        Divider()
                    }
                }
            }
        }
    }
    // Opção de eliminação dos logs?
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Fast") },
            text = {
                Column {
                    TextField(
                        value = editStartText,
                        onValueChange = { editStartText = it },
                        label = { Text("Start (dd/MM/yyyy HH:mm)") }
                    )
                    TextField(
                        value = editEndText,
                        onValueChange = { editEndText = it },
                        label = { Text("End (dd/MM/yyyy HH:mm) - leave blank if in progress") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { saveEdit() }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
