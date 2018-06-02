package na.kephas.kitvei.repository

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.AssetSQLiteOpenHelperFactory

@Database(entities = arrayOf((Bible::class)), version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bibleDao(): Bible.BibleDao

    companion object {
        private const val DB_NAME = "kjv-pce-v2.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        //@Synchronized
        //operator fun get(ctx: Context): AppDatabase? = INSTANCE ?: create(ctx)

        @Synchronized
        operator fun get(ctx: Context): AppDatabase {
            if (INSTANCE != null)
                return INSTANCE!!

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

            val db = Room.databaseBuilder(ctx.applicationContext, AppDatabase::class.java, DB_NAME)
            INSTANCE = db.openHelperFactory(AssetSQLiteOpenHelperFactory())
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }

}