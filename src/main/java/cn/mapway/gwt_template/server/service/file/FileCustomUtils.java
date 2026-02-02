package cn.mapway.gwt_template.server.service.file;

import cn.mapway.biz.core.BizResult;
import lombok.extern.slf4j.Slf4j;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;

/**
 * @Author baoshuaiZealot@163.com  2023/10/30
 */
@Slf4j
public class FileCustomUtils {
    /**
     * 读取文件Magic Number 取
     * @param location
     * @param size
     * @return
     */
    public static byte[] readMagic(String location,int size) {
        byte[] data = new byte[size];
        int readSize = org.nutz.lang.Files.readRange(new File(location), 0, data, 0, size);
        return data;
    }

    public static List<String> splitIgnoreBlank(String s, String regex) {
        if (s == null || regex == null) {
            return Collections.emptyList();
        }
        String[] parts = s.split(regex);
        List<String> list = new LinkedList<>();
        for (String part : parts) {
            if (!Strings.isBlank(part)) {
                list.add(part.trim());
            }
        }
        return list;
    }
    /**
     * 连接路径
     * @param paths
     * @return
     */
    public static String concatPath(String... paths)
    {
        if(paths==null || paths.length==0)
        {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paths.length; i++) {
            String item = paths[i];
            if (item == null|| item.length()==0) {
                continue;
            }
            if (i > 0 && !item.startsWith("/")) {
                sb.append("/");
            }
            sb.append(item);
        }
        return sb.toString();
    }

     public static void setFilePermission(String  file, String permission){
        if(!Lang.isWin()) {
            try {
                // Define permissions (e.g., read/write/execute for owner only: rwx------)
                Set<PosixFilePermission> permissions = PosixFilePermissions.fromString(permission);

                // Create the directory with the specified permissions
                Files.setPosixFilePermissions(Path.of(file), permissions);

                log.info("Directory created successfully with specified permissions.");
            } catch (IOException e) {
                log.error("Failed to create directory: " + e.getMessage());
            }
        }
     }

    /**
     * 检查目录名称是否有效
     *
     * @param name
     * @return
     */
    public static BizResult<Boolean> checkName(String name) {
        if (Strings.isBlank(name)) {
            return BizResult.error(500, "目录名称无效");
        }
        if (name.length() > 64) {
            return BizResult.error(500, "目录名太长了,超过64个字符");
        }
        boolean matches = name.matches("[^\\s\\\\/:\\*\\?\\\"<>\\|](\\x20|[^\\s\\\\/:\\*\\?\\\"<>\\|])*[^\\s\\\\/:\\*\\?\\\"<>\\|\\.]$");
        if (!matches) {
            return BizResult.error(500, "目录名称无效" + name);
        }
        return BizResult.success(true);
    }

    public static File getFirstValidFile(File dir, FileFilter filter, boolean recursive){
        File[] files = dir.listFiles();
        if(files == null || files.length == 0){
            return null;
        }
        for (File file: files){
            if(file.isDirectory() && recursive){
                File targetFile = getFirstValidFile(file, filter, recursive);
                if(targetFile != null){
                    return targetFile;
                }
            } else {
                if(filter.accept(file)){
                    return file;
                }
            }
        }
        return null;
    }

    public static String numberToPath(Long number) {
        if (number == null) {
            throw new IllegalArgumentException("Number cannot be null");
        } else {
            return stringToPath(String.valueOf(number));
        }
    }

    public static String stringToPath(String str) {
        if (str != null && !str.isEmpty()) {
            StringBuilder sb = new StringBuilder();

            for(int i = 0; i < str.length(); i += 2) {
                int end = Math.min(i + 2, str.length());
                if (i > 0) {
                    sb.append("/");
                }

                sb.append(str.substring(i, end));
            }

            return sb.toString();
        } else {
            return "";
        }
    }

    public static String uuidToPath(String uuid) {
        if (uuid != null && uuid.length() >= 2) {
            String cleanUuid = uuid.replace("-", "");
            if (cleanUuid.length() < 2) {
                throw new IllegalArgumentException("UUID after removing hyphens is too short");
            } else {
                String directory = cleanUuid.substring(0, 2);
                String file = cleanUuid.substring(2);
                return directory + "/" + file;
            }
        } else {
            throw new IllegalArgumentException("UUID must be non-null and at least 2 characters long");
        }
    }

    /**
     * 获取文件的创建时间
     * @param file
     * @return
     */
    public static long getFileCreateTimeInMillis(File file) {
        if(!file.exists())
        {
            return new Date().getTime();
        }
        try {
            BasicFileAttributes basicFileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            return basicFileAttributes.creationTime().toMillis();
        } catch (IOException e) {
            return file.lastModified();
        }
    }

    /**
     * 获取文件的创建时间
     * @param file
     * @return
     */
    public static long getFileModifyTimeInMillis(File file) {
        if(!file.exists())
        {
            return new Date().getTime();
        }
        try {
            BasicFileAttributes basicFileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            return basicFileAttributes.lastModifiedTime().toMillis();
        } catch (IOException e) {
            return file.lastModified();
        }
    }

    public static BasicFileAttributes readFileAttributes(File file) {
        if(!file.exists())
        {
            return null;
        }
        try {
            BasicFileAttributes basicFileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            return basicFileAttributes;
        } catch (IOException e) {
            return null;
        }
    }

    public static String getFileName(String filePath) {
        if(filePath == null || filePath.equals("")){
            return "";
        }
        int pos = filePath.lastIndexOf("/");
        if (pos == -1) {
            return filePath;
        }
        return filePath.substring(pos + 1);
    }

    public static String readFirstLine(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return null;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                return reader.readLine();
            }
        } catch (Exception e) {
            log.error("读取文件第一行失败: " + filePath, e);
            return null;
        }
    }

}
