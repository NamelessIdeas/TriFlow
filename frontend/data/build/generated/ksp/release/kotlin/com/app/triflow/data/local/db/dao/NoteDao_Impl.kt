package com.app.triflow.`data`.local.db.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.getTotalChangedRows
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.app.triflow.`data`.local.db.converters.RoomConverters
import com.app.triflow.`data`.local.db.entity.NoteEntity
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class NoteDao_Impl(
  __db: RoomDatabase,
) : NoteDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfNoteEntity: EntityUpsertAdapter<NoteEntity>

  private val __roomConverters: RoomConverters = RoomConverters()
  init {
    this.__db = __db
    this.__upsertAdapterOfNoteEntity = EntityUpsertAdapter<NoteEntity>(object :
        EntityInsertAdapter<NoteEntity>() {
      protected override fun createQuery(): String =
          "INSERT INTO `notes` (`id`,`title`,`contentMd`,`paraCategory`,`tags`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: NoteEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.contentMd)
        val _tmpParaCategory: String? = entity.paraCategory
        if (_tmpParaCategory == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpParaCategory)
        }
        val _tmp: String = __roomConverters.fromStringList(entity.tags)
        statement.bindText(5, _tmp)
        val _tmp_1: Long? = __roomConverters.fromInstant(entity.createdAt)
        if (_tmp_1 == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmp_1)
        }
        val _tmp_2: Long? = __roomConverters.fromInstant(entity.updatedAt)
        if (_tmp_2 == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmp_2)
        }
      }
    }, object : EntityDeleteOrUpdateAdapter<NoteEntity>() {
      protected override fun createQuery(): String =
          "UPDATE `notes` SET `id` = ?,`title` = ?,`contentMd` = ?,`paraCategory` = ?,`tags` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: NoteEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.contentMd)
        val _tmpParaCategory: String? = entity.paraCategory
        if (_tmpParaCategory == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpParaCategory)
        }
        val _tmp: String = __roomConverters.fromStringList(entity.tags)
        statement.bindText(5, _tmp)
        val _tmp_1: Long? = __roomConverters.fromInstant(entity.createdAt)
        if (_tmp_1 == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmp_1)
        }
        val _tmp_2: Long? = __roomConverters.fromInstant(entity.updatedAt)
        if (_tmp_2 == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmp_2)
        }
        statement.bindText(8, entity.id)
      }
    })
  }

  public override suspend fun upsert(note: NoteEntity): Long = performSuspending(__db, false, true)
      { _connection ->
    val _result: Long = __upsertAdapterOfNoteEntity.upsertAndReturnId(_connection, note)
    _result
  }

  public override suspend fun upsertAll(notes: List<NoteEntity>): List<Long> =
      performSuspending(__db, false, true) { _connection ->
    val _result: List<Long> = __upsertAdapterOfNoteEntity.upsertAndReturnIdsList(_connection, notes)
    _result
  }

  public override fun observeAll(): Flow<List<NoteEntity>> {
    val _sql: String = "SELECT * FROM notes ORDER BY updatedAt DESC"
    return createFlow(__db, false, arrayOf("notes")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfContentMd: Int = getColumnIndexOrThrow(_stmt, "contentMd")
        val _columnIndexOfParaCategory: Int = getColumnIndexOrThrow(_stmt, "paraCategory")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<NoteEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: NoteEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpContentMd: String
          _tmpContentMd = _stmt.getText(_columnIndexOfContentMd)
          val _tmpParaCategory: String?
          if (_stmt.isNull(_columnIndexOfParaCategory)) {
            _tmpParaCategory = null
          } else {
            _tmpParaCategory = _stmt.getText(_columnIndexOfParaCategory)
          }
          val _tmpTags: List<String>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfTags)
          _tmpTags = __roomConverters.toStringList(_tmp)
          val _tmpCreatedAt: Instant
          val _tmp_1: Long?
          if (_stmt.isNull(_columnIndexOfCreatedAt)) {
            _tmp_1 = null
          } else {
            _tmp_1 = _stmt.getLong(_columnIndexOfCreatedAt)
          }
          val _tmp_2: Instant? = __roomConverters.toInstant(_tmp_1)
          if (_tmp_2 == null) {
            error("Expected NON-NULL 'kotlinx.datetime.Instant', but it was NULL.")
          } else {
            _tmpCreatedAt = _tmp_2
          }
          val _tmpUpdatedAt: Instant
          val _tmp_3: Long?
          if (_stmt.isNull(_columnIndexOfUpdatedAt)) {
            _tmp_3 = null
          } else {
            _tmp_3 = _stmt.getLong(_columnIndexOfUpdatedAt)
          }
          val _tmp_4: Instant? = __roomConverters.toInstant(_tmp_3)
          if (_tmp_4 == null) {
            error("Expected NON-NULL 'kotlinx.datetime.Instant', but it was NULL.")
          } else {
            _tmpUpdatedAt = _tmp_4
          }
          _item =
              NoteEntity(_tmpId,_tmpTitle,_tmpContentMd,_tmpParaCategory,_tmpTags,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeByCategory(category: String): Flow<List<NoteEntity>> {
    val _sql: String = "SELECT * FROM notes WHERE paraCategory = ? ORDER BY updatedAt DESC"
    return createFlow(__db, false, arrayOf("notes")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, category)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfContentMd: Int = getColumnIndexOrThrow(_stmt, "contentMd")
        val _columnIndexOfParaCategory: Int = getColumnIndexOrThrow(_stmt, "paraCategory")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<NoteEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: NoteEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpContentMd: String
          _tmpContentMd = _stmt.getText(_columnIndexOfContentMd)
          val _tmpParaCategory: String?
          if (_stmt.isNull(_columnIndexOfParaCategory)) {
            _tmpParaCategory = null
          } else {
            _tmpParaCategory = _stmt.getText(_columnIndexOfParaCategory)
          }
          val _tmpTags: List<String>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfTags)
          _tmpTags = __roomConverters.toStringList(_tmp)
          val _tmpCreatedAt: Instant
          val _tmp_1: Long?
          if (_stmt.isNull(_columnIndexOfCreatedAt)) {
            _tmp_1 = null
          } else {
            _tmp_1 = _stmt.getLong(_columnIndexOfCreatedAt)
          }
          val _tmp_2: Instant? = __roomConverters.toInstant(_tmp_1)
          if (_tmp_2 == null) {
            error("Expected NON-NULL 'kotlinx.datetime.Instant', but it was NULL.")
          } else {
            _tmpCreatedAt = _tmp_2
          }
          val _tmpUpdatedAt: Instant
          val _tmp_3: Long?
          if (_stmt.isNull(_columnIndexOfUpdatedAt)) {
            _tmp_3 = null
          } else {
            _tmp_3 = _stmt.getLong(_columnIndexOfUpdatedAt)
          }
          val _tmp_4: Instant? = __roomConverters.toInstant(_tmp_3)
          if (_tmp_4 == null) {
            error("Expected NON-NULL 'kotlinx.datetime.Instant', but it was NULL.")
          } else {
            _tmpUpdatedAt = _tmp_4
          }
          _item =
              NoteEntity(_tmpId,_tmpTitle,_tmpContentMd,_tmpParaCategory,_tmpTags,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getById(id: String): NoteEntity? {
    val _sql: String = "SELECT * FROM notes WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfContentMd: Int = getColumnIndexOrThrow(_stmt, "contentMd")
        val _columnIndexOfParaCategory: Int = getColumnIndexOrThrow(_stmt, "paraCategory")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: NoteEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpContentMd: String
          _tmpContentMd = _stmt.getText(_columnIndexOfContentMd)
          val _tmpParaCategory: String?
          if (_stmt.isNull(_columnIndexOfParaCategory)) {
            _tmpParaCategory = null
          } else {
            _tmpParaCategory = _stmt.getText(_columnIndexOfParaCategory)
          }
          val _tmpTags: List<String>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfTags)
          _tmpTags = __roomConverters.toStringList(_tmp)
          val _tmpCreatedAt: Instant
          val _tmp_1: Long?
          if (_stmt.isNull(_columnIndexOfCreatedAt)) {
            _tmp_1 = null
          } else {
            _tmp_1 = _stmt.getLong(_columnIndexOfCreatedAt)
          }
          val _tmp_2: Instant? = __roomConverters.toInstant(_tmp_1)
          if (_tmp_2 == null) {
            error("Expected NON-NULL 'kotlinx.datetime.Instant', but it was NULL.")
          } else {
            _tmpCreatedAt = _tmp_2
          }
          val _tmpUpdatedAt: Instant
          val _tmp_3: Long?
          if (_stmt.isNull(_columnIndexOfUpdatedAt)) {
            _tmp_3 = null
          } else {
            _tmp_3 = _stmt.getLong(_columnIndexOfUpdatedAt)
          }
          val _tmp_4: Instant? = __roomConverters.toInstant(_tmp_3)
          if (_tmp_4 == null) {
            error("Expected NON-NULL 'kotlinx.datetime.Instant', but it was NULL.")
          } else {
            _tmpUpdatedAt = _tmp_4
          }
          _result =
              NoteEntity(_tmpId,_tmpTitle,_tmpContentMd,_tmpParaCategory,_tmpTags,_tmpCreatedAt,_tmpUpdatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteById(id: String): Int {
    val _sql: String = "DELETE FROM notes WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
        getTotalChangedRows(_connection)
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clear(): Int {
    val _sql: String = "DELETE FROM notes"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
        getTotalChangedRows(_connection)
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
