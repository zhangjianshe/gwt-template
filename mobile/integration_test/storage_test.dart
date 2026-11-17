import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:path/path.dart' as path;
import 'package:sqflite/sqflite.dart';
import 'package:cangling_mobile/core/local_store.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();
  testWidgets('Android SQLite configuration persists across reopening', (
    tester,
  ) async {
    await tester.pumpWidget(
      const MaterialApp(
        home: Scaffold(body: Center(child: Text('正在验证本地数据库'))),
      ),
    );
    final filename = path.join(
      await getDatabasesPath(),
      'storage_smoke_${DateTime.now().microsecondsSinceEpoch}.db',
    );
    final store = LocalStore(databasePath: filename);
    try {
      await store.saveLogin(
        const UserProfile(
          server: 'https://test.example.com/',
          userId: '42',
          username: 'local-test',
          nickname: 'Local Test',
        ),
      );
      await store.setSetting('theme', 'dark');
      await store.close();
      expect(await store.setting('server'), 'https://test.example.com/');
      expect(await store.setting('theme'), 'dark');
      expect((await store.currentUser())?.userId, '42');
      await store.clearCurrentUser();
      expect(await store.currentUser(), isNull);
      expect(await store.setting('theme'), 'dark');
    } finally {
      await store.close();
      await deleteDatabase(filename);
    }
  });
}
