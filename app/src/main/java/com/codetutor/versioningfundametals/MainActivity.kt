package com.codetutor.versioningfundametals

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.codetutor.versioningfundametals.BuildConfig
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.codetutor.versioningfundametals.ui.theme.VersioningFundametalsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VersioningFundametalsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    VersionInfo(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun VersionInfo(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Version Name: ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Version Code: ${BuildConfig.VERSION_CODE}",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VersionInfoPreview() {
    VersioningFundametalsTheme {
        VersionInfo()
    }
}