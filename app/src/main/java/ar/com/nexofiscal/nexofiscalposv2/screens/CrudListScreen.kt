// src/main/java/ar/com/nexofiscal/nexofiscalposv2/screens/CrudListScreen.kt
package ar.com.nexofiscal.nexofiscalposv2.screens

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Pantalla genérica para listar objetos y permitir Editar, Seleccionar o Borrar.
 *
 * @param title       Título de la pantalla.
 * @param items       Lista de objetos a mostrar.
 * @param itemLabel   Función que obtiene el texto a mostrar de cada objeto.
 * @param onEdit      Callback cuando el usuario toca el ícono de editar.
 * @param onSelect    Callback cuando el usuario toca el ícono de seleccionar.
 * @param onDelete    Callback cuando el usuario toca el ícono de borrar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CrudListScreen(
    title: String,
    items: List<T>,
    itemLabel: (T) -> String,
    onEdit: (T) -> Unit,
    onSelect: (T) -> Unit,
    onDelete: (T) -> Unit,
    type: String = "R",//estaclece el tipo de listado R= listar, C= crear, U= actualizar
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(title) })
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay elementos para mostrar", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(items) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Texto principal
                        Text(
                            text = itemLabel(item),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSelect(item) }
                        )

                        // Ícono Seleccionar - mostrar solo en tipos R y C
                        if (type == "R") {
                            IconButton(onClick = { onSelect(item) }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Seleccionar",
                                    tint = Color.Green
                                )
                            }
                        }
                        // Ícono Editar - mostrar solo en tipos R y U
                        if ( type == "U") {
                            IconButton(onClick = { onEdit(item) }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar"
                                )
                            }
                        }
                        // Ícono Borrar - mostrar solo en tipos R y U
                        if ( type == "D") {
                            IconButton(onClick = { onDelete(item) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Borrar"
                                )
                            }
                        }
                    }
                    Divider()
                }
            }
        }
    }
}
