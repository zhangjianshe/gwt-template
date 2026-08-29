package cn.mapway.gwt_template.server.service.file;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;

/**
 * Stream a local file with HTTP Range (RFC 7233) so large downloads can resume.
 */
@Slf4j
public final class FileRangeDownload {
    private static final int BUF = 64 * 1024;

    private FileRangeDownload() {
    }

    public static void send(File file, HttpServletRequest req, HttpServletResponse resp,
                            String contentType, String contentDisposition, String cacheControl)
            throws IOException {
        if (file == null || !file.isFile()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getWriter().println("not found");
            return;
        }

        long length = file.length();
        long lastModified = file.lastModified();
        String rangeHeader = req.getHeader("Range");

        long ifModifiedSince = -1;
        try {
            ifModifiedSince = req.getDateHeader("If-Modified-Since");
        } catch (IllegalArgumentException ignored) {
        }
        if (rangeHeader == null
                && ifModifiedSince != -1
                && ifModifiedSince / 1000 == lastModified / 1000) {
            resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        if (contentType == null || contentType.trim().isEmpty()) {
            try {
                contentType = Files.probeContentType(file.toPath());
            } catch (IOException ignored) {
            }
            if (contentType == null || contentType.trim().isEmpty()) {
                contentType = "application/octet-stream";
            }
        }
        resp.setContentType(contentType);
        if (contentDisposition != null && !contentDisposition.trim().isEmpty()) {
            resp.setHeader("Content-Disposition", contentDisposition);
        }
        if (cacheControl != null && !cacheControl.trim().isEmpty()) {
            resp.setHeader("Cache-Control", cacheControl);
        }
        resp.setDateHeader("Last-Modified", lastModified);
        resp.setHeader("Accept-Ranges", "bytes");
        resp.setHeader("X-Frame-Options", "SAMEORIGIN");

        long start = 0;
        long end = Math.max(0, length - 1);
        boolean partial = false;

        if (rangeHeader != null && rangeHeader.startsWith("bytes=") && useRange(req, lastModified)) {
            ByteRange range = parseRange(rangeHeader.substring("bytes=".length()), length);
            if (range == null) {
                resp.setHeader("Content-Range", "bytes */" + length);
                resp.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return;
            }
            start = range.start;
            end = range.end;
            partial = true;
        }

        long contentLength = length == 0 ? 0 : (end - start + 1);
        resp.setHeader("Content-Length", Long.toString(contentLength));
        if (partial) {
            resp.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + length);
            resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        } else {
            resp.setStatus(HttpServletResponse.SC_OK);
        }

        if ("HEAD".equalsIgnoreCase(req.getMethod()) || contentLength == 0) {
            return;
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "r");
             OutputStream out = resp.getOutputStream()) {
            raf.seek(start);
            byte[] buf = new byte[BUF];
            long remaining = contentLength;
            while (remaining > 0) {
                int n = raf.read(buf, 0, (int) Math.min(buf.length, remaining));
                if (n < 0) {
                    break;
                }
                out.write(buf, 0, n);
                remaining -= n;
            }
            out.flush();
        } catch (IOException e) {
            // Client abort / broken pipe is normal for resume and cancelled downloads.
            log.debug("range download interrupted {} : {}", file.getName(), e.getMessage());
        }
    }

    public static boolean useRange(HttpServletRequest req, long lastModified) {
        String ifRange = req.getHeader("If-Range");
        if (ifRange == null || ifRange.trim().isEmpty()) {
            return true;
        }
        if (ifRange.startsWith("\"") || ifRange.startsWith("W/")) {
            return true;
        }
        try {
            long ir = req.getDateHeader("If-Range");
            if (ir == -1) {
                return true;
            }
            return ir / 1000 == lastModified / 1000;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    /**
     * Parse a single RFC 7233 byte range against {@code length}. Returns null if unsatisfiable.
     */
    public static ByteRange parseRange(String spec, long length) {
        if (spec == null || length <= 0) {
            return null;
        }
        String first = spec.split(",")[0].trim();
        if (first.isEmpty()) {
            return null;
        }
        int dash = first.indexOf('-');
        if (dash < 0) {
            return null;
        }
        String left = first.substring(0, dash).trim();
        String right = first.substring(dash + 1).trim();
        long start;
        long end;
        try {
            if (left.isEmpty()) {
                long suffix = Long.parseLong(right);
                if (suffix <= 0) {
                    return null;
                }
                start = Math.max(0, length - suffix);
                end = length - 1;
            } else {
                start = Long.parseLong(left);
                end = right.isEmpty() ? (length - 1) : Long.parseLong(right);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        if (start < 0 || start >= length || end < start) {
            return null;
        }
        if (end >= length) {
            end = length - 1;
        }
        return new ByteRange(start, end);
    }

    public static final class ByteRange {
        public final long start;
        public final long end;

        ByteRange(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }
}
