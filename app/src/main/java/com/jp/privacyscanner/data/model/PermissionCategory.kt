package com.jp.privacyscanner.data.model

/**
 * Categorias de permissões agrupadas por tipo de dado sensível a que dão
 * acesso. Cada categoria tem um peso base que alimenta o motor de scoring.
 * O peso reflete o impacto na privacidade caso a permissão seja concedida.
 */
enum class PermissionCategory(val label: String, val baseWeight: Int) {
    LOCATION("Localização", 10),
    MICROPHONE("Microfone", 10),
    CAMERA("Câmara", 9),
    CONTACTS("Contactos", 8),
    SMS("SMS / Mensagens", 10),
    CALL_LOG("Registo de chamadas", 9),
    PHONE("Telefone", 7),
    STORAGE("Ficheiros e média", 6),
    CALENDAR("Calendário", 5),
    BODY_SENSORS("Sensores corporais", 6),
    PHYSICAL_ACTIVITY("Atividade física", 4),
    NEARBY_DEVICES("Dispositivos próximos", 4),
    NOTIFICATIONS("Notificações", 2),
    NETWORK("Rede / Internet", 1),
    OTHER("Outras", 1);
}
