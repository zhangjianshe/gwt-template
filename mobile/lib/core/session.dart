import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'api_client.dart';
import 'local_store.dart';

class Session extends ChangeNotifier {
  Session({LocalStore? store}) : store = store ?? LocalStore();
  static const defaultServer = 'https://soft.cangling.cn:22002';
  final LocalStore store;
  final _storage = const FlutterSecureStorage();
  String server = defaultServer;
  String? token;
  UserProfile? user;
  String get name => user?.displayName ?? '';
  bool get signedIn => token != null;
  ApiClient client() => ApiClient(serverUri(server), token: token);

  Future<void> restore() async {
    // Preferences are read only for upgrading the initial prototype, never for new writes.
    var savedServer = await store.setting('server');
    if (savedServer == null) {
      final prefs = await SharedPreferences.getInstance();
      final legacy = prefs.getString('server');
      try {
        savedServer = serverUri(legacy ?? defaultServer).toString();
      } on ApiException {
        savedServer = serverUri(defaultServer).toString();
      }
      await store.setSetting('server', savedServer);
      await prefs.remove('server');
    }
    server = savedServer;
    user = await store.currentUser();
    final saved = await _storage.read(key: 'session');
    if (saved == null) return;
    try {
      final data = jsonDecode(saved) as Map<String, dynamic>;
      final value = data['token'];
      if (data['server'] != server || value is! String || value.isEmpty) return;
      // Upgrade the old secure-storage record without discarding an existing login.
      if (user == null && data['userId'] == null && data['name'] is String) {
        user = UserProfile(
          server: server,
          userId: '',
          username: '',
          nickname: data['name'] as String,
        );
        await store.saveLogin(user!);
      }
      if (user?.server != server ||
          user?.userId != (data['userId']?.toString() ?? '')) {
        return;
      }
      if (data.containsKey('name')) {
        await _storage.write(
          key: 'session',
          value: jsonEncode({
            'server': server,
            'userId': user!.userId,
            'token': value,
          }),
        );
      }
      token = value;
    } on FormatException {
      await _storage.delete(key: 'session');
    } on TypeError {
      await _storage.delete(key: 'session');
    }
  }

  Future<void> login(String address, String username, String password) async {
    final base = serverUri(address);
    final api = ApiClient(base);
    try {
      final result = await api.post('api/v1/user/login', {
        'userName': username.trim(),
        'password': password,
      });
      final value = result['token'];
      if (value is! String || value.isEmpty || result['userId'] == null) {
        throw const ApiException('登录结果缺少访问凭据或用户信息');
      }
      final profile = UserProfile(
        server: base.toString(),
        userId: result['userId'].toString(),
        username: result['userName']?.toString() ?? username.trim(),
        nickname: result['nickName']?.toString() ?? '',
      );
      final previous = await _storage.read(key: 'session');
      await _storage.write(
        key: 'session',
        value: jsonEncode({
          'server': profile.server,
          'userId': profile.userId,
          'token': value,
        }),
      );
      try {
        await store.saveLogin(profile);
      } catch (_) {
        // SQLite and secure storage cannot share a transaction; restore the old credential on failure.
        if (previous == null) {
          await _storage.delete(key: 'session');
        } else {
          await _storage.write(key: 'session', value: previous);
        }
        rethrow;
      }
      server = profile.server;
      token = value;
      user = profile;
      notifyListeners();
    } finally {
      api.close();
    }
  }

  Future<void> logout() async {
    await _storage.delete(key: 'session');
    try {
      await store.clearCurrentUser();
    } finally {
      token = null;
      user = null;
      notifyListeners();
    }
  }
}
