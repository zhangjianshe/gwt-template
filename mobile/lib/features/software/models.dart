class Software {
  const Software({
    required this.id,
    required this.name,
    required this.group,
    required this.code,
    required this.summary,
  });
  factory Software.fromJson(Map<String, dynamic> json) => Software(
    id: json['id']?.toString() ?? '',
    name: json['name']?.toString() ?? '',
    group: json['softwareSet']?.toString() ?? '',
    code: json['code']?.toString() ?? '',
    summary: json['summary']?.toString() ?? '',
  );
  final String id, name, group, code, summary;
  bool matches(String value) =>
      '$name $group $code'.toLowerCase().contains(value.trim().toLowerCase());
}

class SoftwareFile {
  const SoftwareFile({
    required this.name,
    required this.version,
    required this.os,
    required this.arch,
    required this.size,
    required this.location,
    required this.summary,
    required this.hash,
  });
  factory SoftwareFile.fromJson(Map<String, dynamic> json) => SoftwareFile(
    name: json['name']?.toString() ?? '',
    version: json['version']?.toString() ?? '',
    os: json['os']?.toString() ?? '',
    arch: json['arch']?.toString() ?? '',
    size: (json['size'] as num?)?.toInt() ?? 0,
    location: json['location']?.toString() ?? '',
    summary: json['summary']?.toString() ?? '',
    hash: json['hash']?.toString() ?? '',
  );
  final String name, version, os, arch, location, summary, hash;
  final int size;
  String get sizeLabel {
    if (size < 1024) return '$size B';
    if (size < 1024 * 1024) return '${(size / 1024).toStringAsFixed(1)} KB';
    return '${(size / (1024 * 1024)).toStringAsFixed(1)} MB';
  }

  Uri? downloadUri(Uri server) {
    final uri = server.resolve(location);
    return location.isNotEmpty &&
            uri.scheme == 'https' &&
            uri.host.isNotEmpty &&
            uri.userInfo.isEmpty
        ? uri
        : null;
  }
}
