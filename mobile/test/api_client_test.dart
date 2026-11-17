import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:cangling_mobile/core/api_client.dart';
import 'package:cangling_mobile/features/software/models.dart';

void main() {
  test('normalizes a server path and rejects unsafe server addresses', () {
    expect(
      serverUri('https://example.com/service').toString(),
      'https://example.com/service/',
    );
    for (final address in [
      'http://example.com',
      'https://user:pass@example.com',
      'https://example.com?token=x',
      'invalid',
    ]) {
      expect(() => serverUri(address), throwsA(isA<ApiException>()));
    }
  });
  test('uses backend API-TOKEN and JSON envelope contract', () async {
    final api = ApiClient(
      serverUri('https://example.com/'),
      token: 'test-token',
      client: MockClient((request) async {
        expect(request.headers['API-TOKEN'], 'test-token');
        expect(jsonDecode(request.body), {'softwareId': '123'});
        expect(request.url.path, '/api/v1/software/querySoftwareFiles');
        return http.Response(
          jsonEncode({
            'code': 200,
            'data': {'files': []},
          }),
          200,
        );
      }),
    );
    addTearDown(api.close);
    expect(
      await api.post('api/v1/software/querySoftwareFiles', {
        'softwareId': '123',
      }),
      {'files': []},
    );
  });
  test('surfaces business authentication failures', () async {
    final api = ApiClient(
      serverUri('https://example.com'),
      client: MockClient(
        (_) async => http.Response('{"code":401,"message":"expired"}', 200),
      ),
    );
    addTearDown(api.close);
    await expectLater(
      api.post('api/test'),
      throwsA(isA<ApiException>().having((e) => e.code, 'code', 401)),
    );
  });
  test('malformed server responses become actionable errors', () async {
    final api = ApiClient(
      serverUri('https://example.com'),
      client: MockClient((_) async => http.Response('<html>error</html>', 200)),
    );
    addTearDown(api.close);
    await expectLater(api.post('api/test'), throwsA(isA<ApiException>()));
  });
  test(
    'search covers name, group and code; downloads reject non HTTPS schemes',
    () {
      final software = Software.fromJson({
        'id': '1',
        'name': 'Keeper',
        'softwareSet': '系统运维',
        'code': 'operator',
      });
      expect(software.matches('KEEP'), isTrue);
      expect(software.matches('系统'), isTrue);
      expect(software.matches('operator'), isTrue);
      final file = SoftwareFile.fromJson({
        'location': '/software/app.apk',
        'size': 1024,
      });
      expect(file.sizeLabel, '1.0 KB');
      expect(
        file.downloadUri(serverUri('https://example.com'))?.path,
        '/software/app.apk',
      );
      expect(
        SoftwareFile.fromJson({'location': 'javascript:alert(1)'})
            .downloadUri(serverUri('https://example.com')),
        isNull,
      );
    },
  );
}
