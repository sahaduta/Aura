package com.sahaduta.telegrambackup.data;

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
public final class GalleryDao_Impl implements GalleryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MediaEntity> __insertionAdapterOfMediaEntity;

  private final EntityInsertionAdapter<FaceClusterEntity> __insertionAdapterOfFaceClusterEntity;

  private final EntityInsertionAdapter<FaceEmbeddingEntity> __insertionAdapterOfFaceEmbeddingEntity;

  private final EntityInsertionAdapter<MediaTagEntity> __insertionAdapterOfMediaTagEntity;

  private final EntityDeletionOrUpdateAdapter<MediaEntity> __updateAdapterOfMediaEntity;

  private final EntityDeletionOrUpdateAdapter<FaceClusterEntity> __updateAdapterOfFaceClusterEntity;

  private final SharedSQLiteStatement __preparedStmtOfAssignEmbeddingToCluster;

  public GalleryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMediaEntity = new EntityInsertionAdapter<MediaEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `media_items` (`id`,`uriString`,`name`,`dateAdded`,`bucketName`,`mimeType`,`isVideo`,`isBackedUp`,`lastScanned`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MediaEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getUriString());
        statement.bindString(3, entity.getName());
        statement.bindLong(4, entity.getDateAdded());
        statement.bindString(5, entity.getBucketName());
        statement.bindString(6, entity.getMimeType());
        final int _tmp = entity.isVideo() ? 1 : 0;
        statement.bindLong(7, _tmp);
        final int _tmp_1 = entity.isBackedUp() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        statement.bindLong(9, entity.getLastScanned());
      }
    };
    this.__insertionAdapterOfFaceClusterEntity = new EntityInsertionAdapter<FaceClusterEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `face_clusters` (`clusterId`,`personName`) VALUES (nullif(?, 0),?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FaceClusterEntity entity) {
        statement.bindLong(1, entity.getClusterId());
        if (entity.getPersonName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getPersonName());
        }
      }
    };
    this.__insertionAdapterOfFaceEmbeddingEntity = new EntityInsertionAdapter<FaceEmbeddingEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `face_embeddings` (`embeddingId`,`mediaId`,`embeddingData`,`clusterId`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FaceEmbeddingEntity entity) {
        statement.bindLong(1, entity.getEmbeddingId());
        statement.bindLong(2, entity.getMediaId());
        statement.bindBlob(3, entity.getEmbeddingData());
        if (entity.getClusterId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getClusterId());
        }
      }
    };
    this.__insertionAdapterOfMediaTagEntity = new EntityInsertionAdapter<MediaTagEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `media_tags` (`tagId`,`mediaId`,`tag`,`confidence`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MediaTagEntity entity) {
        statement.bindLong(1, entity.getTagId());
        statement.bindLong(2, entity.getMediaId());
        statement.bindString(3, entity.getTag());
        statement.bindDouble(4, entity.getConfidence());
      }
    };
    this.__updateAdapterOfMediaEntity = new EntityDeletionOrUpdateAdapter<MediaEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `media_items` SET `id` = ?,`uriString` = ?,`name` = ?,`dateAdded` = ?,`bucketName` = ?,`mimeType` = ?,`isVideo` = ?,`isBackedUp` = ?,`lastScanned` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MediaEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getUriString());
        statement.bindString(3, entity.getName());
        statement.bindLong(4, entity.getDateAdded());
        statement.bindString(5, entity.getBucketName());
        statement.bindString(6, entity.getMimeType());
        final int _tmp = entity.isVideo() ? 1 : 0;
        statement.bindLong(7, _tmp);
        final int _tmp_1 = entity.isBackedUp() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        statement.bindLong(9, entity.getLastScanned());
        statement.bindLong(10, entity.getId());
      }
    };
    this.__updateAdapterOfFaceClusterEntity = new EntityDeletionOrUpdateAdapter<FaceClusterEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `face_clusters` SET `clusterId` = ?,`personName` = ? WHERE `clusterId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FaceClusterEntity entity) {
        statement.bindLong(1, entity.getClusterId());
        if (entity.getPersonName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getPersonName());
        }
        statement.bindLong(3, entity.getClusterId());
      }
    };
    this.__preparedStmtOfAssignEmbeddingToCluster = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE face_embeddings SET clusterId = ? WHERE embeddingId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertMedia(final MediaEntity media, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMediaEntity.insert(media);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertMediaList(final List<MediaEntity> mediaList,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMediaEntity.insert(mediaList);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertFaceCluster(final FaceClusterEntity cluster,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfFaceClusterEntity.insertAndReturnId(cluster);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertFaceEmbedding(final FaceEmbeddingEntity embedding,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfFaceEmbeddingEntity.insert(embedding);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertMediaTag(final MediaTagEntity tag,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMediaTagEntity.insert(tag);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertMediaTags(final List<MediaTagEntity> tags,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMediaTagEntity.insert(tags);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMedia(final MediaEntity media, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfMediaEntity.handle(media);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateFaceCluster(final FaceClusterEntity cluster,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfFaceClusterEntity.handle(cluster);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object assignEmbeddingToCluster(final long embeddingId, final long clusterId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfAssignEmbeddingToCluster.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, clusterId);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, embeddingId);
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
          __preparedStmtOfAssignEmbeddingToCluster.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MediaEntity>> getAllMediaDesc() {
    final String _sql = "SELECT * FROM media_items ORDER BY dateAdded DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"media_items"}, new Callable<List<MediaEntity>>() {
      @Override
      @NonNull
      public List<MediaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUriString = CursorUtil.getColumnIndexOrThrow(_cursor, "uriString");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDateAdded = CursorUtil.getColumnIndexOrThrow(_cursor, "dateAdded");
          final int _cursorIndexOfBucketName = CursorUtil.getColumnIndexOrThrow(_cursor, "bucketName");
          final int _cursorIndexOfMimeType = CursorUtil.getColumnIndexOrThrow(_cursor, "mimeType");
          final int _cursorIndexOfIsVideo = CursorUtil.getColumnIndexOrThrow(_cursor, "isVideo");
          final int _cursorIndexOfIsBackedUp = CursorUtil.getColumnIndexOrThrow(_cursor, "isBackedUp");
          final int _cursorIndexOfLastScanned = CursorUtil.getColumnIndexOrThrow(_cursor, "lastScanned");
          final List<MediaEntity> _result = new ArrayList<MediaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MediaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUriString;
            _tmpUriString = _cursor.getString(_cursorIndexOfUriString);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpDateAdded;
            _tmpDateAdded = _cursor.getLong(_cursorIndexOfDateAdded);
            final String _tmpBucketName;
            _tmpBucketName = _cursor.getString(_cursorIndexOfBucketName);
            final String _tmpMimeType;
            _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
            final boolean _tmpIsVideo;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsVideo);
            _tmpIsVideo = _tmp != 0;
            final boolean _tmpIsBackedUp;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsBackedUp);
            _tmpIsBackedUp = _tmp_1 != 0;
            final long _tmpLastScanned;
            _tmpLastScanned = _cursor.getLong(_cursorIndexOfLastScanned);
            _item = new MediaEntity(_tmpId,_tmpUriString,_tmpName,_tmpDateAdded,_tmpBucketName,_tmpMimeType,_tmpIsVideo,_tmpIsBackedUp,_tmpLastScanned);
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
  public Object getUnbackedUpMedia(final Continuation<? super List<MediaEntity>> $completion) {
    final String _sql = "SELECT * FROM media_items WHERE isBackedUp = 0 ORDER BY dateAdded ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MediaEntity>>() {
      @Override
      @NonNull
      public List<MediaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUriString = CursorUtil.getColumnIndexOrThrow(_cursor, "uriString");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDateAdded = CursorUtil.getColumnIndexOrThrow(_cursor, "dateAdded");
          final int _cursorIndexOfBucketName = CursorUtil.getColumnIndexOrThrow(_cursor, "bucketName");
          final int _cursorIndexOfMimeType = CursorUtil.getColumnIndexOrThrow(_cursor, "mimeType");
          final int _cursorIndexOfIsVideo = CursorUtil.getColumnIndexOrThrow(_cursor, "isVideo");
          final int _cursorIndexOfIsBackedUp = CursorUtil.getColumnIndexOrThrow(_cursor, "isBackedUp");
          final int _cursorIndexOfLastScanned = CursorUtil.getColumnIndexOrThrow(_cursor, "lastScanned");
          final List<MediaEntity> _result = new ArrayList<MediaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MediaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUriString;
            _tmpUriString = _cursor.getString(_cursorIndexOfUriString);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpDateAdded;
            _tmpDateAdded = _cursor.getLong(_cursorIndexOfDateAdded);
            final String _tmpBucketName;
            _tmpBucketName = _cursor.getString(_cursorIndexOfBucketName);
            final String _tmpMimeType;
            _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
            final boolean _tmpIsVideo;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsVideo);
            _tmpIsVideo = _tmp != 0;
            final boolean _tmpIsBackedUp;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsBackedUp);
            _tmpIsBackedUp = _tmp_1 != 0;
            final long _tmpLastScanned;
            _tmpLastScanned = _cursor.getLong(_cursorIndexOfLastScanned);
            _item = new MediaEntity(_tmpId,_tmpUriString,_tmpName,_tmpDateAdded,_tmpBucketName,_tmpMimeType,_tmpIsVideo,_tmpIsBackedUp,_tmpLastScanned);
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
  public Object getUnscannedMedia(final int limit,
      final Continuation<? super List<MediaEntity>> $completion) {
    final String _sql = "SELECT * FROM media_items WHERE lastScanned = 0 ORDER BY dateAdded DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MediaEntity>>() {
      @Override
      @NonNull
      public List<MediaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUriString = CursorUtil.getColumnIndexOrThrow(_cursor, "uriString");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDateAdded = CursorUtil.getColumnIndexOrThrow(_cursor, "dateAdded");
          final int _cursorIndexOfBucketName = CursorUtil.getColumnIndexOrThrow(_cursor, "bucketName");
          final int _cursorIndexOfMimeType = CursorUtil.getColumnIndexOrThrow(_cursor, "mimeType");
          final int _cursorIndexOfIsVideo = CursorUtil.getColumnIndexOrThrow(_cursor, "isVideo");
          final int _cursorIndexOfIsBackedUp = CursorUtil.getColumnIndexOrThrow(_cursor, "isBackedUp");
          final int _cursorIndexOfLastScanned = CursorUtil.getColumnIndexOrThrow(_cursor, "lastScanned");
          final List<MediaEntity> _result = new ArrayList<MediaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MediaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUriString;
            _tmpUriString = _cursor.getString(_cursorIndexOfUriString);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpDateAdded;
            _tmpDateAdded = _cursor.getLong(_cursorIndexOfDateAdded);
            final String _tmpBucketName;
            _tmpBucketName = _cursor.getString(_cursorIndexOfBucketName);
            final String _tmpMimeType;
            _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
            final boolean _tmpIsVideo;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsVideo);
            _tmpIsVideo = _tmp != 0;
            final boolean _tmpIsBackedUp;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsBackedUp);
            _tmpIsBackedUp = _tmp_1 != 0;
            final long _tmpLastScanned;
            _tmpLastScanned = _cursor.getLong(_cursorIndexOfLastScanned);
            _item = new MediaEntity(_tmpId,_tmpUriString,_tmpName,_tmpDateAdded,_tmpBucketName,_tmpMimeType,_tmpIsVideo,_tmpIsBackedUp,_tmpLastScanned);
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
  public Object getUnclusteredEmbeddings(
      final Continuation<? super List<FaceEmbeddingEntity>> $completion) {
    final String _sql = "SELECT * FROM face_embeddings WHERE clusterId IS NULL";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<FaceEmbeddingEntity>>() {
      @Override
      @NonNull
      public List<FaceEmbeddingEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfEmbeddingId = CursorUtil.getColumnIndexOrThrow(_cursor, "embeddingId");
          final int _cursorIndexOfMediaId = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaId");
          final int _cursorIndexOfEmbeddingData = CursorUtil.getColumnIndexOrThrow(_cursor, "embeddingData");
          final int _cursorIndexOfClusterId = CursorUtil.getColumnIndexOrThrow(_cursor, "clusterId");
          final List<FaceEmbeddingEntity> _result = new ArrayList<FaceEmbeddingEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FaceEmbeddingEntity _item;
            final long _tmpEmbeddingId;
            _tmpEmbeddingId = _cursor.getLong(_cursorIndexOfEmbeddingId);
            final long _tmpMediaId;
            _tmpMediaId = _cursor.getLong(_cursorIndexOfMediaId);
            final byte[] _tmpEmbeddingData;
            _tmpEmbeddingData = _cursor.getBlob(_cursorIndexOfEmbeddingData);
            final Long _tmpClusterId;
            if (_cursor.isNull(_cursorIndexOfClusterId)) {
              _tmpClusterId = null;
            } else {
              _tmpClusterId = _cursor.getLong(_cursorIndexOfClusterId);
            }
            _item = new FaceEmbeddingEntity(_tmpEmbeddingId,_tmpMediaId,_tmpEmbeddingData,_tmpClusterId);
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
  public Object getEmbeddingsForCluster(final long clusterId,
      final Continuation<? super List<FaceEmbeddingEntity>> $completion) {
    final String _sql = "SELECT * FROM face_embeddings WHERE clusterId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, clusterId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<FaceEmbeddingEntity>>() {
      @Override
      @NonNull
      public List<FaceEmbeddingEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfEmbeddingId = CursorUtil.getColumnIndexOrThrow(_cursor, "embeddingId");
          final int _cursorIndexOfMediaId = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaId");
          final int _cursorIndexOfEmbeddingData = CursorUtil.getColumnIndexOrThrow(_cursor, "embeddingData");
          final int _cursorIndexOfClusterId = CursorUtil.getColumnIndexOrThrow(_cursor, "clusterId");
          final List<FaceEmbeddingEntity> _result = new ArrayList<FaceEmbeddingEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FaceEmbeddingEntity _item;
            final long _tmpEmbeddingId;
            _tmpEmbeddingId = _cursor.getLong(_cursorIndexOfEmbeddingId);
            final long _tmpMediaId;
            _tmpMediaId = _cursor.getLong(_cursorIndexOfMediaId);
            final byte[] _tmpEmbeddingData;
            _tmpEmbeddingData = _cursor.getBlob(_cursorIndexOfEmbeddingData);
            final Long _tmpClusterId;
            if (_cursor.isNull(_cursorIndexOfClusterId)) {
              _tmpClusterId = null;
            } else {
              _tmpClusterId = _cursor.getLong(_cursorIndexOfClusterId);
            }
            _item = new FaceEmbeddingEntity(_tmpEmbeddingId,_tmpMediaId,_tmpEmbeddingData,_tmpClusterId);
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
  public Flow<List<FaceClusterEntity>> getAllClusters() {
    final String _sql = "SELECT * FROM face_clusters";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"face_clusters"}, new Callable<List<FaceClusterEntity>>() {
      @Override
      @NonNull
      public List<FaceClusterEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfClusterId = CursorUtil.getColumnIndexOrThrow(_cursor, "clusterId");
          final int _cursorIndexOfPersonName = CursorUtil.getColumnIndexOrThrow(_cursor, "personName");
          final List<FaceClusterEntity> _result = new ArrayList<FaceClusterEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FaceClusterEntity _item;
            final long _tmpClusterId;
            _tmpClusterId = _cursor.getLong(_cursorIndexOfClusterId);
            final String _tmpPersonName;
            if (_cursor.isNull(_cursorIndexOfPersonName)) {
              _tmpPersonName = null;
            } else {
              _tmpPersonName = _cursor.getString(_cursorIndexOfPersonName);
            }
            _item = new FaceClusterEntity(_tmpClusterId,_tmpPersonName);
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
  public Object getAllClustersSync(
      final Continuation<? super List<FaceClusterEntity>> $completion) {
    final String _sql = "SELECT * FROM face_clusters";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<FaceClusterEntity>>() {
      @Override
      @NonNull
      public List<FaceClusterEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfClusterId = CursorUtil.getColumnIndexOrThrow(_cursor, "clusterId");
          final int _cursorIndexOfPersonName = CursorUtil.getColumnIndexOrThrow(_cursor, "personName");
          final List<FaceClusterEntity> _result = new ArrayList<FaceClusterEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FaceClusterEntity _item;
            final long _tmpClusterId;
            _tmpClusterId = _cursor.getLong(_cursorIndexOfClusterId);
            final String _tmpPersonName;
            if (_cursor.isNull(_cursorIndexOfPersonName)) {
              _tmpPersonName = null;
            } else {
              _tmpPersonName = _cursor.getString(_cursorIndexOfPersonName);
            }
            _item = new FaceClusterEntity(_tmpClusterId,_tmpPersonName);
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
  public Object getTagsForMedia(final long mediaId,
      final Continuation<? super List<MediaTagEntity>> $completion) {
    final String _sql = "SELECT * FROM media_tags WHERE mediaId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, mediaId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MediaTagEntity>>() {
      @Override
      @NonNull
      public List<MediaTagEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfTagId = CursorUtil.getColumnIndexOrThrow(_cursor, "tagId");
          final int _cursorIndexOfMediaId = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaId");
          final int _cursorIndexOfTag = CursorUtil.getColumnIndexOrThrow(_cursor, "tag");
          final int _cursorIndexOfConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "confidence");
          final List<MediaTagEntity> _result = new ArrayList<MediaTagEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MediaTagEntity _item;
            final long _tmpTagId;
            _tmpTagId = _cursor.getLong(_cursorIndexOfTagId);
            final long _tmpMediaId;
            _tmpMediaId = _cursor.getLong(_cursorIndexOfMediaId);
            final String _tmpTag;
            _tmpTag = _cursor.getString(_cursorIndexOfTag);
            final float _tmpConfidence;
            _tmpConfidence = _cursor.getFloat(_cursorIndexOfConfidence);
            _item = new MediaTagEntity(_tmpTagId,_tmpMediaId,_tmpTag,_tmpConfidence);
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
  public Flow<List<MediaEntity>> searchMedia(final String query) {
    final String _sql = "\n"
            + "        SELECT DISTINCT m.* FROM media_items m\n"
            + "        LEFT JOIN media_tags t ON m.id = t.mediaId\n"
            + "        LEFT JOIN face_embeddings e ON m.id = e.mediaId\n"
            + "        LEFT JOIN face_clusters c ON e.clusterId = c.clusterId\n"
            + "        WHERE \n"
            + "            m.name LIKE '%' || ? || '%' OR \n"
            + "            t.tag LIKE '%' || ? || '%' OR \n"
            + "            c.personName LIKE '%' || ? || '%'\n"
            + "        ORDER BY m.dateAdded DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, query);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    _argIndex = 3;
    _statement.bindString(_argIndex, query);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"media_items", "media_tags",
        "face_embeddings", "face_clusters"}, new Callable<List<MediaEntity>>() {
      @Override
      @NonNull
      public List<MediaEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUriString = CursorUtil.getColumnIndexOrThrow(_cursor, "uriString");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDateAdded = CursorUtil.getColumnIndexOrThrow(_cursor, "dateAdded");
          final int _cursorIndexOfBucketName = CursorUtil.getColumnIndexOrThrow(_cursor, "bucketName");
          final int _cursorIndexOfMimeType = CursorUtil.getColumnIndexOrThrow(_cursor, "mimeType");
          final int _cursorIndexOfIsVideo = CursorUtil.getColumnIndexOrThrow(_cursor, "isVideo");
          final int _cursorIndexOfIsBackedUp = CursorUtil.getColumnIndexOrThrow(_cursor, "isBackedUp");
          final int _cursorIndexOfLastScanned = CursorUtil.getColumnIndexOrThrow(_cursor, "lastScanned");
          final List<MediaEntity> _result = new ArrayList<MediaEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MediaEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpUriString;
            _tmpUriString = _cursor.getString(_cursorIndexOfUriString);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpDateAdded;
            _tmpDateAdded = _cursor.getLong(_cursorIndexOfDateAdded);
            final String _tmpBucketName;
            _tmpBucketName = _cursor.getString(_cursorIndexOfBucketName);
            final String _tmpMimeType;
            _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
            final boolean _tmpIsVideo;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsVideo);
            _tmpIsVideo = _tmp != 0;
            final boolean _tmpIsBackedUp;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsBackedUp);
            _tmpIsBackedUp = _tmp_1 != 0;
            final long _tmpLastScanned;
            _tmpLastScanned = _cursor.getLong(_cursorIndexOfLastScanned);
            _item = new MediaEntity(_tmpId,_tmpUriString,_tmpName,_tmpDateAdded,_tmpBucketName,_tmpMimeType,_tmpIsVideo,_tmpIsBackedUp,_tmpLastScanned);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
