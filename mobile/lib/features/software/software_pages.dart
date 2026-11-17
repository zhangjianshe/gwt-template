import 'package:material_ui/material_ui.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../core/api_client.dart';
import '../../core/session.dart';
import 'models.dart';

Future<List<T>> loadItems<T>(
  Session session,
  String path,
  String key,
  T Function(Map<String, dynamic>) decode, [
  Map<String, dynamic>? body,
]) async {
  final api = session.client();
  try {
    final data = await api.post(path, body);
    return ((data[key] as List?) ?? [])
        .map((e) => decode(e as Map<String, dynamic>))
        .toList();
  } on ApiException catch (e) {
    if (e.code == 401) await session.logout();
    rethrow;
  } finally {
    api.close();
  }
}

class LoadError extends StatelessWidget {
  const LoadError({super.key, required this.error, required this.retry});
  final Object error;
  final VoidCallback retry;
  @override
  Widget build(BuildContext context) => Center(
    child: Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Icon(Icons.cloud_off_outlined, size: 48),
          const SizedBox(height: 16),
          Text(error.toString(), textAlign: TextAlign.center),
          const SizedBox(height: 16),
          OutlinedButton.icon(
            onPressed: retry,
            icon: const Icon(Icons.refresh),
            label: const Text('重新加载'),
          ),
        ],
      ),
    ),
  );
}

class SoftwarePage extends StatefulWidget {
  const SoftwarePage({super.key, required this.session});
  final Session session;
  @override
  State<SoftwarePage> createState() => _SoftwarePageState();
}

class _SoftwarePageState extends State<SoftwarePage> {
  late Future<List<Software>> _future;
  String _search = '';
  @override
  void initState() {
    super.initState();
    _future = _load();
  }

  Future<List<Software>> _load() => loadItems(
    widget.session,
    'api/v1/software/querySoftware',
    'softwares',
    Software.fromJson,
  );
  Future<void> _refresh() async {
    final next = _load();
    setState(() => _future = next);
    try {
      await next;
    } catch (_) {
      /* FutureBuilder displays the error. */
    }
  }

  @override
  Widget build(BuildContext context) => Column(
    children: [
      Padding(
        padding: const EdgeInsets.fromLTRB(16, 12, 16, 8),
        child: TextField(
          decoration: const InputDecoration(
            hintText: '搜索名称、软件集或 code',
            prefixIcon: Icon(Icons.search),
          ),
          onChanged: (value) => setState(() => _search = value),
        ),
      ),
      Expanded(
        child: FutureBuilder<List<Software>>(
          future: _future,
          builder: (context, snapshot) {
            if (snapshot.connectionState != ConnectionState.done) {
              return const Center(child: CircularProgressIndicator());
            }
            if (snapshot.hasError) {
              return LoadError(error: snapshot.error!, retry: _refresh);
            }
            final items =
                snapshot.data!.where((s) => s.matches(_search)).toList()
                  ..sort((a, b) {
                    final group = a.group.compareTo(b.group);
                    return group != 0
                        ? group
                        : a.name.toLowerCase().compareTo(b.name.toLowerCase());
                  });
            return RefreshIndicator(
              onRefresh: _refresh,
              child: ListView.builder(
                physics: const AlwaysScrollableScrollPhysics(),
                padding: const EdgeInsets.all(16),
                itemCount: items.isEmpty ? 1 : items.length,
                itemBuilder: (context, index) {
                  if (items.isEmpty) {
                    return const Padding(
                      padding: EdgeInsets.all(32),
                      child: Center(child: Text('没有匹配的软件')),
                    );
                  }
                  final item = items[index];
                  return Card(
                    child: ListTile(
                      contentPadding: const EdgeInsets.all(16),
                      leading: const CircleAvatar(child: Icon(Icons.apps)),
                      title: Text(item.name),
                      subtitle: Text(
                        '${item.group.isEmpty ? '未分组' : item.group} · ${item.code}\n${item.summary}',
                      ),
                      isThreeLine: true,
                      trailing: const Icon(Icons.chevron_right),
                      onTap: () => Navigator.of(context).push(
                        MaterialPageRoute(
                          builder: (_) => SoftwareDetailPage(
                            session: widget.session,
                            software: item,
                          ),
                        ),
                      ),
                    ),
                  );
                },
              ),
            );
          },
        ),
      ),
    ],
  );
}

class SoftwareDetailPage extends StatefulWidget {
  const SoftwareDetailPage({
    super.key,
    required this.session,
    required this.software,
  });
  final Session session;
  final Software software;
  @override
  State<SoftwareDetailPage> createState() => _SoftwareDetailPageState();
}

class _SoftwareDetailPageState extends State<SoftwareDetailPage> {
  late Future<List<SoftwareFile>> _future;
  String _os = '', _arch = '';
  @override
  void initState() {
    super.initState();
    _future = _load();
  }

  Future<List<SoftwareFile>> _load() => loadItems(
    widget.session,
    'api/v1/software/querySoftwareFiles',
    'files',
    SoftwareFile.fromJson,
    {'softwareId': widget.software.id},
  );
  Future<void> _refresh() async {
    final next = _load();
    setState(() {
      _future = next;
      _os = '';
      _arch = '';
    });
    try {
      await next;
    } catch (_) {
      /* FutureBuilder displays the error. */
    }
  }

  Future<void> _download(SoftwareFile file) async {
    try {
      final uri = file.downloadUri(serverUri(widget.session.server));
      if (uri == null ||
          !await launchUrl(uri, mode: LaunchMode.externalApplication)) {
        throw const ApiException('无法打开下载链接');
      }
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context)
            .showSnackBar(const SnackBar(content: Text('无法打开下载链接，请稍后重试')));
      }
    }
  }

  Widget _filter(
    String label,
    String current,
    List<String> values,
    ValueChanged<String?> change,
  ) => SizedBox(
    width: 160,
    child: DropdownButtonFormField<String>(
      initialValue: current,
      isExpanded: true,
      decoration: InputDecoration(labelText: label),
      items: [
        const DropdownMenuItem(value: '', child: Text('全部')),
        ...values.map((v) => DropdownMenuItem(value: v, child: Text(v))),
      ],
      onChanged: change,
    ),
  );
  @override
  Widget build(BuildContext context) => Scaffold(
    appBar: AppBar(
      title: Text(widget.software.name),
      actions: [
        IconButton(
          onPressed: _refresh,
          tooltip: '刷新',
          icon: const Icon(Icons.refresh),
        ),
      ],
    ),
    body: FutureBuilder<List<SoftwareFile>>(
      future: _future,
      builder: (context, snapshot) {
        if (snapshot.connectionState != ConnectionState.done) {
          return const Center(child: CircularProgressIndicator());
        }
        if (snapshot.hasError) {
          return LoadError(error: snapshot.error!, retry: _refresh);
        }
        final all = snapshot.data!;
        final systems =
            all.map((f) => f.os).where((v) => v.isNotEmpty).toSet().toList()
              ..sort();
        final arches =
            all.map((f) => f.arch).where((v) => v.isNotEmpty).toSet().toList()
              ..sort();
        final files =
            all
                .where(
                  (f) =>
                      (_os.isEmpty || f.os == _os) &&
                      (_arch.isEmpty || f.arch == _arch),
                )
                .toList()
              ..sort((a, b) {
                final version = a.version == b.version
                    ? 0
                    : a.version == 'latest'
                    ? -1
                    : b.version == 'latest'
                    ? 1
                    : b.version.compareTo(a.version);
                return version != 0
                    ? version
                    : a.name.toLowerCase().compareTo(b.name.toLowerCase());
              });
        return RefreshIndicator(
          onRefresh: _refresh,
          child: ListView(
            physics: const AlwaysScrollableScrollPhysics(),
            padding: const EdgeInsets.all(16),
            children: [
              Text(
                widget.software.summary,
                style: Theme.of(context).textTheme.bodyLarge,
              ),
              const SizedBox(height: 20),
              Wrap(
                spacing: 12,
                runSpacing: 12,
                children: [
                  _filter(
                    '操作系统',
                    _os,
                    systems,
                    (v) => setState(() => _os = v ?? ''),
                  ),
                  _filter(
                    '架构',
                    _arch,
                    arches,
                    (v) => setState(() => _arch = v ?? ''),
                  ),
                ],
              ),
              const SizedBox(height: 20),
              if (files.isEmpty)
                const Padding(
                  padding: EdgeInsets.all(32),
                  child: Center(child: Text('暂无符合条件的文件')),
                ),
              for (int i = 0; i < files.length; i++) ...[
                if (i == 0 || files[i].version != files[i - 1].version)
                  Padding(
                    padding: const EdgeInsets.symmetric(vertical: 12),
                    child: Text(
                      files[i].version,
                      style: Theme.of(context).textTheme.titleMedium,
                    ),
                  ),
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          files[i].name,
                          style: Theme.of(context).textTheme.titleSmall,
                        ),
                        const SizedBox(height: 8),
                        Text(
                          '${files[i].os} / ${files[i].arch} · ${files[i].sizeLabel}',
                        ),
                        if (files[i].summary.isNotEmpty)
                          Padding(
                            padding: const EdgeInsets.only(top: 8),
                            child: Text(files[i].summary),
                          ),
                        if (files[i].hash.isNotEmpty)
                          Padding(
                            padding: const EdgeInsets.only(top: 8),
                            child: SelectableText(
                              'SHA-256: ${files[i].hash}',
                              style: Theme.of(context).textTheme.bodySmall,
                            ),
                          ),
                        const SizedBox(height: 12),
                        Align(
                          alignment: Alignment.centerRight,
                          child: FilledButton.tonalIcon(
                            onPressed:
                                files[i].downloadUri(
                                      serverUri(widget.session.server),
                                    ) ==
                                    null
                                ? null
                                : () => _download(files[i]),
                            icon: const Icon(Icons.open_in_browser),
                            label: const Text('浏览器下载'),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ],
          ),
        );
      },
    ),
  );
}
