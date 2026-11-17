import 'dart:convert';
import 'dart:io';

import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';
import 'package:cangling_mobile/core/local_store.dart';
import 'package:cangling_mobile/core/session.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  sqfliteFfiInit();
  late LocalStore store;
  late Directory directory;
  setUp(() async {
    directory = await Directory.systemTemp.createTemp('cangling-sqlite-test-');
    store = LocalStore(
      factory: databaseFactoryFfi,
      databasePath: '${directory.path}/app.db',
    );
    SharedPreferences.setMockInitialValues({});
    FlutterSecureStorage.setMockInitialValues({});
  });
  tearDown(() async {
    await store.close();
    await directory.delete(recursive: true);
  });
  test(
    'configuration and user profile survive closing and reopening SQLite',
    () async {
      const user = UserProfile(
        server: 'https://example.com/',
        userId: '42',
        username: 'alice',
        nickname: 'Alice',
      );
      await store.saveLogin(user);
      await store.setSetting('theme', 'dark');
      await store.close();
      expect(await store.setting('server'), user.server);
      expect(await store.setting('theme'), 'dark');
      expect((await store.currentUser())?.userId, '42');
      final db = await store.database;
      final columns = await db.rawQuery('PRAGMA table_info(current_user)');
      expect(columns.map((r) => r['name']), isNot(contains('token')));
      await store.clearCurrentUser();
      expect(await store.currentUser(), isNull);
      expect(await store.setting('server'), user.server);
    },
  );
  test(
    'migrates old preferences and login without losing the existing session',
    () async {
      SharedPreferences.setMockInitialValues({
        'server': 'https://legacy.example.com/',
      });
      FlutterSecureStorage.setMockInitialValues({
        'session': jsonEncode({
          'server': 'https://legacy.example.com/',
          'name': 'Legacy User',
          'token': 'test-token',
        }),
      });
      final session = Session(store: store);
      addTearDown(session.dispose);
      await session.restore();
      expect(session.signedIn, isTrue);
      expect(session.name, 'Legacy User');
      expect(await store.setting('server'), session.server);
      expect(
        (await SharedPreferences.getInstance()).getString('server'),
        isNull,
      );
      final secret = jsonDecode(
        (await const FlutterSecureStorage().read(key: 'session'))!,
      );
      expect(secret.containsKey('name'), isFalse);
      // Subsequent starts use SQLite even when stale preferences exist.
      SharedPreferences.setMockInitialValues({
        'server': 'https://wrong.example.com/',
      });
      final restored = Session(store: store);
      addTearDown(restored.dispose);
      await restored.restore();
      expect(restored.name, 'Legacy User');
      await restored.logout();
      expect(restored.signedIn, isFalse);
      expect(await store.currentUser(), isNull);
      expect(await store.setting('server'), 'https://legacy.example.com/');
    },
  );
  test('does not restore credentials belonging to a different user', () async {
    await store.saveLogin(
      const UserProfile(
        server: 'https://example.com/',
        userId: '2',
        username: 'bob',
        nickname: 'Bob',
      ),
    );
    FlutterSecureStorage.setMockInitialValues({
      'session': jsonEncode({
        'server': 'https://example.com/',
        'userId': '1',
        'token': 'old-user-token',
      }),
    });
    final session = Session(store: store);
    addTearDown(session.dispose);
    await session.restore();
    expect(session.signedIn, isFalse);
  });
}
