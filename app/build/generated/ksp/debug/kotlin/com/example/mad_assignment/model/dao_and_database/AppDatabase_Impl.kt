package com.example.mad_assignment.model.dao_and_database

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _offlineDao: Lazy<OfflineDao> = lazy {
    OfflineDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(6, "d568893c4fba3ae6a135a090724317ef", "e09b69e07e041bfeeb97182822a1ab16") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `offline_books` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `author` TEXT NOT NULL, `isbn` TEXT NOT NULL, `coverBase64` TEXT, `coverUrl` TEXT, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `current_borrows` (`loanId` TEXT NOT NULL, `bookId` TEXT NOT NULL, `borrowerName` TEXT NOT NULL, `borrowedAt` INTEGER NOT NULL, `dueAt` INTEGER NOT NULL, PRIMARY KEY(`loanId`), FOREIGN KEY(`bookId`) REFERENCES `offline_books`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_current_borrows_bookId` ON `current_borrows` (`bookId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'd568893c4fba3ae6a135a090724317ef')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `offline_books`")
        connection.execSQL("DROP TABLE IF EXISTS `current_borrows`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsOfflineBooks: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsOfflineBooks.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOfflineBooks.put("title", TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOfflineBooks.put("author", TableInfo.Column("author", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOfflineBooks.put("isbn", TableInfo.Column("isbn", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOfflineBooks.put("coverBase64", TableInfo.Column("coverBase64", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOfflineBooks.put("coverUrl", TableInfo.Column("coverUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsOfflineBooks.put("cachedAt", TableInfo.Column("cachedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysOfflineBooks: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesOfflineBooks: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoOfflineBooks: TableInfo = TableInfo("offline_books", _columnsOfflineBooks, _foreignKeysOfflineBooks, _indicesOfflineBooks)
        val _existingOfflineBooks: TableInfo = read(connection, "offline_books")
        if (!_infoOfflineBooks.equals(_existingOfflineBooks)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |offline_books(com.example.mad_assignment.model.entity.OfflineBookEntity).
              | Expected:
              |""".trimMargin() + _infoOfflineBooks + """
              |
              | Found:
              |""".trimMargin() + _existingOfflineBooks)
        }
        val _columnsCurrentBorrows: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCurrentBorrows.put("loanId", TableInfo.Column("loanId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCurrentBorrows.put("bookId", TableInfo.Column("bookId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCurrentBorrows.put("borrowerName", TableInfo.Column("borrowerName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCurrentBorrows.put("borrowedAt", TableInfo.Column("borrowedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCurrentBorrows.put("dueAt", TableInfo.Column("dueAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCurrentBorrows: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysCurrentBorrows.add(TableInfo.ForeignKey("offline_books", "CASCADE", "NO ACTION", listOf("bookId"), listOf("id")))
        val _indicesCurrentBorrows: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesCurrentBorrows.add(TableInfo.Index("index_current_borrows_bookId", false, listOf("bookId"), listOf("ASC")))
        val _infoCurrentBorrows: TableInfo = TableInfo("current_borrows", _columnsCurrentBorrows, _foreignKeysCurrentBorrows, _indicesCurrentBorrows)
        val _existingCurrentBorrows: TableInfo = read(connection, "current_borrows")
        if (!_infoCurrentBorrows.equals(_existingCurrentBorrows)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |current_borrows(com.example.mad_assignment.model.entity.CurrentBorrowEntity).
              | Expected:
              |""".trimMargin() + _infoCurrentBorrows + """
              |
              | Found:
              |""".trimMargin() + _existingCurrentBorrows)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "offline_books", "current_borrows")
  }

  public override fun clearAllTables() {
    super.performClear(true, "offline_books", "current_borrows")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(OfflineDao::class, OfflineDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun offlineDao(): OfflineDao = _offlineDao.value
}
