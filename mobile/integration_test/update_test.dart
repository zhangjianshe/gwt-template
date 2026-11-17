import 'package:cangling_mobile/features/update/update_button.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:material_ui/material_ui.dart';
import 'package:package_info_plus/package_info_plus.dart';

// Run after publishing 0.2.0+2 to the development software repository.
void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();
  testWidgets('older installed version receives the published APK offer', (
    tester,
  ) async {
    PackageInfo.setMockInitialValues(
      appName: 'cangling mobile',
      packageName: 'cn.mapway.cangling_mobile',
      version: '0.1.0',
      buildNumber: '1',
      buildSignature: '',
    );
    await tester.pumpWidget(
      const MaterialApp(home: Scaffold(body: UpdateButton())),
    );
    await tester.tap(find.text('检查更新'));
    await tester.pump();
    expect(find.text('正在检查更新…'), findsOneWidget);
    // Wait for the real repository response, without an indeterminate spinner blocking settle.
    for (var i = 0; i < 50 && find.text('下载更新').evaluate().isEmpty; i++) {
      await tester.pump(const Duration(milliseconds: 500));
    }
    expect(find.textContaining('发现新版本'), findsOneWidget);
    expect(find.text('下载更新'), findsOneWidget);
    await tester.tap(find.text('稍后'));
    await tester.pumpAndSettle();
    expect(find.text('检查更新'), findsOneWidget);
  });
}
