import 'package:cupertino_ui/cupertino_ui.dart';
import 'package:material_ui/material_ui.dart';
import 'package:flutter_localizations/flutter_localizations.dart'
    show GlobalWidgetsLocalizations;

import 'core/session.dart';
import 'features/update/update_button.dart';
import 'features/auth/login_page.dart';
import 'features/software/software_pages.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final session = Session();
  String? startupError;
  try {
    await session.restore();
  } catch (_) {
    startupError = '无法恢复登录状态，请重新登录';
  }
  runApp(CanglingApp(session: session, startupError: startupError));
}

class CanglingApp extends StatelessWidget {
  const CanglingApp({super.key, required this.session, this.startupError});
  final Session session;
  final String? startupError;
  @override
  Widget build(BuildContext context) => ListenableBuilder(
    listenable: session,
    builder: (context, _) => MaterialApp(
      key: ValueKey(session.signedIn),
      title: 'cangling mobile',
      debugShowCheckedModeBanner: false,
      locale: const Locale('zh', 'CN'),
      supportedLocales: const [Locale('zh', 'CN'), Locale('en')],
      localizationsDelegates: const [
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xff4163b7)),
        scaffoldBackgroundColor: const Color(0xfff6f8fc),
        inputDecorationTheme: const InputDecorationTheme(
          border: OutlineInputBorder(),
          filled: true,
          fillColor: Colors.white,
        ),
      ),
      home: session.signedIn
          ? HomePage(session: session)
          : LoginPage(session: session),
      builder: (context, child) => Column(
        children: [
          if (startupError != null && !session.signedIn)
            SafeArea(
              bottom: false,
              child: Material(
                child: Padding(
                  padding: const EdgeInsets.all(12),
                  child: Text(startupError!, textDirection: TextDirection.ltr),
                ),
              ),
            ),
          Expanded(child: child!),
        ],
      ),
    ),
  );
}

class HomePage extends StatefulWidget {
  const HomePage({super.key, required this.session});
  final Session session;
  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  int _index = 0;
  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(title: Text(['工作台', '软件仓库', '我的'][_index])),
    body: IndexedStack(
      index: _index,
      children: [
        ListView(
          padding: const EdgeInsets.all(20),
          children: [
            Text(
              '你好，${widget.session.name}',
              style: Theme.of(context).textTheme.headlineSmall,
            ),
            const SizedBox(height: 8),
            const Text('随时访问研发资源'),
            const SizedBox(height: 24),
            Card(
              child: ListTile(
                contentPadding: const EdgeInsets.all(20),
                leading: const Icon(Icons.inventory_2_outlined),
                title: const Text('软件仓库'),
                subtitle: const Text('搜索软件、查看版本和下载文件'),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => setState(() => _index = 1),
              ),
            ),
          ],
        ),
        SoftwarePage(session: widget.session),
        ListView(
          padding: const EdgeInsets.all(20),
          children: [
            Card(
              child: Column(
                children: [
                  ListTile(
                    leading: const Icon(Icons.person_outline),
                    title: Text(widget.session.name),
                    subtitle: Text(widget.session.user?.username ?? ''),
                  ),
                  ListTile(
                    leading: const Icon(Icons.dns_outlined),
                    title: const Text('当前服务器'),
                    subtitle: Text(widget.session.server),
                  ),
                  const ListTile(
                    leading: Icon(Icons.info_outline),
                    title: Text('cangling mobile'),
                    subtitle: Text('Android / iOS'),
                  ),
                ],
              ),
            ),
            const UpdateButton(),
            const SizedBox(height: 24),
            OutlinedButton.icon(
              onPressed: () async {
                try {
                  await widget.session.logout();
                } catch (_) {
                  if (context.mounted) {
                    ScaffoldMessenger.of(
                      context,
                    ).showSnackBar(const SnackBar(content: Text('退出失败，请重试')));
                  }
                }
              },
              icon: const Icon(Icons.logout),
              label: const Text('退出登录 / 切换服务器'),
            ),
          ],
        ),
      ],
    ),
    bottomNavigationBar: NavigationBar(
      selectedIndex: _index,
      onDestinationSelected: (i) => setState(() => _index = i),
      destinations: const [
        NavigationDestination(
          icon: Icon(Icons.dashboard_outlined),
          selectedIcon: Icon(Icons.dashboard),
          label: '工作台',
        ),
        NavigationDestination(
          icon: Icon(Icons.inventory_2_outlined),
          selectedIcon: Icon(Icons.inventory_2),
          label: '软件仓库',
        ),
        NavigationDestination(
          icon: Icon(Icons.person_outline),
          selectedIcon: Icon(Icons.person),
          label: '我的',
        ),
      ],
    ),
  );
}
