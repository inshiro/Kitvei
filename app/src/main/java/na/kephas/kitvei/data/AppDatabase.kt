package na.kephas.kitvei.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.AssetSQLiteOpenHelperFactory
import na.kephas.kitvei.util.DATABASE_NAME

@Database(entities = arrayOf(Bible::class), version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun verseDao(): VerseDao
    abstract fun searchDao(): SearchDao
    abstract fun miniSearchDao(): MiniSearchDao

    companion object {

        @Volatile
        private var instance: AppDatabase? = null

        @Synchronized
        fun getInstance(ctx: Context): AppDatabase {
            if (instance != null)
                return instance!!

            val MIGRATION_1_2 =
                    object : Migration(1, 2) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            Log.w("AppDatabase", "migrate from version 1 to 2")
                        }
                    }

            val MIGRATION_2_3 =
                    object : Migration(2, 3) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            Log.w("AppDatabase", "migrate from version 2 to 3")
                        }
                    }

            val db = Room.databaseBuilder(ctx.applicationContext, AppDatabase::class.java, DATABASE_NAME)
            instance = db.openHelperFactory(AssetSQLiteOpenHelperFactory())
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
            return instance!!
        }

        fun destroyInstance() {
            instance = null
        }
    }

}