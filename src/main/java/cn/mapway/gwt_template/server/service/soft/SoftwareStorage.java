package cn.mapway.gwt_template.server.service.soft;

import cn.mapway.gwt_template.server.service.file.FileCustomUtils;
import cn.mapway.gwt_template.shared.db.SysSoftwareEntity;
import org.nutz.lang.Strings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

/**
 * Software disk path and file hash helpers.
 */
final class SoftwareStorage {
    static final Pattern CODE_PATTERN = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._-]{0,63}$");

    private SoftwareStorage() {
    }

    static String normalizeSet(String softwareSet) {
        return softwareSet == null ? "" : softwareSet.trim();
    }

    static String normalizeCode(String code) {
        return code == null ? "" : code.trim();
    }

    static boolean isValidCode(String code) {
        return Strings.isNotBlank(code) && CODE_PATTERN.matcher(code).matches();
    }

    static String fallbackCode(String name, String id) {
        StringBuilder sb = new StringBuilder();
        if (name != null) {
            for (int i = 0; i < name.length(); i++) {
                char c = name.charAt(i);
                if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '.' || c == '_' || c == '-') {
                    sb.append(c);
                } else if (c == ' ' || c == '/' || c == '\\') {
                    sb.append('-');
                }
            }
        }
        String code = sb.toString().replaceAll("-{2,}", "-").replaceAll("^[._-]+|[._-]+$", "");
        if (isValidCode(code)) {
            return code;
        }
        String seed = Strings.isBlank(id) ? "s" : id;
        if (seed.length() > 16) {
            seed = seed.substring(0, 16);
        }
        return "s" + seed.toLowerCase();
    }

    static String diskDirName(SysSoftwareEntity software) {
        String code = normalizeCode(software.getCode());
        if (isValidCode(code)) {
            return code;
        }
        return software.getId();
    }

    static File toDiskFile(String uploadRoot, String location) {
        if (Strings.isBlank(location)) {
            return null;
        }
        return new File(FileCustomUtils.concatPath(uploadRoot, location));
    }

    static String sha256Hex(File file) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 not available", e);
        }
        try (InputStream in = new FileInputStream(file);
             DigestInputStream din = new DigestInputStream(in, digest)) {
            byte[] buf = new byte[8192];
            while (din.read(buf) >= 0) {
                // drain
            }
        }
        byte[] hash = digest.digest();
        char[] hex = "0123456789abcdef".toCharArray();
        char[] out = new char[hash.length * 2];
        for (int i = 0; i < hash.length; i++) {
            int v = hash[i] & 0xff;
            out[i * 2] = hex[v >>> 4];
            out[i * 2 + 1] = hex[v & 0x0f];
        }
        return new String(out);
    }
}
