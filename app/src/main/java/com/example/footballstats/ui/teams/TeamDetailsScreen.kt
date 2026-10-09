package com.example.footballstats.ui.teams

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.footballstats.domain.model.Team

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailsScreen(
    team: Team,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Карточка клуба",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val shareText = """
                                ${team.name}
                                Страна: ${team.country}
                                ID команды в SStats: ${team.id}
                            """.trimIndent()

                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }

                            context.startActivity(
                                Intent.createChooser(
                                    intent,
                                    "Поделиться клубом"
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Поделиться"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        ConstraintLayout(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val (logo, name, country, information) = createRefs()

            Surface(
                modifier = Modifier
                    .size(110.dp)
                    .constrainAs(logo) {
                        top.linkTo(parent.top, margin = 8.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    TeamLogo(
                        team = team,
                        modifier = Modifier.size(72.dp)
                    )
                }
            }

            Text(
                text = team.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.constrainAs(name) {
                    top.linkTo(logo.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
            )

            Text(
                text = team.country,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.constrainAs(country) {
                    top.linkTo(name.bottom, margin = 6.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
            )

            Card(
                modifier = Modifier.constrainAs(information) {
                    top.linkTo(country.bottom, margin = 24.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom, margin = 24.dp)
                    width = Dimension.fillToConstraints
                },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Информация о команде",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text("Название: ${team.name}")
                    Text("Страна: ${team.country}")
                    Text("ID команды: ${team.id}")

                    HorizontalDivider()

                    Text(
                        text = "Источник: SStats.net",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}