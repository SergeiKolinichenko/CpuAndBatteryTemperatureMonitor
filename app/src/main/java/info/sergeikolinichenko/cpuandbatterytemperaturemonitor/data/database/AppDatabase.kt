package info.sergeikolinichenko.cpuandbatterytemperaturemonitor.data.database

import android.app.Application
import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import info.sergeikolinichenko.cpuandbatterytemperaturemonitor.data.dbmodels.TempsDbModels

/** Created by Sergei Kolinichenko on 25.10.2022 at 08:05 (GMT+3) **/

@Database(
    entities = [TempsDbModels::class],
    autoMigrations = [ AutoMigration ( from = 1, to = 2, spec = AutoMigrationFrom1To2::class ) ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tempsDao(): TempsDao

    companion object {

        private var INSTANCE: AppDatabase? = null
        private val LOCK = Any()
        private const val DB_NAME = "test_database.db"

        fun getInstance(application: Application): AppDatabase {
            INSTANCE?.let {
                return it
            }
            synchronized(LOCK) {
                INSTANCE?.let {
                    return it
                }
                val db = Room.databaseBuilder(
                    application,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .build()
                INSTANCE = db
                return db
            }
        }
    }
}

@RenameColumn(tableName = "temperatures", fromColumnName = "tempCpu0", toColumnName = "tempCpu")
@DeleteColumn(tableName = "temperatures", columnName = "tempCpu1")
@DeleteColumn(tableName = "temperatures", columnName = "tempCpu2")
@DeleteColumn(tableName = "temperatures", columnName = "tempCpu3")
@DeleteColumn(tableName = "temperatures", columnName = "tempCpu4")
@DeleteColumn(tableName = "temperatures", columnName = "tempCpu5")
@DeleteColumn(tableName = "temperatures", columnName = "tempCpu6")
@DeleteColumn(tableName = "temperatures", columnName = "tempCpu7")
@DeleteColumn(tableName = "temperatures", columnName = "tempCpu8")
@DeleteColumn(tableName = "temperatures", columnName = "tempCpu9")
@DeleteColumn(tableName = "temperatures", columnName = "tempCpu10")
@DeleteColumn(tableName = "temperatures", columnName = "tempCpu11")
@DeleteColumn(tableName = "temperatures", columnName = "tempCpu12")
@DeleteColumn(tableName = "temperatures", columnName = "tempCpu13")
@DeleteColumn(tableName = "temperatures", columnName = "tempCpu14")
@DeleteColumn(tableName = "temperatures", columnName = "tempCpu15")
@DeleteColumn(tableName = "temperatures", columnName = "tempCpu16")
@DeleteColumn(tableName = "temperatures", columnName = "tempCpu17")
@DeleteColumn(tableName = "temperatures", columnName = "tempCpu18")
@DeleteColumn(tableName = "temperatures", columnName = "tempCpu19")
class AutoMigrationFrom1To2: AutoMigrationSpec