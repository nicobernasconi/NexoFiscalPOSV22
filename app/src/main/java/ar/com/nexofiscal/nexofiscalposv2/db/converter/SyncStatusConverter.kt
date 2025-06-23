package ar.com.nexofiscal.nexofiscalposv2.db.converter

import androidx.room.TypeConverter
import ar.com.nexofiscal.nexofiscalposv2.db.entity.SyncStatus

class SyncStatusConverter {
    @TypeConverter
    fun fromSyncStatus(status: SyncStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun toSyncStatus(status: String?): SyncStatus? {
        return status?.let { enumValueOf<SyncStatus>(it) }
    }
}