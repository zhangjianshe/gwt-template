import 'package:path/path.dart' as path;
import 'package:sqflite/sqflite.dart';

class UserProfile {
  const UserProfile({
    required this.server,
    required this.userId,
    required this.username,
    required this.nickname,
  });
  final String server, userId, username, nickname;
  String get displayName => nickname.isNotEmpty ? nickname : username;

  Map<String, Object?> toRow() => {
    'id': 1,
    'server': server,
    'user_id': userId,
    'username': username,
    'nickname': nickname,
  };
  factory UserProfile.fromRow(Map<String, Object?> row) => UserProfile(
    server: row['server'] as String,
    userId: row['user_id'] as String,
    username: row['username'] as String,
    nickname: row['nickname'] as String,
  );
}

/// SQLite is the source of truth for configuration and user profile data.
/// Credentials deliberately live outside this database in platform secure storage.
class LocalStore {
  LocalStore({this.factory, this.databasePath});
  final DatabaseFactory? factory;
  final String? databasePath;
  Future<Database>? _opening;

  Future<Database> get database => _opening ??= _open();
  Future<Database> _open() async {
    try {
      final factory = this.factory ?? databaseFactory;
      final filename =
          databasePath ??
          path.join(await factory.getDatabasesPath(), 'cangling_mobile.db');
      return await factory.openDatabase(
        filename,
        options: OpenDatabaseOptions(
          version: 1,
          onCreate: (db, version) async {
            await db.execute(
              'CREATE TABLE app_settings (key TEXT PRIMARY KEY, value TEXT NOT NULL)',
            );
            await db.execute('''CREATE TABLE current_user (
            id INTEGER PRIMARY KEY CHECK (id = 1),
            server TEXT NOT NULL, user_id TEXT NOT NULL,
            username TEXT NOT NULL, nickname TEXT NOT NULL
          )''');
          },
        ),
      );
    } catch (_) {
      _opening = null;
      rethrow;
    }
  }

  Future<String?> setting(String key) async {
    final db = await database;
    final rows = await db.query(
      'app_settings',
      columns: ['value'],
      where: 'key = ?',
      whereArgs: [key],
    );
    return rows.isEmpty ? null : rows.first['value'] as String;
  }

  Future<void> setSetting(String key, String value) async {
    final db = await database;
    await db.insert('app_settings', {
      'key': key,
      'value': value,
    }, conflictAlgorithm: ConflictAlgorithm.replace);
  }

  Future<UserProfile?> currentUser() async {
    final db = await database;
    final rows = await db.query(
      'current_user',
      where: 'id = ?',
      whereArgs: [1],
    );
    return rows.isEmpty ? null : UserProfile.fromRow(rows.first);
  }

  Future<void> saveLogin(UserProfile user) async {
    final db = await database;
    await db.transaction((txn) async {
      await txn.insert('app_settings', {
        'key': 'server',
        'value': user.server,
      }, conflictAlgorithm: ConflictAlgorithm.replace);
      await txn.insert(
        'current_user',
        user.toRow(),
        conflictAlgorithm: ConflictAlgorithm.replace,
      );
    });
  }

  Future<void> clearCurrentUser() async {
    final db = await database;
    await db.delete('current_user');
  }

  Future<void> close() async {
    final pending = _opening;
    if (pending != null) await (await pending).close();
    _opening = null;
  }
}
