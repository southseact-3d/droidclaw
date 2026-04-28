package com.agentapp.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.agentapp.data.models.Converters;
import com.agentapp.data.models.JobStatus;
import com.agentapp.data.models.JobType;
import com.agentapp.data.models.ScheduledJob;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ScheduledJobDao_Impl implements ScheduledJobDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ScheduledJob> __insertionAdapterOfScheduledJob;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<ScheduledJob> __updateAdapterOfScheduledJob;

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  private final SharedSQLiteStatement __preparedStmtOfRecordRun;

  public ScheduledJobDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfScheduledJob = new EntityInsertionAdapter<ScheduledJob>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `scheduled_jobs` (`id`,`name`,`type`,`cronExpression`,`intervalMinutes`,`prompt`,`status`,`lastRunAt`,`nextRunAt`,`runCount`,`notifyOnResult`,`activeHoursStart`,`activeHoursEnd`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ScheduledJob entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        final String _tmp = __converters.fromJobType(entity.getType());
        statement.bindString(3, _tmp);
        if (entity.getCronExpression() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getCronExpression());
        }
        statement.bindLong(5, entity.getIntervalMinutes());
        statement.bindString(6, entity.getPrompt());
        final String _tmp_1 = __converters.fromJobStatus(entity.getStatus());
        statement.bindString(7, _tmp_1);
        if (entity.getLastRunAt() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getLastRunAt());
        }
        if (entity.getNextRunAt() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getNextRunAt());
        }
        statement.bindLong(10, entity.getRunCount());
        final int _tmp_2 = entity.getNotifyOnResult() ? 1 : 0;
        statement.bindLong(11, _tmp_2);
        statement.bindLong(12, entity.getActiveHoursStart());
        statement.bindLong(13, entity.getActiveHoursEnd());
      }
    };
    this.__updateAdapterOfScheduledJob = new EntityDeletionOrUpdateAdapter<ScheduledJob>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `scheduled_jobs` SET `id` = ?,`name` = ?,`type` = ?,`cronExpression` = ?,`intervalMinutes` = ?,`prompt` = ?,`status` = ?,`lastRunAt` = ?,`nextRunAt` = ?,`runCount` = ?,`notifyOnResult` = ?,`activeHoursStart` = ?,`activeHoursEnd` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ScheduledJob entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        final String _tmp = __converters.fromJobType(entity.getType());
        statement.bindString(3, _tmp);
        if (entity.getCronExpression() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getCronExpression());
        }
        statement.bindLong(5, entity.getIntervalMinutes());
        statement.bindString(6, entity.getPrompt());
        final String _tmp_1 = __converters.fromJobStatus(entity.getStatus());
        statement.bindString(7, _tmp_1);
        if (entity.getLastRunAt() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getLastRunAt());
        }
        if (entity.getNextRunAt() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getNextRunAt());
        }
        statement.bindLong(10, entity.getRunCount());
        final int _tmp_2 = entity.getNotifyOnResult() ? 1 : 0;
        statement.bindLong(11, _tmp_2);
        statement.bindLong(12, entity.getActiveHoursStart());
        statement.bindLong(13, entity.getActiveHoursEnd());
        statement.bindString(14, entity.getId());
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM scheduled_jobs WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfRecordRun = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE scheduled_jobs SET lastRunAt = ?, runCount = runCount + 1 WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final ScheduledJob job, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfScheduledJob.insert(job);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final ScheduledJob job, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfScheduledJob.handle(job);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDelete.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDelete.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object recordRun(final String id, final long lastRun,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRecordRun.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, lastRun);
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfRecordRun.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ScheduledJob>> getAllJobs() {
    final String _sql = "SELECT * FROM scheduled_jobs ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"scheduled_jobs"}, new Callable<List<ScheduledJob>>() {
      @Override
      @NonNull
      public List<ScheduledJob> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfCronExpression = CursorUtil.getColumnIndexOrThrow(_cursor, "cronExpression");
          final int _cursorIndexOfIntervalMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalMinutes");
          final int _cursorIndexOfPrompt = CursorUtil.getColumnIndexOrThrow(_cursor, "prompt");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfLastRunAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastRunAt");
          final int _cursorIndexOfNextRunAt = CursorUtil.getColumnIndexOrThrow(_cursor, "nextRunAt");
          final int _cursorIndexOfRunCount = CursorUtil.getColumnIndexOrThrow(_cursor, "runCount");
          final int _cursorIndexOfNotifyOnResult = CursorUtil.getColumnIndexOrThrow(_cursor, "notifyOnResult");
          final int _cursorIndexOfActiveHoursStart = CursorUtil.getColumnIndexOrThrow(_cursor, "activeHoursStart");
          final int _cursorIndexOfActiveHoursEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "activeHoursEnd");
          final List<ScheduledJob> _result = new ArrayList<ScheduledJob>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScheduledJob _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final JobType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __converters.toJobType(_tmp);
            final String _tmpCronExpression;
            if (_cursor.isNull(_cursorIndexOfCronExpression)) {
              _tmpCronExpression = null;
            } else {
              _tmpCronExpression = _cursor.getString(_cursorIndexOfCronExpression);
            }
            final int _tmpIntervalMinutes;
            _tmpIntervalMinutes = _cursor.getInt(_cursorIndexOfIntervalMinutes);
            final String _tmpPrompt;
            _tmpPrompt = _cursor.getString(_cursorIndexOfPrompt);
            final JobStatus _tmpStatus;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toJobStatus(_tmp_1);
            final Long _tmpLastRunAt;
            if (_cursor.isNull(_cursorIndexOfLastRunAt)) {
              _tmpLastRunAt = null;
            } else {
              _tmpLastRunAt = _cursor.getLong(_cursorIndexOfLastRunAt);
            }
            final Long _tmpNextRunAt;
            if (_cursor.isNull(_cursorIndexOfNextRunAt)) {
              _tmpNextRunAt = null;
            } else {
              _tmpNextRunAt = _cursor.getLong(_cursorIndexOfNextRunAt);
            }
            final int _tmpRunCount;
            _tmpRunCount = _cursor.getInt(_cursorIndexOfRunCount);
            final boolean _tmpNotifyOnResult;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfNotifyOnResult);
            _tmpNotifyOnResult = _tmp_2 != 0;
            final int _tmpActiveHoursStart;
            _tmpActiveHoursStart = _cursor.getInt(_cursorIndexOfActiveHoursStart);
            final int _tmpActiveHoursEnd;
            _tmpActiveHoursEnd = _cursor.getInt(_cursorIndexOfActiveHoursEnd);
            _item = new ScheduledJob(_tmpId,_tmpName,_tmpType,_tmpCronExpression,_tmpIntervalMinutes,_tmpPrompt,_tmpStatus,_tmpLastRunAt,_tmpNextRunAt,_tmpRunCount,_tmpNotifyOnResult,_tmpActiveHoursStart,_tmpActiveHoursEnd);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getActiveJobs(final Continuation<? super List<ScheduledJob>> $completion) {
    final String _sql = "SELECT * FROM scheduled_jobs WHERE status = 'ACTIVE'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ScheduledJob>>() {
      @Override
      @NonNull
      public List<ScheduledJob> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfCronExpression = CursorUtil.getColumnIndexOrThrow(_cursor, "cronExpression");
          final int _cursorIndexOfIntervalMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalMinutes");
          final int _cursorIndexOfPrompt = CursorUtil.getColumnIndexOrThrow(_cursor, "prompt");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfLastRunAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastRunAt");
          final int _cursorIndexOfNextRunAt = CursorUtil.getColumnIndexOrThrow(_cursor, "nextRunAt");
          final int _cursorIndexOfRunCount = CursorUtil.getColumnIndexOrThrow(_cursor, "runCount");
          final int _cursorIndexOfNotifyOnResult = CursorUtil.getColumnIndexOrThrow(_cursor, "notifyOnResult");
          final int _cursorIndexOfActiveHoursStart = CursorUtil.getColumnIndexOrThrow(_cursor, "activeHoursStart");
          final int _cursorIndexOfActiveHoursEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "activeHoursEnd");
          final List<ScheduledJob> _result = new ArrayList<ScheduledJob>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScheduledJob _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final JobType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __converters.toJobType(_tmp);
            final String _tmpCronExpression;
            if (_cursor.isNull(_cursorIndexOfCronExpression)) {
              _tmpCronExpression = null;
            } else {
              _tmpCronExpression = _cursor.getString(_cursorIndexOfCronExpression);
            }
            final int _tmpIntervalMinutes;
            _tmpIntervalMinutes = _cursor.getInt(_cursorIndexOfIntervalMinutes);
            final String _tmpPrompt;
            _tmpPrompt = _cursor.getString(_cursorIndexOfPrompt);
            final JobStatus _tmpStatus;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toJobStatus(_tmp_1);
            final Long _tmpLastRunAt;
            if (_cursor.isNull(_cursorIndexOfLastRunAt)) {
              _tmpLastRunAt = null;
            } else {
              _tmpLastRunAt = _cursor.getLong(_cursorIndexOfLastRunAt);
            }
            final Long _tmpNextRunAt;
            if (_cursor.isNull(_cursorIndexOfNextRunAt)) {
              _tmpNextRunAt = null;
            } else {
              _tmpNextRunAt = _cursor.getLong(_cursorIndexOfNextRunAt);
            }
            final int _tmpRunCount;
            _tmpRunCount = _cursor.getInt(_cursorIndexOfRunCount);
            final boolean _tmpNotifyOnResult;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfNotifyOnResult);
            _tmpNotifyOnResult = _tmp_2 != 0;
            final int _tmpActiveHoursStart;
            _tmpActiveHoursStart = _cursor.getInt(_cursorIndexOfActiveHoursStart);
            final int _tmpActiveHoursEnd;
            _tmpActiveHoursEnd = _cursor.getInt(_cursorIndexOfActiveHoursEnd);
            _item = new ScheduledJob(_tmpId,_tmpName,_tmpType,_tmpCronExpression,_tmpIntervalMinutes,_tmpPrompt,_tmpStatus,_tmpLastRunAt,_tmpNextRunAt,_tmpRunCount,_tmpNotifyOnResult,_tmpActiveHoursStart,_tmpActiveHoursEnd);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getJob(final String id, final Continuation<? super ScheduledJob> $completion) {
    final String _sql = "SELECT * FROM scheduled_jobs WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ScheduledJob>() {
      @Override
      @Nullable
      public ScheduledJob call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfCronExpression = CursorUtil.getColumnIndexOrThrow(_cursor, "cronExpression");
          final int _cursorIndexOfIntervalMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "intervalMinutes");
          final int _cursorIndexOfPrompt = CursorUtil.getColumnIndexOrThrow(_cursor, "prompt");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfLastRunAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastRunAt");
          final int _cursorIndexOfNextRunAt = CursorUtil.getColumnIndexOrThrow(_cursor, "nextRunAt");
          final int _cursorIndexOfRunCount = CursorUtil.getColumnIndexOrThrow(_cursor, "runCount");
          final int _cursorIndexOfNotifyOnResult = CursorUtil.getColumnIndexOrThrow(_cursor, "notifyOnResult");
          final int _cursorIndexOfActiveHoursStart = CursorUtil.getColumnIndexOrThrow(_cursor, "activeHoursStart");
          final int _cursorIndexOfActiveHoursEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "activeHoursEnd");
          final ScheduledJob _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final JobType _tmpType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfType);
            _tmpType = __converters.toJobType(_tmp);
            final String _tmpCronExpression;
            if (_cursor.isNull(_cursorIndexOfCronExpression)) {
              _tmpCronExpression = null;
            } else {
              _tmpCronExpression = _cursor.getString(_cursorIndexOfCronExpression);
            }
            final int _tmpIntervalMinutes;
            _tmpIntervalMinutes = _cursor.getInt(_cursorIndexOfIntervalMinutes);
            final String _tmpPrompt;
            _tmpPrompt = _cursor.getString(_cursorIndexOfPrompt);
            final JobStatus _tmpStatus;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.toJobStatus(_tmp_1);
            final Long _tmpLastRunAt;
            if (_cursor.isNull(_cursorIndexOfLastRunAt)) {
              _tmpLastRunAt = null;
            } else {
              _tmpLastRunAt = _cursor.getLong(_cursorIndexOfLastRunAt);
            }
            final Long _tmpNextRunAt;
            if (_cursor.isNull(_cursorIndexOfNextRunAt)) {
              _tmpNextRunAt = null;
            } else {
              _tmpNextRunAt = _cursor.getLong(_cursorIndexOfNextRunAt);
            }
            final int _tmpRunCount;
            _tmpRunCount = _cursor.getInt(_cursorIndexOfRunCount);
            final boolean _tmpNotifyOnResult;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfNotifyOnResult);
            _tmpNotifyOnResult = _tmp_2 != 0;
            final int _tmpActiveHoursStart;
            _tmpActiveHoursStart = _cursor.getInt(_cursorIndexOfActiveHoursStart);
            final int _tmpActiveHoursEnd;
            _tmpActiveHoursEnd = _cursor.getInt(_cursorIndexOfActiveHoursEnd);
            _result = new ScheduledJob(_tmpId,_tmpName,_tmpType,_tmpCronExpression,_tmpIntervalMinutes,_tmpPrompt,_tmpStatus,_tmpLastRunAt,_tmpNextRunAt,_tmpRunCount,_tmpNotifyOnResult,_tmpActiveHoursStart,_tmpActiveHoursEnd);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
