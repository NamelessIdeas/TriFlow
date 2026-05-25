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
import com.app.triflow.`data`.local.db.entity.TaskEntity
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
public class TaskDao_Impl(
  __db: RoomDatabase,
) : TaskDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfTaskEntity: EntityUpsertAdapter<TaskEntity>

  private val __roomConverters: RoomConverters = RoomConverters()
  init {
    this.__db = __db
    this.__upsertAdapterOfTaskEntity = EntityUpsertAdapter<TaskEntity>(object :
        EntityInsertAdapter<TaskEntity>() {
      protected override fun createQuery(): String =
          "INSERT INTO `tasks` (`id`,`title`,`notes`,`projectId`,`contextId`,`status`,`energy`,`estimatedMinutes`,`priority`,`dueDate`,`deferDate`,`completedAt`,`tags`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TaskEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.notes)
        val _tmpProjectId: String? = entity.projectId
        if (_tmpProjectId == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpProjectId)
        }
        val _tmpContextId: String? = entity.contextId
        if (_tmpContextId == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpContextId)
        }
        statement.bindText(6, entity.status)
        val _tmpEnergy: String? = entity.energy
        if (_tmpEnergy == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpEnergy)
        }
        val _tmpEstimatedMinutes: Int? = entity.estimatedMinutes
        if (_tmpEstimatedMinutes == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpEstimatedMinutes.toLong())
        }
        statement.bindLong(9, entity.priority.toLong())
        val _tmpDueDate: Instant? = entity.dueDate
        val _tmp: Long? = __roomConverters.fromInstant(_tmpDueDate)
        if (_tmp == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmp)
        }
        val _tmpDeferDate: Instant? = entity.deferDate
        val _tmp_1: Long? = __roomConverters.fromInstant(_tmpDeferDate)
        if (_tmp_1 == null) {
          statement.bindNull(11)
        } else {
          statement.bindLong(11, _tmp_1)
        }
        val _tmpCompletedAt: Instant? = entity.completedAt
        val _tmp_2: Long? = __roomConverters.fromInstant(_tmpCompletedAt)
        if (_tmp_2 == null) {
          statement.bindNull(12)
        } else {
          statement.bindLong(12, _tmp_2)
        }
        val _tmp_3: String = __roomConverters.fromStringList(entity.tags)
        statement.bindText(13, _tmp_3)
        val _tmp_4: Long? = __roomConverters.fromInstant(entity.createdAt)
        if (_tmp_4 == null) {
          statement.bindNull(14)
        } else {
          statement.bindLong(14, _tmp_4)
        }
        val _tmp_5: Long? = __roomConverters.fromInstant(entity.updatedAt)
        if (_tmp_5 == null) {
          statement.bindNull(15)
        } else {
          statement.bindLong(15, _tmp_5)
        }
      }
    }, object : EntityDeleteOrUpdateAdapter<TaskEntity>() {
      protected override fun createQuery(): String =
          "UPDATE `tasks` SET `id` = ?,`title` = ?,`notes` = ?,`projectId` = ?,`contextId` = ?,`status` = ?,`energy` = ?,`estimatedMinutes` = ?,`priority` = ?,`dueDate` = ?,`deferDate` = ?,`completedAt` = ?,`tags` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TaskEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.notes)
        val _tmpProjectId: String? = entity.projectId
        if (_tmpProjectId == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpProjectId)
        }
        val _tmpContextId: String? = entity.contextId
        if (_tmpContextId == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpContextId)
        }
        statement.bindText(6, entity.status)
        val _tmpEnergy: String? = entity.energy
        if (_tmpEnergy == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpEnergy)
        }
        val _tmpEstimatedMinutes: Int? = entity.estimatedMinutes
        if (_tmpEstimatedMinutes == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpEstimatedMinutes.toLong())
        }
        statement.bindLong(9, entity.priority.toLong())
        val _tmpDueDate: Instant? = entity.dueDate
        val _tmp: Long? = __roomConverters.fromInstant(_tmpDueDate)
        if (_tmp == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmp)
        }
        val _tmpDeferDate: Instant? = entity.deferDate
        val _tmp_1: Long? = __roomConverters.fromInstant(_tmpDeferDate)
        if (_tmp_1 == null) {
          statement.bindNull(11)
        } else {
          statement.bindLong(11, _tmp_1)
        }
        val _tmpCompletedAt: Instant? = entity.completedAt
        val _tmp_2: Long? = __roomConverters.fromInstant(_tmpCompletedAt)
        if (_tmp_2 == null) {
          statement.bindNull(12)
        } else {
          statement.bindLong(12, _tmp_2)
        }
        val _tmp_3: String = __roomConverters.fromStringList(entity.tags)
        statement.bindText(13, _tmp_3)
        val _tmp_4: Long? = __roomConverters.fromInstant(entity.createdAt)
        if (_tmp_4 == null) {
          statement.bindNull(14)
        } else {
          statement.bindLong(14, _tmp_4)
        }
        val _tmp_5: Long? = __roomConverters.fromInstant(entity.updatedAt)
        if (_tmp_5 == null) {
          statement.bindNull(15)
        } else {
          statement.bindLong(15, _tmp_5)
        }
        statement.bindText(16, entity.id)
      }
    })
  }

  public override suspend fun upsert(task: TaskEntity): Long = performSuspending(__db, false, true)
      { _connection ->
    val _result: Long = __upsertAdapterOfTaskEntity.upsertAndReturnId(_connection, task)
    _result
  }

  public override suspend fun upsertAll(tasks: List<TaskEntity>): List<Long> =
      performSuspending(__db, false, true) { _connection ->
    val _result: List<Long> = __upsertAdapterOfTaskEntity.upsertAndReturnIdsList(_connection, tasks)
    _result
  }

  public override fun observeAll(): Flow<List<TaskEntity>> {
    val _sql: String = "SELECT * FROM tasks ORDER BY updatedAt DESC"
    return createFlow(__db, false, arrayOf("tasks")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfProjectId: Int = getColumnIndexOrThrow(_stmt, "projectId")
        val _columnIndexOfContextId: Int = getColumnIndexOrThrow(_stmt, "contextId")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfEnergy: Int = getColumnIndexOrThrow(_stmt, "energy")
        val _columnIndexOfEstimatedMinutes: Int = getColumnIndexOrThrow(_stmt, "estimatedMinutes")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfDueDate: Int = getColumnIndexOrThrow(_stmt, "dueDate")
        val _columnIndexOfDeferDate: Int = getColumnIndexOrThrow(_stmt, "deferDate")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completedAt")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<TaskEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TaskEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          val _tmpProjectId: String?
          if (_stmt.isNull(_columnIndexOfProjectId)) {
            _tmpProjectId = null
          } else {
            _tmpProjectId = _stmt.getText(_columnIndexOfProjectId)
          }
          val _tmpContextId: String?
          if (_stmt.isNull(_columnIndexOfContextId)) {
            _tmpContextId = null
          } else {
            _tmpContextId = _stmt.getText(_columnIndexOfContextId)
          }
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpEnergy: String?
          if (_stmt.isNull(_columnIndexOfEnergy)) {
            _tmpEnergy = null
          } else {
            _tmpEnergy = _stmt.getText(_columnIndexOfEnergy)
          }
          val _tmpEstimatedMinutes: Int?
          if (_stmt.isNull(_columnIndexOfEstimatedMinutes)) {
            _tmpEstimatedMinutes = null
          } else {
            _tmpEstimatedMinutes = _stmt.getLong(_columnIndexOfEstimatedMinutes).toInt()
          }
          val _tmpPriority: Int
          _tmpPriority = _stmt.getLong(_columnIndexOfPriority).toInt()
          val _tmpDueDate: Instant?
          val _tmp: Long?
          if (_stmt.isNull(_columnIndexOfDueDate)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfDueDate)
          }
          _tmpDueDate = __roomConverters.toInstant(_tmp)
          val _tmpDeferDate: Instant?
          val _tmp_1: Long?
          if (_stmt.isNull(_columnIndexOfDeferDate)) {
            _tmp_1 = null
          } else {
            _tmp_1 = _stmt.getLong(_columnIndexOfDeferDate)
          }
          _tmpDeferDate = __roomConverters.toInstant(_tmp_1)
          val _tmpCompletedAt: Instant?
          val _tmp_2: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmp_2 = null
          } else {
            _tmp_2 = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          _tmpCompletedAt = __roomConverters.toInstant(_tmp_2)
          val _tmpTags: List<String>
          val _tmp_3: String
          _tmp_3 = _stmt.getText(_columnIndexOfTags)
          _tmpTags = __roomConverters.toStringList(_tmp_3)
          val _tmpCreatedAt: Instant
          val _tmp_4: Long?
          if (_stmt.isNull(_columnIndexOfCreatedAt)) {
            _tmp_4 = null
          } else {
            _tmp_4 = _stmt.getLong(_columnIndexOfCreatedAt)
          }
          val _tmp_5: Instant? = __roomConverters.toInstant(_tmp_4)
          if (_tmp_5 == null) {
            error("Expected NON-NULL 'kotlinx.datetime.Instant', but it was NULL.")
          } else {
            _tmpCreatedAt = _tmp_5
          }
          val _tmpUpdatedAt: Instant
          val _tmp_6: Long?
          if (_stmt.isNull(_columnIndexOfUpdatedAt)) {
            _tmp_6 = null
          } else {
            _tmp_6 = _stmt.getLong(_columnIndexOfUpdatedAt)
          }
          val _tmp_7: Instant? = __roomConverters.toInstant(_tmp_6)
          if (_tmp_7 == null) {
            error("Expected NON-NULL 'kotlinx.datetime.Instant', but it was NULL.")
          } else {
            _tmpUpdatedAt = _tmp_7
          }
          _item =
              TaskEntity(_tmpId,_tmpTitle,_tmpNotes,_tmpProjectId,_tmpContextId,_tmpStatus,_tmpEnergy,_tmpEstimatedMinutes,_tmpPriority,_tmpDueDate,_tmpDeferDate,_tmpCompletedAt,_tmpTags,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeByStatus(status: String): Flow<List<TaskEntity>> {
    val _sql: String = "SELECT * FROM tasks WHERE status = ? ORDER BY priority DESC, dueDate ASC"
    return createFlow(__db, false, arrayOf("tasks")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, status)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfProjectId: Int = getColumnIndexOrThrow(_stmt, "projectId")
        val _columnIndexOfContextId: Int = getColumnIndexOrThrow(_stmt, "contextId")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfEnergy: Int = getColumnIndexOrThrow(_stmt, "energy")
        val _columnIndexOfEstimatedMinutes: Int = getColumnIndexOrThrow(_stmt, "estimatedMinutes")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfDueDate: Int = getColumnIndexOrThrow(_stmt, "dueDate")
        val _columnIndexOfDeferDate: Int = getColumnIndexOrThrow(_stmt, "deferDate")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completedAt")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<TaskEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TaskEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          val _tmpProjectId: String?
          if (_stmt.isNull(_columnIndexOfProjectId)) {
            _tmpProjectId = null
          } else {
            _tmpProjectId = _stmt.getText(_columnIndexOfProjectId)
          }
          val _tmpContextId: String?
          if (_stmt.isNull(_columnIndexOfContextId)) {
            _tmpContextId = null
          } else {
            _tmpContextId = _stmt.getText(_columnIndexOfContextId)
          }
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpEnergy: String?
          if (_stmt.isNull(_columnIndexOfEnergy)) {
            _tmpEnergy = null
          } else {
            _tmpEnergy = _stmt.getText(_columnIndexOfEnergy)
          }
          val _tmpEstimatedMinutes: Int?
          if (_stmt.isNull(_columnIndexOfEstimatedMinutes)) {
            _tmpEstimatedMinutes = null
          } else {
            _tmpEstimatedMinutes = _stmt.getLong(_columnIndexOfEstimatedMinutes).toInt()
          }
          val _tmpPriority: Int
          _tmpPriority = _stmt.getLong(_columnIndexOfPriority).toInt()
          val _tmpDueDate: Instant?
          val _tmp: Long?
          if (_stmt.isNull(_columnIndexOfDueDate)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfDueDate)
          }
          _tmpDueDate = __roomConverters.toInstant(_tmp)
          val _tmpDeferDate: Instant?
          val _tmp_1: Long?
          if (_stmt.isNull(_columnIndexOfDeferDate)) {
            _tmp_1 = null
          } else {
            _tmp_1 = _stmt.getLong(_columnIndexOfDeferDate)
          }
          _tmpDeferDate = __roomConverters.toInstant(_tmp_1)
          val _tmpCompletedAt: Instant?
          val _tmp_2: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmp_2 = null
          } else {
            _tmp_2 = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          _tmpCompletedAt = __roomConverters.toInstant(_tmp_2)
          val _tmpTags: List<String>
          val _tmp_3: String
          _tmp_3 = _stmt.getText(_columnIndexOfTags)
          _tmpTags = __roomConverters.toStringList(_tmp_3)
          val _tmpCreatedAt: Instant
          val _tmp_4: Long?
          if (_stmt.isNull(_columnIndexOfCreatedAt)) {
            _tmp_4 = null
          } else {
            _tmp_4 = _stmt.getLong(_columnIndexOfCreatedAt)
          }
          val _tmp_5: Instant? = __roomConverters.toInstant(_tmp_4)
          if (_tmp_5 == null) {
            error("Expected NON-NULL 'kotlinx.datetime.Instant', but it was NULL.")
          } else {
            _tmpCreatedAt = _tmp_5
          }
          val _tmpUpdatedAt: Instant
          val _tmp_6: Long?
          if (_stmt.isNull(_columnIndexOfUpdatedAt)) {
            _tmp_6 = null
          } else {
            _tmp_6 = _stmt.getLong(_columnIndexOfUpdatedAt)
          }
          val _tmp_7: Instant? = __roomConverters.toInstant(_tmp_6)
          if (_tmp_7 == null) {
            error("Expected NON-NULL 'kotlinx.datetime.Instant', but it was NULL.")
          } else {
            _tmpUpdatedAt = _tmp_7
          }
          _item =
              TaskEntity(_tmpId,_tmpTitle,_tmpNotes,_tmpProjectId,_tmpContextId,_tmpStatus,_tmpEnergy,_tmpEstimatedMinutes,_tmpPriority,_tmpDueDate,_tmpDeferDate,_tmpCompletedAt,_tmpTags,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeByProject(projectId: String): Flow<List<TaskEntity>> {
    val _sql: String = "SELECT * FROM tasks WHERE projectId = ? ORDER BY priority DESC, dueDate ASC"
    return createFlow(__db, false, arrayOf("tasks")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, projectId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfProjectId: Int = getColumnIndexOrThrow(_stmt, "projectId")
        val _columnIndexOfContextId: Int = getColumnIndexOrThrow(_stmt, "contextId")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfEnergy: Int = getColumnIndexOrThrow(_stmt, "energy")
        val _columnIndexOfEstimatedMinutes: Int = getColumnIndexOrThrow(_stmt, "estimatedMinutes")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfDueDate: Int = getColumnIndexOrThrow(_stmt, "dueDate")
        val _columnIndexOfDeferDate: Int = getColumnIndexOrThrow(_stmt, "deferDate")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completedAt")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<TaskEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TaskEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          val _tmpProjectId: String?
          if (_stmt.isNull(_columnIndexOfProjectId)) {
            _tmpProjectId = null
          } else {
            _tmpProjectId = _stmt.getText(_columnIndexOfProjectId)
          }
          val _tmpContextId: String?
          if (_stmt.isNull(_columnIndexOfContextId)) {
            _tmpContextId = null
          } else {
            _tmpContextId = _stmt.getText(_columnIndexOfContextId)
          }
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpEnergy: String?
          if (_stmt.isNull(_columnIndexOfEnergy)) {
            _tmpEnergy = null
          } else {
            _tmpEnergy = _stmt.getText(_columnIndexOfEnergy)
          }
          val _tmpEstimatedMinutes: Int?
          if (_stmt.isNull(_columnIndexOfEstimatedMinutes)) {
            _tmpEstimatedMinutes = null
          } else {
            _tmpEstimatedMinutes = _stmt.getLong(_columnIndexOfEstimatedMinutes).toInt()
          }
          val _tmpPriority: Int
          _tmpPriority = _stmt.getLong(_columnIndexOfPriority).toInt()
          val _tmpDueDate: Instant?
          val _tmp: Long?
          if (_stmt.isNull(_columnIndexOfDueDate)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfDueDate)
          }
          _tmpDueDate = __roomConverters.toInstant(_tmp)
          val _tmpDeferDate: Instant?
          val _tmp_1: Long?
          if (_stmt.isNull(_columnIndexOfDeferDate)) {
            _tmp_1 = null
          } else {
            _tmp_1 = _stmt.getLong(_columnIndexOfDeferDate)
          }
          _tmpDeferDate = __roomConverters.toInstant(_tmp_1)
          val _tmpCompletedAt: Instant?
          val _tmp_2: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmp_2 = null
          } else {
            _tmp_2 = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          _tmpCompletedAt = __roomConverters.toInstant(_tmp_2)
          val _tmpTags: List<String>
          val _tmp_3: String
          _tmp_3 = _stmt.getText(_columnIndexOfTags)
          _tmpTags = __roomConverters.toStringList(_tmp_3)
          val _tmpCreatedAt: Instant
          val _tmp_4: Long?
          if (_stmt.isNull(_columnIndexOfCreatedAt)) {
            _tmp_4 = null
          } else {
            _tmp_4 = _stmt.getLong(_columnIndexOfCreatedAt)
          }
          val _tmp_5: Instant? = __roomConverters.toInstant(_tmp_4)
          if (_tmp_5 == null) {
            error("Expected NON-NULL 'kotlinx.datetime.Instant', but it was NULL.")
          } else {
            _tmpCreatedAt = _tmp_5
          }
          val _tmpUpdatedAt: Instant
          val _tmp_6: Long?
          if (_stmt.isNull(_columnIndexOfUpdatedAt)) {
            _tmp_6 = null
          } else {
            _tmp_6 = _stmt.getLong(_columnIndexOfUpdatedAt)
          }
          val _tmp_7: Instant? = __roomConverters.toInstant(_tmp_6)
          if (_tmp_7 == null) {
            error("Expected NON-NULL 'kotlinx.datetime.Instant', but it was NULL.")
          } else {
            _tmpUpdatedAt = _tmp_7
          }
          _item =
              TaskEntity(_tmpId,_tmpTitle,_tmpNotes,_tmpProjectId,_tmpContextId,_tmpStatus,_tmpEnergy,_tmpEstimatedMinutes,_tmpPriority,_tmpDueDate,_tmpDeferDate,_tmpCompletedAt,_tmpTags,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getById(id: String): TaskEntity? {
    val _sql: String = "SELECT * FROM tasks WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfProjectId: Int = getColumnIndexOrThrow(_stmt, "projectId")
        val _columnIndexOfContextId: Int = getColumnIndexOrThrow(_stmt, "contextId")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfEnergy: Int = getColumnIndexOrThrow(_stmt, "energy")
        val _columnIndexOfEstimatedMinutes: Int = getColumnIndexOrThrow(_stmt, "estimatedMinutes")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfDueDate: Int = getColumnIndexOrThrow(_stmt, "dueDate")
        val _columnIndexOfDeferDate: Int = getColumnIndexOrThrow(_stmt, "deferDate")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completedAt")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: TaskEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          val _tmpProjectId: String?
          if (_stmt.isNull(_columnIndexOfProjectId)) {
            _tmpProjectId = null
          } else {
            _tmpProjectId = _stmt.getText(_columnIndexOfProjectId)
          }
          val _tmpContextId: String?
          if (_stmt.isNull(_columnIndexOfContextId)) {
            _tmpContextId = null
          } else {
            _tmpContextId = _stmt.getText(_columnIndexOfContextId)
          }
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpEnergy: String?
          if (_stmt.isNull(_columnIndexOfEnergy)) {
            _tmpEnergy = null
          } else {
            _tmpEnergy = _stmt.getText(_columnIndexOfEnergy)
          }
          val _tmpEstimatedMinutes: Int?
          if (_stmt.isNull(_columnIndexOfEstimatedMinutes)) {
            _tmpEstimatedMinutes = null
          } else {
            _tmpEstimatedMinutes = _stmt.getLong(_columnIndexOfEstimatedMinutes).toInt()
          }
          val _tmpPriority: Int
          _tmpPriority = _stmt.getLong(_columnIndexOfPriority).toInt()
          val _tmpDueDate: Instant?
          val _tmp: Long?
          if (_stmt.isNull(_columnIndexOfDueDate)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfDueDate)
          }
          _tmpDueDate = __roomConverters.toInstant(_tmp)
          val _tmpDeferDate: Instant?
          val _tmp_1: Long?
          if (_stmt.isNull(_columnIndexOfDeferDate)) {
            _tmp_1 = null
          } else {
            _tmp_1 = _stmt.getLong(_columnIndexOfDeferDate)
          }
          _tmpDeferDate = __roomConverters.toInstant(_tmp_1)
          val _tmpCompletedAt: Instant?
          val _tmp_2: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmp_2 = null
          } else {
            _tmp_2 = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          _tmpCompletedAt = __roomConverters.toInstant(_tmp_2)
          val _tmpTags: List<String>
          val _tmp_3: String
          _tmp_3 = _stmt.getText(_columnIndexOfTags)
          _tmpTags = __roomConverters.toStringList(_tmp_3)
          val _tmpCreatedAt: Instant
          val _tmp_4: Long?
          if (_stmt.isNull(_columnIndexOfCreatedAt)) {
            _tmp_4 = null
          } else {
            _tmp_4 = _stmt.getLong(_columnIndexOfCreatedAt)
          }
          val _tmp_5: Instant? = __roomConverters.toInstant(_tmp_4)
          if (_tmp_5 == null) {
            error("Expected NON-NULL 'kotlinx.datetime.Instant', but it was NULL.")
          } else {
            _tmpCreatedAt = _tmp_5
          }
          val _tmpUpdatedAt: Instant
          val _tmp_6: Long?
          if (_stmt.isNull(_columnIndexOfUpdatedAt)) {
            _tmp_6 = null
          } else {
            _tmp_6 = _stmt.getLong(_columnIndexOfUpdatedAt)
          }
          val _tmp_7: Instant? = __roomConverters.toInstant(_tmp_6)
          if (_tmp_7 == null) {
            error("Expected NON-NULL 'kotlinx.datetime.Instant', but it was NULL.")
          } else {
            _tmpUpdatedAt = _tmp_7
          }
          _result =
              TaskEntity(_tmpId,_tmpTitle,_tmpNotes,_tmpProjectId,_tmpContextId,_tmpStatus,_tmpEnergy,_tmpEstimatedMinutes,_tmpPriority,_tmpDueDate,_tmpDeferDate,_tmpCompletedAt,_tmpTags,_tmpCreatedAt,_tmpUpdatedAt)
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
    val _sql: String = "DELETE FROM tasks WHERE id = ?"
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
    val _sql: String = "DELETE FROM tasks"
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
