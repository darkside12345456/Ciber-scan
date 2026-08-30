package com.jp.privacyscanner.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Ecrã de boas-vindas mostrado no primeiro arranque. Explica o que a app faz,
 * reforça o processamento local e — importante — justifica ao utilizador
 * porque precisa de ver a lista de apps instaladas. Transparência aqui aumenta
 * a confiança e reduz desinstalações.
 */
@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Icon(
            Icons.Default.Shield,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Scanner de Privacidade",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Descobre que apps acedem aos teus dados — e o que fazer quanto a isso.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(32.dp))

        FeatureRow(
            Icons.Default.Search,
            "Analisamos as tuas apps",
            "Vemos que permissões cada app tem e explicamos, sem jargão, o que cada uma permite."
        )
        FeatureRow(
            Icons.Default.CheckCircle,
            "Recomendações claras",
            "Damos-te um score de privacidade e dizemos-te exatamente o que rever ou desligar."
        )
        FeatureRow(
            Icons.Default.Lock,
            "Nada sai do teu telemóvel",
            "Toda a análise é feita localmente. Não recolhemos nem enviamos os teus dados."
        )

        Spacer(Modifier.height(24.dp))
        Text(
            "Porque pedimos acesso à lista de apps? É a única forma de analisar as permissões " +
                "instaladas. Só lemos permissões — nunca o conteúdo das outras apps.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(24.dp))

        Button(onClick = onGetStarted, modifier = Modifier.fillMaxWidth()) {
            Text("Começar")
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, title: String, body: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
