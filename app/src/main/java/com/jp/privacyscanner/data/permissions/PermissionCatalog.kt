package com.jp.privacyscanner.data.permissions

import com.jp.privacyscanner.data.model.PermissionCategory
import com.jp.privacyscanner.data.model.RiskLevel

/**
 * Catálogo central que mapeia cada permissão Android conhecida para a sua
 * categoria, nível de risco intrínseco e uma explicação em linguagem simples.
 *
 * É aqui que vive o valor técnico da app: em vez de dizer apenas "permissão
 * perigosa", explicamos *o que* a permissão permite e *porquê* pode ser
 * preocupante. Manter este catálogo atualizado a cada versão do Android é
 * parte da manutenção contínua (ver relatório, secção 9).
 */
object PermissionCatalog {

    /** Metadados estáticos de uma permissão, antes de sabermos se está concedida. */
    data class Entry(
        val category: PermissionCategory,
        val riskLevel: RiskLevel,
        val explanation: String
    )

    private val DEFAULT = Entry(
        category = PermissionCategory.OTHER,
        riskLevel = RiskLevel.LOW,
        explanation = "Permissão de menor sensibilidade ou específica do sistema."
    )

    private val catalog: Map<String, Entry> = buildMap {
        // ---------------- Localização ----------------
        put("android.permission.ACCESS_FINE_LOCATION", Entry(
            PermissionCategory.LOCATION, RiskLevel.HIGH,
            "Permite saber a tua localização exata (GPS). Faz sentido em mapas, " +
                "transportes ou meteorologia. Numa app que não precise de saber onde estás, é um sinal de alerta."
        ))
        put("android.permission.ACCESS_COARSE_LOCATION", Entry(
            PermissionCategory.LOCATION, RiskLevel.MEDIUM,
            "Permite saber a tua localização aproximada (por rede/Wi-Fi). Menos precisa que o GPS, " +
                "mas ainda revela a zona onde te encontras."
        ))
        put("android.permission.ACCESS_BACKGROUND_LOCATION", Entry(
            PermissionCategory.LOCATION, RiskLevel.CRITICAL,
            "Permite seguir a tua localização mesmo com a app fechada. É das permissões mais intrusivas: " +
                "concede apenas a apps em que confias totalmente e que dependem disso (ex.: navegação)."
        ))

        // ---------------- Microfone ----------------
        put("android.permission.RECORD_AUDIO", Entry(
            PermissionCategory.MICROPHONE, RiskLevel.HIGH,
            "Permite gravar áudio pelo microfone. Normal em apps de chamadas, gravação ou assistentes de voz. " +
                "Numa app sem essas funções, questiona porque precisa de te ouvir."
        ))

        // ---------------- Câmara ----------------
        put("android.permission.CAMERA", Entry(
            PermissionCategory.CAMERA, RiskLevel.HIGH,
            "Permite tirar fotos e gravar vídeo. Esperado em câmara, videochamadas ou leitura de QR codes. " +
                "Fora disso, é um acesso poderoso que convém rever."
        ))

        // ---------------- Contactos ----------------
        put("android.permission.READ_CONTACTS", Entry(
            PermissionCategory.CONTACTS, RiskLevel.HIGH,
            "Permite ler a tua lista de contactos. Útil em mensagens ou email, mas é também um dos dados " +
                "mais recolhidos para publicidade e criação de perfis."
        ))
        put("android.permission.WRITE_CONTACTS", Entry(
            PermissionCategory.CONTACTS, RiskLevel.MEDIUM,
            "Permite adicionar ou alterar contactos no teu telemóvel."
        ))
        put("android.permission.GET_ACCOUNTS", Entry(
            PermissionCategory.CONTACTS, RiskLevel.MEDIUM,
            "Permite ver as contas configuradas no dispositivo (Google, email, etc.)."
        ))

        // ---------------- SMS / Mensagens ----------------
        put("android.permission.READ_SMS", Entry(
            PermissionCategory.SMS, RiskLevel.CRITICAL,
            "Permite ler as tuas mensagens SMS — incluindo códigos de verificação de bancos e contas. " +
                "Muito poucas apps legítimas precisam disto. Trata com máxima desconfiança."
        ))
        put("android.permission.SEND_SMS", Entry(
            PermissionCategory.SMS, RiskLevel.CRITICAL,
            "Permite enviar SMS em teu nome, o que pode gerar custos ou ser usado para fraude."
        ))
        put("android.permission.RECEIVE_SMS", Entry(
            PermissionCategory.SMS, RiskLevel.CRITICAL,
            "Permite intercetar SMS recebidos, incluindo códigos de autenticação de dois fatores."
        ))

        // ---------------- Chamadas / Telefone ----------------
        put("android.permission.READ_CALL_LOG", Entry(
            PermissionCategory.CALL_LOG, RiskLevel.HIGH,
            "Permite ver o histórico de chamadas: quem contactaste e quando."
        ))
        put("android.permission.READ_PHONE_STATE", Entry(
            PermissionCategory.PHONE, RiskLevel.MEDIUM,
            "Permite ler o estado do telefone e identificadores do dispositivo. Pode ser usada para te " +
                "identificar de forma persistente."
        ))
        put("android.permission.CALL_PHONE", Entry(
            PermissionCategory.PHONE, RiskLevel.MEDIUM,
            "Permite iniciar chamadas diretamente, sem passares pelo marcador."
        ))

        // ---------------- Armazenamento / Média ----------------
        put("android.permission.READ_EXTERNAL_STORAGE", Entry(
            PermissionCategory.STORAGE, RiskLevel.MEDIUM,
            "Permite ler ficheiros guardados no dispositivo (fotos, documentos, downloads)."
        ))
        put("android.permission.READ_MEDIA_IMAGES", Entry(
            PermissionCategory.STORAGE, RiskLevel.MEDIUM,
            "Permite aceder às tuas fotografias. Normal em galerias e editores de imagem."
        ))
        put("android.permission.READ_MEDIA_VIDEO", Entry(
            PermissionCategory.STORAGE, RiskLevel.MEDIUM,
            "Permite aceder aos teus vídeos."
        ))
        put("android.permission.READ_MEDIA_AUDIO", Entry(
            PermissionCategory.STORAGE, RiskLevel.MEDIUM,
            "Permite aceder aos teus ficheiros de áudio e música."
        ))

        // ---------------- Calendário ----------------
        put("android.permission.READ_CALENDAR", Entry(
            PermissionCategory.CALENDAR, RiskLevel.MEDIUM,
            "Permite ler os teus eventos de calendário — revela a tua rotina e compromissos."
        ))
        put("android.permission.WRITE_CALENDAR", Entry(
            PermissionCategory.CALENDAR, RiskLevel.MEDIUM,
            "Permite criar ou alterar eventos no teu calendário."
        ))

        // ---------------- Sensores / Atividade ----------------
        put("android.permission.BODY_SENSORS", Entry(
            PermissionCategory.BODY_SENSORS, RiskLevel.MEDIUM,
            "Permite ler sensores corporais como o ritmo cardíaco. Dados de saúde são sensíveis."
        ))
        put("android.permission.ACTIVITY_RECOGNITION", Entry(
            PermissionCategory.PHYSICAL_ACTIVITY, RiskLevel.LOW,
            "Permite detetar se estás a andar, a correr ou parado. Comum em apps de fitness."
        ))

        // ---------------- Dispositivos próximos ----------------
        put("android.permission.BLUETOOTH_CONNECT", Entry(
            PermissionCategory.NEARBY_DEVICES, RiskLevel.LOW,
            "Permite ligar-se a dispositivos Bluetooth emparelhados."
        ))
        put("android.permission.NEARBY_WIFI_DEVICES", Entry(
            PermissionCategory.NEARBY_DEVICES, RiskLevel.LOW,
            "Permite descobrir dispositivos Wi-Fi próximos, o que pode indiciar a tua localização."
        ))

        // ---------------- Notificações ----------------
        put("android.permission.POST_NOTIFICATIONS", Entry(
            PermissionCategory.NOTIFICATIONS, RiskLevel.LOW,
            "Permite mostrar notificações. Baixo risco de privacidade."
        ))

        // ---------------- Rede ----------------
        put("android.permission.INTERNET", Entry(
            PermissionCategory.NETWORK, RiskLevel.LOW,
            "Permite aceder à internet. Quase todas as apps a usam; por si só não é preocupante, " +
                "mas é o canal por onde os dados podem sair do dispositivo."
        ))
        put("android.permission.ACCESS_NETWORK_STATE", Entry(
            PermissionCategory.NETWORK, RiskLevel.LOW,
            "Permite ver se há ligação à internet."
        ))
    }

    /** Devolve os metadados de uma permissão, ou um valor por omissão seguro. */
    fun lookup(rawName: String): Entry = catalog[rawName] ?: DEFAULT

    /** Se a permissão consta explicitamente do catálogo (não caiu no DEFAULT). */
    fun isKnown(rawName: String): Boolean = catalog.containsKey(rawName)
}
