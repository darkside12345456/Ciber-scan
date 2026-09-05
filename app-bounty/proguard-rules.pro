# Regras de ofuscação para a build de release.
# Room e Compose já trazem regras consumer; mantemos os modelos de dados.
-keep class com.jp.privacyscanner.data.model.** { *; }
