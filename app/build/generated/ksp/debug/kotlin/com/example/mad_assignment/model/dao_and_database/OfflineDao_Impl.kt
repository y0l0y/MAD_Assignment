package com.example.mad_assignment.model.dao_and_database

import androidx.collection.ArrayMap
import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndex
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performInTransactionSuspending
import androidx.room.util.performSuspending
import androidx.room.util.recursiveFetchArrayMap
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement
import com.example.mad_assignment.model.entity.BorrowWithBook
import com.example.mad_assignment.model.entity.CurrentBorrowEntity
import com.example.mad_assignment.model.entity.OfflineBookEntity
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlin.text.StringBuilder
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class OfflineDao_Impl(
  __db: RoomDatabase,
) : OfflineDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfCurrentBorrowEntity: EntityInsertAdapter<CurrentBorrowEntity>

  private val __upsertAdapterOfOfflineBookEntity: EntityUpsertAdapter<OfflineBookEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfCurrentBorrowEntity = object : EntityInsertAdapter<CurrentBorrowEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `current_borrows` (`loanId`,`bookId`,`borrowerName`,`borrowedAt`,`dueAt`) VALUES (?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CurrentBorrowEntity) {
        statement.bindText(1, entity.loanId)
        statement.bindText(2, entity.bookId)
        statement.bindText(3, entity.borrowerName)
        statement.bindLong(4, entity.borrowedAt)
        statement.bindLong(5, entity.dueAt)
      }
    }
    this.__upsertAdapterOfOfflineBookEntity = EntityUpsertAdapter<OfflineBookEntity>(object : EntityInsertAdapter<OfflineBookEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `offline_books` (`id`,`title`,`author`,`isbn`,`coverBase64`,`coverUrl`,`cachedAt`) VALUES (?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: OfflineBookEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.author)
        statement.bindText(4, entity.isbn)
        val _tmpCoverBase64: String? = entity.coverBase64
        if (_tmpCoverBase64 == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpCoverBase64)
        }
        val _tmpCoverUrl: String? = entity.coverUrl
        if (_tmpCoverUrl == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpCoverUrl)
        }
        statement.bindLong(7, entity.cachedAt)
      }
    }, object : EntityDeleteOrUpdateAdapter<OfflineBookEntity>() {
      protected override fun createQuery(): String = "UPDATE `offline_books` SET `id` = ?,`title` = ?,`author` = ?,`isbn` = ?,`coverBase64` = ?,`coverUrl` = ?,`cachedAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: OfflineBookEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.author)
        statement.bindText(4, entity.isbn)
        val _tmpCoverBase64: String? = entity.coverBase64
        if (_tmpCoverBase64 == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpCoverBase64)
        }
        val _tmpCoverUrl: String? = entity.coverUrl
        if (_tmpCoverUrl == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpCoverUrl)
        }
        statement.bindLong(7, entity.cachedAt)
        statement.bindText(8, entity.id)
      }
    })
  }

  public override suspend fun insertBorrows(borrows: List<CurrentBorrowEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfCurrentBorrowEntity.insert(_connection, borrows)
  }

  public override suspend fun replaceCache(books: List<OfflineBookEntity>, borrows: List<CurrentBorrowEntity>): Unit = performInTransactionSuspending(__db) {
    super@OfflineDao_Impl.replaceCache(books, borrows)
  }

  public override suspend fun upsertBooks(books: List<OfflineBookEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfOfflineBookEntity.upsert(_connection, books)
  }

  public override fun getOfflineBook(): Flow<List<OfflineBookEntity>> {
    val _sql: String = "SELECT * FROM offline_books ORDER BY title COLLATE NOCASE ASC"
    return createFlow(__db, false, arrayOf("offline_books")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfAuthor: Int = getColumnIndexOrThrow(_stmt, "author")
        val _columnIndexOfIsbn: Int = getColumnIndexOrThrow(_stmt, "isbn")
        val _columnIndexOfCoverBase64: Int = getColumnIndexOrThrow(_stmt, "coverBase64")
        val _columnIndexOfCoverUrl: Int = getColumnIndexOrThrow(_stmt, "coverUrl")
        val _columnIndexOfCachedAt: Int = getColumnIndexOrThrow(_stmt, "cachedAt")
        val _result: MutableList<OfflineBookEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: OfflineBookEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpAuthor: String
          _tmpAuthor = _stmt.getText(_columnIndexOfAuthor)
          val _tmpIsbn: String
          _tmpIsbn = _stmt.getText(_columnIndexOfIsbn)
          val _tmpCoverBase64: String?
          if (_stmt.isNull(_columnIndexOfCoverBase64)) {
            _tmpCoverBase64 = null
          } else {
            _tmpCoverBase64 = _stmt.getText(_columnIndexOfCoverBase64)
          }
          val _tmpCoverUrl: String?
          if (_stmt.isNull(_columnIndexOfCoverUrl)) {
            _tmpCoverUrl = null
          } else {
            _tmpCoverUrl = _stmt.getText(_columnIndexOfCoverUrl)
          }
          val _tmpCachedAt: Long
          _tmpCachedAt = _stmt.getLong(_columnIndexOfCachedAt)
          _item = OfflineBookEntity(_tmpId,_tmpTitle,_tmpAuthor,_tmpIsbn,_tmpCoverBase64,_tmpCoverUrl,_tmpCachedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getBorrowsWithBook(): Flow<List<BorrowWithBook>> {
    val _sql: String = "SELECT * FROM current_borrows ORDER BY borrowedAt DESC"
    return createFlow(__db, true, arrayOf("offline_books", "current_borrows")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfLoanId: Int = getColumnIndexOrThrow(_stmt, "loanId")
        val _columnIndexOfBookId: Int = getColumnIndexOrThrow(_stmt, "bookId")
        val _columnIndexOfBorrowerName: Int = getColumnIndexOrThrow(_stmt, "borrowerName")
        val _columnIndexOfBorrowedAt: Int = getColumnIndexOrThrow(_stmt, "borrowedAt")
        val _columnIndexOfDueAt: Int = getColumnIndexOrThrow(_stmt, "dueAt")
        val _collectionBook: ArrayMap<String, OfflineBookEntity?> = ArrayMap<String, OfflineBookEntity?>()
        while (_stmt.step()) {
          val _tmpKey: String
          _tmpKey = _stmt.getText(_columnIndexOfBookId)
          _collectionBook.put(_tmpKey, null)
        }
        _stmt.reset()
        __fetchRelationshipofflineBooksAscomExampleMadAssignmentModelEntityOfflineBookEntity(_connection, _collectionBook)
        val _result: MutableList<BorrowWithBook> = mutableListOf()
        while (_stmt.step()) {
          val _item: BorrowWithBook
          val _tmpBorrow: CurrentBorrowEntity
          val _tmpLoanId: String
          _tmpLoanId = _stmt.getText(_columnIndexOfLoanId)
          val _tmpBookId: String
          _tmpBookId = _stmt.getText(_columnIndexOfBookId)
          val _tmpBorrowerName: String
          _tmpBorrowerName = _stmt.getText(_columnIndexOfBorrowerName)
          val _tmpBorrowedAt: Long
          _tmpBorrowedAt = _stmt.getLong(_columnIndexOfBorrowedAt)
          val _tmpDueAt: Long
          _tmpDueAt = _stmt.getLong(_columnIndexOfDueAt)
          _tmpBorrow = CurrentBorrowEntity(_tmpLoanId,_tmpBookId,_tmpBorrowerName,_tmpBorrowedAt,_tmpDueAt)
          val _tmpBook: OfflineBookEntity?
          val _tmpKey_1: String
          _tmpKey_1 = _stmt.getText(_columnIndexOfBookId)
          _tmpBook = _collectionBook.get(_tmpKey_1)
          if (_tmpBook == null) {
            error("Relationship item 'book' was expected to be NON-NULL but is NULL in @Relation involving a parent column named 'bookId' and entityColumn named 'id'.")
          }
          _item = BorrowWithBook(_tmpBorrow,_tmpBook)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearBorrows() {
    val _sql: String = "DELETE FROM current_borrows"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearBooks() {
    val _sql: String = "DELETE FROM offline_books"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteBooksNotIn(ids: List<String>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("DELETE FROM offline_books WHERE id NOT IN (")
    val _inputSize: Int = ids.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: String in ids) {
          _stmt.bindText(_argIndex, _item)
          _argIndex++
        }
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  private fun __fetchRelationshipofflineBooksAscomExampleMadAssignmentModelEntityOfflineBookEntity(_connection: SQLiteConnection, _map: ArrayMap<String, OfflineBookEntity?>) {
    val __mapKeySet: Set<String> = _map.keys
    if (__mapKeySet.isEmpty()) {
      return
    }
    if (_map.size > 999) {
      recursiveFetchArrayMap(_map, false) { _tmpMap ->
        __fetchRelationshipofflineBooksAscomExampleMadAssignmentModelEntityOfflineBookEntity(_connection, _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`title`,`author`,`isbn`,`coverBase64`,`coverUrl`,`cachedAt` FROM `offline_books` WHERE `id` IN (")
    val _inputSize: Int = __mapKeySet.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    val _stmt: SQLiteStatement = _connection.prepare(_sql)
    var _argIndex: Int = 1
    for (_item: String in __mapKeySet) {
      _stmt.bindText(_argIndex, _item)
      _argIndex++
    }
    try {
      val _itemKeyIndex: Int = getColumnIndex(_stmt, "id")
      if (_itemKeyIndex == -1) {
        return
      }
      val _columnIndexOfId: Int = 0
      val _columnIndexOfTitle: Int = 1
      val _columnIndexOfAuthor: Int = 2
      val _columnIndexOfIsbn: Int = 3
      val _columnIndexOfCoverBase64: Int = 4
      val _columnIndexOfCoverUrl: Int = 5
      val _columnIndexOfCachedAt: Int = 6
      while (_stmt.step()) {
        val _tmpKey: String
        _tmpKey = _stmt.getText(_itemKeyIndex)
        if (_map.containsKey(_tmpKey)) {
          val _item_1: OfflineBookEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpAuthor: String
          _tmpAuthor = _stmt.getText(_columnIndexOfAuthor)
          val _tmpIsbn: String
          _tmpIsbn = _stmt.getText(_columnIndexOfIsbn)
          val _tmpCoverBase64: String?
          if (_stmt.isNull(_columnIndexOfCoverBase64)) {
            _tmpCoverBase64 = null
          } else {
            _tmpCoverBase64 = _stmt.getText(_columnIndexOfCoverBase64)
          }
          val _tmpCoverUrl: String?
          if (_stmt.isNull(_columnIndexOfCoverUrl)) {
            _tmpCoverUrl = null
          } else {
            _tmpCoverUrl = _stmt.getText(_columnIndexOfCoverUrl)
          }
          val _tmpCachedAt: Long
          _tmpCachedAt = _stmt.getLong(_columnIndexOfCachedAt)
          _item_1 = OfflineBookEntity(_tmpId,_tmpTitle,_tmpAuthor,_tmpIsbn,_tmpCoverBase64,_tmpCoverUrl,_tmpCachedAt)
          _map.put(_tmpKey, _item_1)
        }
      }
    } finally {
      _stmt.close()
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
