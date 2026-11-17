import 'dart:convert';
import 'dart:io';

import 'package:http/http.dart' as http;
import 'package:material_ui/material_ui.dart';
import 'package:package_info_plus/package_info_plus.dart';
import 'package:url_launcher/url_launcher.dart';

const updateOrigin = 'https://soft.cangling.cn:22002';

/// Stable numeric versions only; `latest` and prereleases never replace a release.
List<int>? releaseVersion(String value) {
  final match = RegExp(r'^v?(\d+)\.(\d+)\.(\d+)(?:\+(\d+))?$')
      .firstMatch(value);
  return match == null
      ? null
      : [for (var i = 1; i <= 4; i++) int.parse(match[i] ?? '0')];
}

int compareVersions(String a, String b) {
  final left = releaseVersion(a), right = releaseVersion(b);
  if (left == null || right == null) return 0;
  for (var i = 0; i < left.length; i++) {
    final result = left[i].compareTo(right[i]);
    if (result != 0) return result;
  }
  return 0;
}

Map<String, dynamic>? newestAndroidRelease(List<dynamic> softwares) {
  final files = <Map<String, dynamic>>[];
  for (final software in softwares.whereType<Map<String, dynamic>>()) {
    if (software['code'] != 'mobile') continue;
    for (final file
        in (software['files'] as List? ?? [])
            .whereType<Map<String, dynamic>>()) {
      final uri = Uri.tryParse('$updateOrigin${file['url']}');
      if (file['os'] == 'android' &&
          file['arch'] == 'all' &&
          releaseVersion('${file['version']}') != null &&
          uri != null &&
          uri.origin == updateOrigin &&
          uri.path.startsWith('/software/mobile/') &&
          uri.path.endsWith('.apk')) {
        files.add(file);
      }
    }
  }
  files.sort((a, b) => compareVersions('${b['version']}', '${a['version']}'));
  return files.firstOrNull;
}

class UpdateButton extends StatefulWidget {
  const UpdateButton({super.key});
  @override
  State<UpdateButton> createState() => _UpdateButtonState();
}

class _UpdateButtonState extends State<UpdateButton> {
  bool busy = false;
  Future<void> check() async {
    if (busy) return;
    setState(() => busy = true);
    try {
      final info = await PackageInfo.fromPlatform();
      if (!Platform.isAndroid) throw Exception('当前平台请通过应用商店更新');
      final response = await http
          .get(
            Uri.parse(
              '$updateOrigin/api/v1/software/manifest?set=${Uri.encodeQueryComponent('系统运维')}',
            ),
          )
          .timeout(const Duration(seconds: 20));
      if (response.statusCode != 200) throw Exception('更新服务暂时不可用');
      final body =
          jsonDecode(utf8.decode(response.bodyBytes)) as Map<String, dynamic>;
      if (body['code'] != 200) throw Exception('无法获取更新信息');
      final release = newestAndroidRelease(body['data']['softwares'] as List);
      if (!mounted) return;
      final current = '${info.version}+${info.buildNumber}';
      if (release == null ||
          compareVersions('${release['version']}', current) <= 0) {
        ScaffoldMessenger.of(context)
            .showSnackBar(SnackBar(content: Text('当前已是最新版本（$current）')));
        return;
      }
      final download = await showDialog<bool>(
        context: context,
        builder: (context) => AlertDialog(
          title: Text('发现新版本 ${release['version']}'),
          content: Text(
            '当前版本：$current\n\n${release['summary'] ?? ''}\n\n将打开浏览器下载 APK，下载完成后点击文件安装。',
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context, false),
              child: const Text('稍后'),
            ),
            FilledButton(
              onPressed: () => Navigator.pop(context, true),
              child: const Text('下载更新'),
            ),
          ],
        ),
      );
      if (download == true &&
          !await launchUrl(
            Uri.parse('$updateOrigin${release['url']}'),
            mode: LaunchMode.externalApplication,
          )) {
        throw Exception('无法打开下载链接');
      }
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context)
            .showSnackBar(const SnackBar(content: Text('检查更新失败，请检查网络后重试')));
      }
    } finally {
      if (mounted) setState(() => busy = false);
    }
  }

  @override
  Widget build(BuildContext context) => TextButton.icon(
    onPressed: busy ? null : check,
    icon: busy
        ? const SizedBox(
            width: 16,
            height: 16,
            child: CircularProgressIndicator(strokeWidth: 2),
          )
        : const Icon(Icons.system_update),
    label: Text(busy ? '正在检查更新…' : '检查更新'),
  );
}
