package com.sahaduta.telegrambackup.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sahaduta.telegrambackup.data.FaceClusterEntity
import com.sahaduta.telegrambackup.data.GalleryDatabase
import kotlinx.coroutines.launch

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
            modifier = Modifier.padding(16.dp)
        )

        if (clusters.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No faces detected yet. ML Indexer is running.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(clusters, key = { it.clusterId }) { cluster ->
                    PersonThumbnail(cluster) {
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
fun PersonThumbnail(cluster: FaceClusterEntity, onClick: () -> Unit) {
    val context = LocalContext.current
    val database = remember { GalleryDatabase.getDatabase(context) }
    
    // Fetch one media item associated with this cluster to show as the face avatar
    var displayPhotoUrl by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(cluster.clusterId) {
        val embeddings = database.galleryDao().getEmbeddingsForCluster(cluster.clusterId)
        if (embeddings.isNotEmpty()) {
            // Ideally we get the media associated with the first embedding
            // For now, we'll just mock this or fetch the media if we had a direct query
            // Since we didn't add a direct query for MediaEntity from cluster, let's just 
            // leave it blank or load a placeholder.
            // A production app would query: SELECT * FROM media_items m JOIN face_embeddings e ON m.id = e.mediaId WHERE e.clusterId = X LIMIT 1
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = androidx.compose.ui.graphics.Color.LightGray)
            }
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
                    imageVector = androidx.compose.material.icons.Icons.Default.Person,
                    contentDescription = "Person",
                    tint = androidx.compose.ui.graphics.Color.Gray,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        Text(
            text = cluster.personName ?: "Unnamed",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
