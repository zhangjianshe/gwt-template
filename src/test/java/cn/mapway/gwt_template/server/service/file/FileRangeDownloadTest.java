package cn.mapway.gwt_template.server.service.file;

public class FileRangeDownloadTest {
    public static void main(String[] args) {
        FileRangeDownload.ByteRange a = FileRangeDownload.parseRange("0-99", 1000);
        assert a != null && a.start == 0 && a.end == 99;
        FileRangeDownload.ByteRange b = FileRangeDownload.parseRange("500-", 1000);
        assert b != null && b.start == 500 && b.end == 999;
        FileRangeDownload.ByteRange c = FileRangeDownload.parseRange("-200", 1000);
        assert c != null && c.start == 800 && c.end == 999;
        FileRangeDownload.ByteRange d = FileRangeDownload.parseRange("0-9999", 1000);
        assert d != null && d.start == 0 && d.end == 999;
        assert FileRangeDownload.parseRange("1000-", 1000) == null;
        assert FileRangeDownload.parseRange("abc", 1000) == null;
        assert FileRangeDownload.parseRange("10-5", 1000) == null;
        System.out.println("FileRangeDownload parse ok");
    }
}
