package com.sahaduta.telegrambackup.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class GalleryDatabase_Impl extends GalleryDatabase {
  private volatile GalleryDao _galleryDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `media_items` (`id` INTEGER NOT NULL, `uriString` TEXT NOT NULL, `name` TEXT NOT NULL, `dateAdded` INTEGER NOT NULL, `bucketName` TEXT NOT NULL, `mimeType` TEXT NOT NULL, `isVideo` INTEGER NOT NULL, `isBackedUp` INTEGER NOT NULL, `lastScanned` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `face_clusters` (`clusterId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `personName` TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `face_embeddings` (`embeddingId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `mediaId` INTEGER NOT NULL, `embeddingData` BLOB NOT NULL, `clusterId` INTEGER, FOREIGN KEY(`mediaId`) REFERENCES `media_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`clusterId`) REFERENCES `face_clusters`(`clusterId`) ON UPDATE NO ACTION ON DELETE SET NULL )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_face_embeddings_mediaId` ON `face_embeddings` (`mediaId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_face_embeddings_clusterId` ON `face_embeddings` (`clusterId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `media_tags` (`tagId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `mediaId` INTEGER NOT NULL, `tag` TEXT NOT NULL, `confidence` REAL NOT NULL, FOREIGN KEY(`mediaId`) REFERENCES `media_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_media_tags_mediaId` ON `media_tags` (`mediaId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_media_tags_tag` ON `media_tags` (`tag`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'fd5566c3b2502be879cc651d1be56d6e')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `media_items`");
        db.execSQL("DROP TABLE IF EXISTS `face_clusters`");
        db.execSQL("DROP TABLE IF EXISTS `face_embeddings`");
        db.execSQL("DROP TABLE IF EXISTS `media_tags`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsMediaItems = new HashMap<String, TableInfo.Column>(9);
        _columnsMediaItems.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaItems.put("uriString", new TableInfo.Column("uriString", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaItems.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaItems.put("dateAdded", new TableInfo.Column("dateAdded", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaItems.put("bucketName", new TableInfo.Column("bucketName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaItems.put("mimeType", new TableInfo.Column("mimeType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaItems.put("isVideo", new TableInfo.Column("isVideo", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaItems.put("isBackedUp", new TableInfo.Column("isBackedUp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaItems.put("lastScanned", new TableInfo.Column("lastScanned", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMediaItems = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMediaItems = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMediaItems = new TableInfo("media_items", _columnsMediaItems, _foreignKeysMediaItems, _indicesMediaItems);
        final TableInfo _existingMediaItems = TableInfo.read(db, "media_items");
        if (!_infoMediaItems.equals(_existingMediaItems)) {
          return new RoomOpenHelper.ValidationResult(false, "media_items(com.sahaduta.telegrambackup.data.MediaEntity).\n"
                  + " Expected:\n" + _infoMediaItems + "\n"
                  + " Found:\n" + _existingMediaItems);
        }
        final HashMap<String, TableInfo.Column> _columnsFaceClusters = new HashMap<String, TableInfo.Column>(2);
        _columnsFaceClusters.put("clusterId", new TableInfo.Column("clusterId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFaceClusters.put("personName", new TableInfo.Column("personName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFaceClusters = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesFaceClusters = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoFaceClusters = new TableInfo("face_clusters", _columnsFaceClusters, _foreignKeysFaceClusters, _indicesFaceClusters);
        final TableInfo _existingFaceClusters = TableInfo.read(db, "face_clusters");
        if (!_infoFaceClusters.equals(_existingFaceClusters)) {
          return new RoomOpenHelper.ValidationResult(false, "face_clusters(com.sahaduta.telegrambackup.data.FaceClusterEntity).\n"
                  + " Expected:\n" + _infoFaceClusters + "\n"
                  + " Found:\n" + _existingFaceClusters);
        }
        final HashMap<String, TableInfo.Column> _columnsFaceEmbeddings = new HashMap<String, TableInfo.Column>(4);
        _columnsFaceEmbeddings.put("embeddingId", new TableInfo.Column("embeddingId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFaceEmbeddings.put("mediaId", new TableInfo.Column("mediaId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFaceEmbeddings.put("embeddingData", new TableInfo.Column("embeddingData", "BLOB", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFaceEmbeddings.put("clusterId", new TableInfo.Column("clusterId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFaceEmbeddings = new HashSet<TableInfo.ForeignKey>(2);
        _foreignKeysFaceEmbeddings.add(new TableInfo.ForeignKey("media_items", "CASCADE", "NO ACTION", Arrays.asList("mediaId"), Arrays.asList("id")));
        _foreignKeysFaceEmbeddings.add(new TableInfo.ForeignKey("face_clusters", "SET NULL", "NO ACTION", Arrays.asList("clusterId"), Arrays.asList("clusterId")));
        final HashSet<TableInfo.Index> _indicesFaceEmbeddings = new HashSet<TableInfo.Index>(2);
        _indicesFaceEmbeddings.add(new TableInfo.Index("index_face_embeddings_mediaId", false, Arrays.asList("mediaId"), Arrays.asList("ASC")));
        _indicesFaceEmbeddings.add(new TableInfo.Index("index_face_embeddings_clusterId", false, Arrays.asList("clusterId"), Arrays.asList("ASC")));
        final TableInfo _infoFaceEmbeddings = new TableInfo("face_embeddings", _columnsFaceEmbeddings, _foreignKeysFaceEmbeddings, _indicesFaceEmbeddings);
        final TableInfo _existingFaceEmbeddings = TableInfo.read(db, "face_embeddings");
        if (!_infoFaceEmbeddings.equals(_existingFaceEmbeddings)) {
          return new RoomOpenHelper.ValidationResult(false, "face_embeddings(com.sahaduta.telegrambackup.data.FaceEmbeddingEntity).\n"
                  + " Expected:\n" + _infoFaceEmbeddings + "\n"
                  + " Found:\n" + _existingFaceEmbeddings);
        }
        final HashMap<String, TableInfo.Column> _columnsMediaTags = new HashMap<String, TableInfo.Column>(4);
        _columnsMediaTags.put("tagId", new TableInfo.Column("tagId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaTags.put("mediaId", new TableInfo.Column("mediaId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaTags.put("tag", new TableInfo.Column("tag", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaTags.put("confidence", new TableInfo.Column("confidence", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMediaTags = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysMediaTags.add(new TableInfo.ForeignKey("media_items", "CASCADE", "NO ACTION", Arrays.asList("mediaId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesMediaTags = new HashSet<TableInfo.Index>(2);
        _indicesMediaTags.add(new TableInfo.Index("index_media_tags_mediaId", false, Arrays.asList("mediaId"), Arrays.asList("ASC")));
        _indicesMediaTags.add(new TableInfo.Index("index_media_tags_tag", false, Arrays.asList("tag"), Arrays.asList("ASC")));
        final TableInfo _infoMediaTags = new TableInfo("media_tags", _columnsMediaTags, _foreignKeysMediaTags, _indicesMediaTags);
        final TableInfo _existingMediaTags = TableInfo.read(db, "media_tags");
        if (!_infoMediaTags.equals(_existingMediaTags)) {
          return new RoomOpenHelper.ValidationResult(false, "media_tags(com.sahaduta.telegrambackup.data.MediaTagEntity).\n"
                  + " Expected:\n" + _infoMediaTags + "\n"
                  + " Found:\n" + _existingMediaTags);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "fd5566c3b2502be879cc651d1be56d6e", "cbb1dcdc47153f8638933a83d62ce191");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "media_items","face_clusters","face_embeddings","media_tags");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `media_items`");
      _db.execSQL("DELETE FROM `face_clusters`");
      _db.execSQL("DELETE FROM `face_embeddings`");
      _db.execSQL("DELETE FROM `media_tags`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(GalleryDao.class, GalleryDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public GalleryDao galleryDao() {
    if (_galleryDao != null) {
      return _galleryDao;
    } else {
      synchronized(this) {
        if(_galleryDao == null) {
          _galleryDao = new GalleryDao_Impl(this);
        }
        return _galleryDao;
      }
    }
  }
}
