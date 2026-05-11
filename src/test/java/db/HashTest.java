package db;

import org.nutz.lang.Lang;

public class HashTest {
    public static void main(String[] args) {
        System.out.println(Lang.md5("Hello World"));
        System.out.println(Lang.sha1("Hello World"));
        System.out.println(Lang.sha256("Hello World"));
        // b10a8db164e0754105b7a99be72e3fe54e474f20dca9421f8bf8a546a707b1fe
        // a591a6d40bf420404a011733cfb7b19@0d62c65bf0bcda32b57b277d9ad9f146e
    }
}
