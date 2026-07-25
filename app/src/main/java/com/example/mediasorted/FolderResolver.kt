package com.example.mediasorted

object FolderResolver {

    private val folderMap: Map<SourceApp, Map<MediaType, String>> = mapOf(

        SourceApp.WHATSAPP to mapOf(

            MediaType.DOCUMENT to "com.whatsapp/WhatsApp/Media/WhatsApp Documents",
            MediaType.IMAGE to "com.whatsapp/WhatsApp/Media/WhatsApp Images",
            MediaType.VIDEO to "com.whatsapp/WhatsApp/Media/WhatsApp Video",
            MediaType.AUDIO to "com.whatsapp/WhatsApp/Media/WhatsApp Audio",
            MediaType.VOICE_NOTE to "com.whatsapp/WhatsApp/Media/WhatsApp Voice Notes"

        )

    )

    fun getFolderPath(
        sourceApp: SourceApp,
        mediaType: MediaType
    ): String? {
        return folderMap[sourceApp]?.get(mediaType)
    }

}