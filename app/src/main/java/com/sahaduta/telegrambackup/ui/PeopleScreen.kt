package com.sahaduta.telegrambackup.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sahaduta.telegrambackup.data.FaceClusterEntity
import com.sahaduta.telegrambackup.data.GalleryDatabase
import kotlinx.coroutines.launch
import androidx.compose.foundation.isSystemInDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen() {
    val context = LocalContext.current
    val database = remember { GalleryDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()
    
    val clusters by database.galleryDao().getAllClusters().collectAsState(initial = emptyList())
    
    var showRenameDialog by remember { mutableStateOf<FaceClusterEntity?>(null) }
    var newName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "People & Faces",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 32.dp, start = 16.dp, bottom = 16.dp)
        )

        if (clusters.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp).padding(bottom = 16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "No faces detected yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp, start = 8.dp, end = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(clusters, key = { it.clusterId }) { cluster ->
                    PersonGlassCard(cluster) {
                        showRenameDialog = cluster
                        newName = cluster.personName ?: ""
                    }
                }
            }
        }
    }

    if (showRenameDialog != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Name this person") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true,
                    placeholder = { Text("Enter name (e.g., John)") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        val updatedCluster = showRenameDialog!!.copy(personName = newName.takeIf { it.isNotBlank() })
                        database.galleryDao().updateFaceCluster(updatedCluster)
                        showRenameDialog = null
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PersonGlassCard(cluster: FaceClusterEntity, onClick: () -> Unit) {
    val context = LocalContext.current
    val database = remember { GalleryDatabase.getDatabase(context) }
    
    var displayPhotoUrl by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(cluster.clusterId) {
        val embeddings = database.galleryDao().getEmbeddingsForCluster(cluster.clusterId)
        if (embeddings.isNotEmpty()) {
            // Ideally we get the media associated with the first embedding
        }
    }

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.92f else 1f, label = "scale")
    
    val isDark = isSystemInDarkTheme()
    val glassColor = if (isDark) Color(0x33FFFFFF) else Color(0x99FFFFFF)
    val borderColor = if (isDark) Color(0x22FFFFFF) else Color(0x44FFFFFF)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(glassColor, glassColor.copy(alpha = 0.1f))
                    )
                )
                .blur(radius = 8.dp, edgeTreatment = androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded)
                .background(borderColor)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.Gray.copy(alpha=0.2f))) {
                if (displayPhotoUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(displayPhotoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = cluster.personName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Person",
                        tint = if (isDark) Color.LightGray else Color.DarkGray,
                        modifier = Modifier.align(Alignment.Center).size(48.dp)
                    )
                }
            }
        }
        Text(
            text = cluster.personName ?: "Unnamed",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
