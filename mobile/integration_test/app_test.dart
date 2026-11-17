import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:integration_test/integration_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:cangling_mobile/main.dart' as app;

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('Android starts and validates login before sending credentials', (
    tester,
  ) async {
    await app.main();
    await tester.pumpAndSettle();
    expect(find.text('cangling mobile'), findsOneWidget);
    expect(find.textContaining('https://soft.cangling.cn'), findsNothing);
    expect(find.byType(TextFormField), findsNWidgets(2));
    final login = find.widgetWithText(FilledButton, '登录');
    await tester.ensureVisible(login);
    await tester.tap(login);
    await tester.pumpAndSettle();
    expect(find.text('请输入用户名'), findsOneWidget);
    expect(find.text('请输入密码'), findsOneWidget);

    const storage = FlutterSecureStorage();
    const key = 'cangling_local_smoke_test';
    try {
      await storage.write(key: key, value: 'roundtrip');
      expect(await storage.read(key: key), 'roundtrip');
    } finally {
      await storage.delete(key: key);
    }
  });
}
