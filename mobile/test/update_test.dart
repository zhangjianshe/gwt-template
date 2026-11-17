import 'package:flutter_test/flutter_test.dart';
import 'package:cangling_mobile/features/update/update_button.dart';

void main() {
  test('numeric version and build ordering', () {
    expect(compareVersions('0.10.0+1', '0.2.0+2'), greaterThan(0));
    expect(compareVersions('0.2.0+3', '0.2.0+2'), greaterThan(0));
    expect(compareVersions('0.2.0+2', '0.2.0+2'), 0);
    expect(releaseVersion('latest'), isNull);
    expect(releaseVersion('0.3.0-beta'), isNull);
  });
  test('only mobile stable universal Android APKs are offered', () {
    Map<String, dynamic> file(
      String version, {
      String os = 'android',
      String url = '/software/mobile/0.2.0/app.apk',
    }) => {'version': version, 'os': os, 'arch': 'all', 'url': url};
    expect(
      newestAndroidRelease([
        {
          'code': 'mobile',
          'files': [
            file('0.2.0+2'),
            file('0.10.0+3'),
            file('latest'),
            file('1.0.0+9', os: 'ios'),
            file('2.0.0+8', url: '/software/other/evil.apk'),
          ],
        },
      ])?['version'],
      '0.10.0+3',
    );
    expect(newestAndroidRelease([]), isNull);
  });
}
