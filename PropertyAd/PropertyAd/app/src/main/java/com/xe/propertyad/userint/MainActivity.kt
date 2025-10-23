package com.xe.propertyad.userint

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xe.propertyad.data.Suggestion





class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PropertyAdForm()
        }
    }
}

class AdViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdViewModel::class.java)) {
            return AdViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyAdForm(
    vm: AdViewModel = viewModel(factory = AdViewModelFactory(LocalContext.current))
) {
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var jsonOutput by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val suggestions by vm.suggestions.collectAsState()
    val locationValid by vm.selectedLocationValid.collectAsState()

    // 💙 Background
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Create Property Ad",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )


            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *") },
                modifier = Modifier.fillMaxWidth()
            )


            OutlinedTextField(
                value = location,
                onValueChange = {
                    location = it
                    vm.onLocationChanged(it)
                    expanded = it.isNotBlank()
                },
                label = { Text("Location *") },
                modifier = Modifier.fillMaxWidth()
            )


            if (expanded && suggestions.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    items(suggestions) { suggestion: Suggestion ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    location = suggestion.mainText
                                    vm.validateLocation(suggestion.mainText)
                                    expanded = false
                                }
                                .padding(8.dp)
                        ) {
                            Text(
                                text = suggestion.mainText,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = suggestion.secondaryText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }


            if (!locationValid && location.isNotBlank()) {
                Text(
                    text = "Please select a valid location",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }


            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            //teleia anti gia comma
            OutlinedTextField(
                value = price,
                onValueChange = { input ->
                    if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*\$"))) {
                        price = input
                    }
                },
                label = { Text("Price (optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                )

            )


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Submit button
                Button(
                    onClick = {
                        val ad = PropertyAd(
                            title = title,
                            location = location,
                            description = description.ifEmpty { null },
                            price = price.ifEmpty { null }
                        )
                        jsonOutput = vm.buildJson(ad)
                        showDialog = true
                    },
                    enabled = title.isNotBlank() && locationValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Submit")
                }


                Button(
                    onClick = {
                        title = ""
                        location = ""
                        description = ""
                        price = ""
                        expanded = false
                        vm.onLocationChanged("")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }
        }

        // JSON popup
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("OK")
                    }
                },
                title = { Text("Submitted JSON") },
                text = { Text(jsonOutput) }
            )
        }
    }
}

