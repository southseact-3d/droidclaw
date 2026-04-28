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
import com.agentapp.data.models.Skill;
import java.lang.Class;
import java.lang.Exception;
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
public final class SkillDao_Impl implements SkillDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Skill> __insertionAdapterOfSkill;

  private final EntityDeletionOrUpdateAdapter<Skill> __updateAdapterOfSkill;

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  private final SharedSQLiteStatement __preparedStmtOfSetEnabled;

  public SkillDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSkill = new EntityInsertionAdapter<Skill>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `skills` (`id`,`name`,`description`,`markdownContent`,`toolDefinitions`,`enabled`,`installedAt`,`sourceUrl`,`version`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Skill entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getDescription());
        statement.bindString(4, entity.getMarkdownContent());
        statement.bindString(5, entity.getToolDefinitions());
        final int _tmp = entity.getEnabled() ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindLong(7, entity.getInstalledAt());
        if (entity.getSourceUrl() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getSourceUrl());
        }
        statement.bindString(9, entity.getVersion());
      }
    };
    this.__updateAdapterOfSkill = new EntityDeletionOrUpdateAdapter<Skill>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `skills` SET `id` = ?,`name` = ?,`description` = ?,`markdownContent` = ?,`toolDefinitions` = ?,`enabled` = ?,`installedAt` = ?,`sourceUrl` = ?,`version` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Skill entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getDescription());
        statement.bindString(4, entity.getMarkdownContent());
        statement.bindString(5, entity.getToolDefinitions());
        final int _tmp = entity.getEnabled() ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindLong(7, entity.getInstalledAt());
        if (entity.getSourceUrl() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getSourceUrl());
        }
        statement.bindString(9, entity.getVersion());
        statement.bindString(10, entity.getId());
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM skills WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSetEnabled = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE skills SET enabled = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final Skill skill, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSkill.insert(skill);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Skill skill, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSkill.handle(skill);
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
  public Object setEnabled(final String id, final boolean enabled,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetEnabled.acquire();
        int _argIndex = 1;
        final int _tmp = enabled ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
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
          __preparedStmtOfSetEnabled.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Skill>> getAllSkills() {
    final String _sql = "SELECT * FROM skills ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"skills"}, new Callable<List<Skill>>() {
      @Override
      @NonNull
      public List<Skill> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfMarkdownContent = CursorUtil.getColumnIndexOrThrow(_cursor, "markdownContent");
          final int _cursorIndexOfToolDefinitions = CursorUtil.getColumnIndexOrThrow(_cursor, "toolDefinitions");
          final int _cursorIndexOfEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "enabled");
          final int _cursorIndexOfInstalledAt = CursorUtil.getColumnIndexOrThrow(_cursor, "installedAt");
          final int _cursorIndexOfSourceUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceUrl");
          final int _cursorIndexOfVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "version");
          final List<Skill> _result = new ArrayList<Skill>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Skill _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpMarkdownContent;
            _tmpMarkdownContent = _cursor.getString(_cursorIndexOfMarkdownContent);
            final String _tmpToolDefinitions;
            _tmpToolDefinitions = _cursor.getString(_cursorIndexOfToolDefinitions);
            final boolean _tmpEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfEnabled);
            _tmpEnabled = _tmp != 0;
            final long _tmpInstalledAt;
            _tmpInstalledAt = _cursor.getLong(_cursorIndexOfInstalledAt);
            final String _tmpSourceUrl;
            if (_cursor.isNull(_cursorIndexOfSourceUrl)) {
              _tmpSourceUrl = null;
            } else {
              _tmpSourceUrl = _cursor.getString(_cursorIndexOfSourceUrl);
            }
            final String _tmpVersion;
            _tmpVersion = _cursor.getString(_cursorIndexOfVersion);
            _item = new Skill(_tmpId,_tmpName,_tmpDescription,_tmpMarkdownContent,_tmpToolDefinitions,_tmpEnabled,_tmpInstalledAt,_tmpSourceUrl,_tmpVersion);
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
  public Object getEnabledSkills(final Continuation<? super List<Skill>> $completion) {
    final String _sql = "SELECT * FROM skills WHERE enabled = 1 ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Skill>>() {
      @Override
      @NonNull
      public List<Skill> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfMarkdownContent = CursorUtil.getColumnIndexOrThrow(_cursor, "markdownContent");
          final int _cursorIndexOfToolDefinitions = CursorUtil.getColumnIndexOrThrow(_cursor, "toolDefinitions");
          final int _cursorIndexOfEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "enabled");
          final int _cursorIndexOfInstalledAt = CursorUtil.getColumnIndexOrThrow(_cursor, "installedAt");
          final int _cursorIndexOfSourceUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceUrl");
          final int _cursorIndexOfVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "version");
          final List<Skill> _result = new ArrayList<Skill>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Skill _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpMarkdownContent;
            _tmpMarkdownContent = _cursor.getString(_cursorIndexOfMarkdownContent);
            final String _tmpToolDefinitions;
            _tmpToolDefinitions = _cursor.getString(_cursorIndexOfToolDefinitions);
            final boolean _tmpEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfEnabled);
            _tmpEnabled = _tmp != 0;
            final long _tmpInstalledAt;
            _tmpInstalledAt = _cursor.getLong(_cursorIndexOfInstalledAt);
            final String _tmpSourceUrl;
            if (_cursor.isNull(_cursorIndexOfSourceUrl)) {
              _tmpSourceUrl = null;
            } else {
              _tmpSourceUrl = _cursor.getString(_cursorIndexOfSourceUrl);
            }
            final String _tmpVersion;
            _tmpVersion = _cursor.getString(_cursorIndexOfVersion);
            _item = new Skill(_tmpId,_tmpName,_tmpDescription,_tmpMarkdownContent,_tmpToolDefinitions,_tmpEnabled,_tmpInstalledAt,_tmpSourceUrl,_tmpVersion);
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
  public Object getSkill(final String id, final Continuation<? super Skill> $completion) {
    final String _sql = "SELECT * FROM skills WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Skill>() {
      @Override
      @Nullable
      public Skill call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfMarkdownContent = CursorUtil.getColumnIndexOrThrow(_cursor, "markdownContent");
          final int _cursorIndexOfToolDefinitions = CursorUtil.getColumnIndexOrThrow(_cursor, "toolDefinitions");
          final int _cursorIndexOfEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "enabled");
          final int _cursorIndexOfInstalledAt = CursorUtil.getColumnIndexOrThrow(_cursor, "installedAt");
          final int _cursorIndexOfSourceUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceUrl");
          final int _cursorIndexOfVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "version");
          final Skill _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpMarkdownContent;
            _tmpMarkdownContent = _cursor.getString(_cursorIndexOfMarkdownContent);
            final String _tmpToolDefinitions;
            _tmpToolDefinitions = _cursor.getString(_cursorIndexOfToolDefinitions);
            final boolean _tmpEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfEnabled);
            _tmpEnabled = _tmp != 0;
            final long _tmpInstalledAt;
            _tmpInstalledAt = _cursor.getLong(_cursorIndexOfInstalledAt);
            final String _tmpSourceUrl;
            if (_cursor.isNull(_cursorIndexOfSourceUrl)) {
              _tmpSourceUrl = null;
            } else {
              _tmpSourceUrl = _cursor.getString(_cursorIndexOfSourceUrl);
            }
            final String _tmpVersion;
            _tmpVersion = _cursor.getString(_cursorIndexOfVersion);
            _result = new Skill(_tmpId,_tmpName,_tmpDescription,_tmpMarkdownContent,_tmpToolDefinitions,_tmpEnabled,_tmpInstalledAt,_tmpSourceUrl,_tmpVersion);
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
