package com.agentapp.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
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
import com.agentapp.data.models.Message;
import com.agentapp.data.models.MessageSource;
import com.agentapp.data.models.Role;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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
public final class MessageDao_Impl implements MessageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Message> __insertionAdapterOfMessage;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<Message> __updateAdapterOfMessage;

  private final SharedSQLiteStatement __preparedStmtOfClearSession;

  public MessageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMessage = new EntityInsertionAdapter<Message>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `messages` (`id`,`sessionId`,`role`,`content`,`source`,`timestamp`,`providerUsed`,`tokenCount`,`isStreaming`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Message entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSessionId());
        final String _tmp = __converters.fromRole(entity.getRole());
        statement.bindString(3, _tmp);
        statement.bindString(4, entity.getContent());
        final String _tmp_1 = __converters.fromSource(entity.getSource());
        statement.bindString(5, _tmp_1);
        statement.bindLong(6, entity.getTimestamp());
        if (entity.getProviderUsed() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getProviderUsed());
        }
        if (entity.getTokenCount() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getTokenCount());
        }
        final int _tmp_2 = entity.isStreaming() ? 1 : 0;
        statement.bindLong(9, _tmp_2);
      }
    };
    this.__updateAdapterOfMessage = new EntityDeletionOrUpdateAdapter<Message>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `messages` SET `id` = ?,`sessionId` = ?,`role` = ?,`content` = ?,`source` = ?,`timestamp` = ?,`providerUsed` = ?,`tokenCount` = ?,`isStreaming` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Message entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSessionId());
        final String _tmp = __converters.fromRole(entity.getRole());
        statement.bindString(3, _tmp);
        statement.bindString(4, entity.getContent());
        final String _tmp_1 = __converters.fromSource(entity.getSource());
        statement.bindString(5, _tmp_1);
        statement.bindLong(6, entity.getTimestamp());
        if (entity.getProviderUsed() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getProviderUsed());
        }
        if (entity.getTokenCount() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getTokenCount());
        }
        final int _tmp_2 = entity.isStreaming() ? 1 : 0;
        statement.bindLong(9, _tmp_2);
        statement.bindLong(10, entity.getId());
      }
    };
    this.__preparedStmtOfClearSession = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM messages WHERE sessionId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final Message message, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfMessage.insertAndReturnId(message);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Message message, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfMessage.handle(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearSession(final String sessionId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearSession.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, sessionId);
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
          __preparedStmtOfClearSession.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Message>> getMessages(final String sessionId) {
    final String _sql = "SELECT * FROM messages WHERE sessionId = ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"messages"}, new Callable<List<Message>>() {
      @Override
      @NonNull
      public List<Message> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfProviderUsed = CursorUtil.getColumnIndexOrThrow(_cursor, "providerUsed");
          final int _cursorIndexOfTokenCount = CursorUtil.getColumnIndexOrThrow(_cursor, "tokenCount");
          final int _cursorIndexOfIsStreaming = CursorUtil.getColumnIndexOrThrow(_cursor, "isStreaming");
          final List<Message> _result = new ArrayList<Message>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Message _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final Role _tmpRole;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfRole);
            _tmpRole = __converters.toRole(_tmp);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final MessageSource _tmpSource;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfSource);
            _tmpSource = __converters.toSource(_tmp_1);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpProviderUsed;
            if (_cursor.isNull(_cursorIndexOfProviderUsed)) {
              _tmpProviderUsed = null;
            } else {
              _tmpProviderUsed = _cursor.getString(_cursorIndexOfProviderUsed);
            }
            final Integer _tmpTokenCount;
            if (_cursor.isNull(_cursorIndexOfTokenCount)) {
              _tmpTokenCount = null;
            } else {
              _tmpTokenCount = _cursor.getInt(_cursorIndexOfTokenCount);
            }
            final boolean _tmpIsStreaming;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsStreaming);
            _tmpIsStreaming = _tmp_2 != 0;
            _item = new Message(_tmpId,_tmpSessionId,_tmpRole,_tmpContent,_tmpSource,_tmpTimestamp,_tmpProviderUsed,_tmpTokenCount,_tmpIsStreaming);
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
  public Object getMessagesOnce(final String sessionId,
      final Continuation<? super List<Message>> $completion) {
    final String _sql = "SELECT * FROM messages WHERE sessionId = ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Message>>() {
      @Override
      @NonNull
      public List<Message> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfProviderUsed = CursorUtil.getColumnIndexOrThrow(_cursor, "providerUsed");
          final int _cursorIndexOfTokenCount = CursorUtil.getColumnIndexOrThrow(_cursor, "tokenCount");
          final int _cursorIndexOfIsStreaming = CursorUtil.getColumnIndexOrThrow(_cursor, "isStreaming");
          final List<Message> _result = new ArrayList<Message>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Message _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final Role _tmpRole;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfRole);
            _tmpRole = __converters.toRole(_tmp);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final MessageSource _tmpSource;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfSource);
            _tmpSource = __converters.toSource(_tmp_1);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpProviderUsed;
            if (_cursor.isNull(_cursorIndexOfProviderUsed)) {
              _tmpProviderUsed = null;
            } else {
              _tmpProviderUsed = _cursor.getString(_cursorIndexOfProviderUsed);
            }
            final Integer _tmpTokenCount;
            if (_cursor.isNull(_cursorIndexOfTokenCount)) {
              _tmpTokenCount = null;
            } else {
              _tmpTokenCount = _cursor.getInt(_cursorIndexOfTokenCount);
            }
            final boolean _tmpIsStreaming;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsStreaming);
            _tmpIsStreaming = _tmp_2 != 0;
            _item = new Message(_tmpId,_tmpSessionId,_tmpRole,_tmpContent,_tmpSource,_tmpTimestamp,_tmpProviderUsed,_tmpTokenCount,_tmpIsStreaming);
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
  public Object getRecentMessages(final String sessionId, final int limit,
      final Continuation<? super List<Message>> $completion) {
    final String _sql = "SELECT * FROM messages WHERE sessionId = ? ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Message>>() {
      @Override
      @NonNull
      public List<Message> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfProviderUsed = CursorUtil.getColumnIndexOrThrow(_cursor, "providerUsed");
          final int _cursorIndexOfTokenCount = CursorUtil.getColumnIndexOrThrow(_cursor, "tokenCount");
          final int _cursorIndexOfIsStreaming = CursorUtil.getColumnIndexOrThrow(_cursor, "isStreaming");
          final List<Message> _result = new ArrayList<Message>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Message _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final Role _tmpRole;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfRole);
            _tmpRole = __converters.toRole(_tmp);
            final String _tmpContent;
            _tmpContent = _cursor.getString(_cursorIndexOfContent);
            final MessageSource _tmpSource;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfSource);
            _tmpSource = __converters.toSource(_tmp_1);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpProviderUsed;
            if (_cursor.isNull(_cursorIndexOfProviderUsed)) {
              _tmpProviderUsed = null;
            } else {
              _tmpProviderUsed = _cursor.getString(_cursorIndexOfProviderUsed);
            }
            final Integer _tmpTokenCount;
            if (_cursor.isNull(_cursorIndexOfTokenCount)) {
              _tmpTokenCount = null;
            } else {
              _tmpTokenCount = _cursor.getInt(_cursorIndexOfTokenCount);
            }
            final boolean _tmpIsStreaming;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsStreaming);
            _tmpIsStreaming = _tmp_2 != 0;
            _item = new Message(_tmpId,_tmpSessionId,_tmpRole,_tmpContent,_tmpSource,_tmpTimestamp,_tmpProviderUsed,_tmpTokenCount,_tmpIsStreaming);
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
  public Object countMessages(final String sessionId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM messages WHERE sessionId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
