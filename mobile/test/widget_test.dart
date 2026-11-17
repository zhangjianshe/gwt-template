import 'package:material_ui/material_ui.dart';
import 'package:package_info_plus/package_info_plus.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:cangling_mobile/core/session.dart';
import 'package:cangling_mobile/main.dart';

void main() {
  testWidgets(
    'login validates missing username and password without making requests',
    (tester) async {
      PackageInfo.setMockInitialValues(
        appName: 'cangling mobile',
        packageName: 'cn.mapway.cangling_mobile',
        version: '0.2.0',
        buildNumber: '2',
        buildSignature: '',
      );
      final session = Session();
      addTearDown(session.dispose);
      await tester.pumpWidget(CanglingApp(session: session));
      await tester.pumpAndSettle();
      expect(find.text('cangling mobile'), findsOneWidget);
      expect(find.byType(TextFormField), findsNWidgets(2));
      expect(find.text('服务器地址'), findsNothing);
      expect(find.text('v0.2.0 (2)'), findsOneWidget);
      const buildDate = String.fromEnvironment('BUILD_DATE');
      if (buildDate.isNotEmpty) {
        expect(find.text(buildDate), findsOneWidget);
      }

      expect(
        tester.getTopLeft(find.text('检查更新')).dy,
        greaterThan(
          tester.getBottomLeft(find.widgetWithText(FilledButton, '登录')).dy,
        ),
      );
      await tester.ensureVisible(find.widgetWithText(FilledButton, '登录'));
      await tester.tap(find.widgetWithText(FilledButton, '登录'));
      await tester.pumpAndSettle();
      expect(find.text('请输入用户名'), findsOneWidget);
      expect(find.text('请输入密码'), findsOneWidget);
    },
  );
}
