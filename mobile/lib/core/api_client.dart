import 'dart:convert';

import 'package:http/http.dart' as http;

class ApiException implements Exception {
  const ApiException(this.message, {this.code});
  final String message;
  final int? code;
  bool get unauthorized => code == 401 || code == 403;
  @override
  String toString() => message;
}

Uri serverUri(String input) {
  final uri = Uri.tryParse(input.trim());
  if (uri == null ||
      uri.scheme != 'https' ||
      uri.host.isEmpty ||
      uri.userInfo.isNotEmpty ||
      uri.hasQuery ||
      uri.hasFragment) {
    throw const ApiException('请输入有效的 HTTPS 服务器地址');
  }
  return uri.replace(path: '${uri.path.replaceFirst(RegExp(r'/+$'), '')}/');
}

class ApiClient {
  ApiClient(this.baseUrl, {this.token, http.Client? client})
    : _client = client ?? http.Client();
  final Uri baseUrl;
  final String? token;
  final http.Client _client;

  Future<Map<String, dynamic>> post(
    String path, [
    Map<String, dynamic>? body,
  ]) async {
    http.Response response;
    try {
      response = await _client
          .post(
            baseUrl.resolve(path),
            headers: {
              'Content-Type': 'application/json; charset=utf-8',
              if (token != null) 'API-TOKEN': token!,
            },
            body: jsonEncode(body ?? {}),
          )
          .timeout(const Duration(seconds: 20));
    } on Exception {
      throw const ApiException('无法连接服务器，请检查网络和服务器地址');
    }
    if (response.statusCode != 200) {
      throw ApiException(
        '请求失败（${response.statusCode}）',
        code: response.statusCode,
      );
    }
    dynamic decoded;
    try {
      decoded = jsonDecode(utf8.decode(response.bodyBytes));
    } on FormatException {
      throw const ApiException('服务器返回了无效数据');
    }
    if (decoded is! Map<String, dynamic>) {
      throw const ApiException('服务器响应格式不正确');
    }
    final code = decoded['code'];
    if (code != 200) {
      throw ApiException(
        decoded['message']?.toString() ?? '请求失败',
        code: code is int ? code : null,
      );
    }
    final data = decoded['data'];
    if (data is! Map<String, dynamic>) {
      throw const ApiException('服务器响应缺少数据');
    }
    return data;
  }

  void close() => _client.close();
}
