package cn.mapway.gwt_template.server.service.config;

import cn.mapway.biz.core.BizResult;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.util.OpenSSHPrivateKeyUtil;
import org.bouncycastle.crypto.util.OpenSSHPublicKeyUtil;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.bc.BcPEMDecryptorProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

import static org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.pkcs_9_at_extensionRequest;

/**
 * CertService
 * 证书服务类
 * 我们使用一个根证书 对 安装在客户服务器上的系统 签发证书
 * 服务器上的系统 通过导入证书 开放相应的权限
 * <p>
 * 首先 客户通过网页生成一个证书请求 包含以下信息  (客户的私钥保存在客户系统的数据库中)
 * - 客户的k8s集群ID
 * - 客户申请的服务器IP地址
 * - 客户的名称 组织 单位
 * - 客户的公钥
 * <p>
 * 我们为客户的证书请求 颁发包含一下信息的证书
 * <p>
 * - 客户的k8s集群ID
 * - 客户申请的服务器IP地址
 * - 客户的名称 组织 单位
 * - CA的颁发者信息
 * - CA对请求信息的签名
 * - CA对客户系统的 可用子模块信息
 * - 证书的有效使用期限
 * <p>
 * 安装在客户K8S集群中的系统 通过导入我们颁发的证书，进行系统的初始化操作
 * 这个过程要进行证书的验证 验证过程如下
 * -证书是否是一个有效的X.509格式的证书
 * -证书是否是CA颁发的 通过我们嵌入在系统中的CA公钥进行签名认证（这一步很关键,如果客户替换了CA公钥,用它自己签名的私钥进行颁发一个证书导入,也是可以通过的）
 * -证书是否是颁发给客户系统的 (通过保存在数据库中的私钥,进行加密验证)
 * -读取证书中的权限信息初始化系统
 *
 * @author zhang
 */
@Component
@Slf4j
public class CertService implements EnvironmentAware {

    Environment environment;
    private BizResult<X509Certificate> x509Certificate;

    /**
     * Extract sequence with extensions from CSR
     *
     * @param pkcs10Csr The CSR
     * @return Extensions from that CSR (if any)
     */
    public static Extensions getExtensions(PKCS10CertificationRequest pkcs10Csr) {
        Attribute[] attributes = pkcs10Csr.getAttributes(pkcs_9_at_extensionRequest);

        if ((attributes != null) && (attributes.length > 0)) {
            ASN1Encodable[] attributeValues = attributes[0].getAttributeValues();
            if (attributeValues.length > 0) {
                ASN1Sequence asn1Sequence = ASN1Sequence.getInstance(attributeValues[0]);
                Extensions instance = Extensions.getInstance(asn1Sequence);
                return instance;
            }
        }
        return null;
    }


    public static KeyPair genKeyPair(String password) {
        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();
        return pair;
    }


    public static String writePem(String type, byte[] data) {
        StringBuilder sb = new StringBuilder();
        try {
            PemWriter pemWriter = new PemWriter(Lang.opw(sb));
            pemWriter.writeObject(new PemObject(type, data));
            pemWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private static PublicKey parsePublicKey(byte[] data) {
        try {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
            return publicKey;
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PublicKey parsePublicKey(String pem) {
        PemReader reader = new PemReader(Lang.inr(pem));
        PemObject pemObject = null;
        try {
            pemObject = reader.readPemObject();
            X509EncodedKeySpec spec = new X509EncodedKeySpec(pemObject.getContent());
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
            return publicKey;
        } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] readPem(String pem) throws IOException {
        PemReader reader = new PemReader(Lang.inr(pem));
        return reader.readPemObject().getContent();
    }

    public static PrivateKey parsePrivateKey(String pem) {
        PemReader reader = new PemReader(Lang.inr(pem));
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(reader.readPemObject().getContent());
            RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
            return rsaPrivateKey;
        } catch (InvalidKeySpecException | IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static PrivateKey parsePrivateKeyWithPassword(String pem, String password) throws Exception {

        PEMEncryptedKeyPair pemEncryptedKeyPair;
        PEMParser pemParser = new PEMParser(Lang.inr(pem));
        // we know it is this type
        Object readObject = pemParser.readObject();
        if (readObject instanceof PKCS8EncryptedPrivateKeyInfo) {
            PKCS8EncryptedPrivateKeyInfo privateKeyInfo = (PKCS8EncryptedPrivateKeyInfo) readObject;
            InputDecryptorProvider pkcs8Prov =
                    new JceOpenSSLPKCS8DecryptorProviderBuilder().build(password.toCharArray());
            PrivateKeyInfo privateKeyInfo1 = privateKeyInfo.decryptPrivateKeyInfo(pkcs8Prov);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            return converter.getPrivateKey(privateKeyInfo1);
        } else if (readObject instanceof PEMEncryptedKeyPair) {
            pemEncryptedKeyPair = (PEMEncryptedKeyPair) readObject;
            PEMKeyPair pemKeyPair = pemEncryptedKeyPair.decryptKeyPair(new BcPEMDecryptorProvider(password.toCharArray()));
            PrivateKeyInfo privateKeyInfo = pemKeyPair.getPrivateKeyInfo();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            return converter.getPrivateKey(privateKeyInfo);
        } else {
            throw new Exception("不能识别的机密方式");
        }
    }


    public static String toOpenSSHPrivateKey(String pemPrivateKey) throws IOException {
        PrivateKey privateKey = parsePrivateKey(pemPrivateKey);
        // Get the encoded form of the public key
        byte[] keyBytes = privateKey.getEncoded();

        AsymmetricKeyParameter parameter = OpenSSHPrivateKeyUtil.parsePrivateKeyBlob(keyBytes);
        return new String(OpenSSHPrivateKeyUtil.encodePrivateKey(parameter));
    }

    public static String toOpenSSHKey(String pemPublic) {
        PublicKey publicKey = parsePublicKey(pemPublic);
        // Get the encoded form of the public key
        byte[] keyBytes = publicKey.getEncoded();

        // Create a ByteBuffer to contain the OpenSSH format
        ByteBuffer buffer = ByteBuffer.allocate(4 + keyBytes.length);
        buffer.putInt(keyBytes.length);
        buffer.put(keyBytes); // actual key bytes

        // Create OpenSSH format
        String algorithm = "ssh-rsa"; // Change this if your key is a different algorithm
        byte[] algorithmBytes = algorithm.getBytes(StandardCharsets.UTF_8);

        // Final OpenSSH key representation
        ByteBuffer openSSHBuffer = ByteBuffer.allocate(4 + algorithmBytes.length + buffer.array().length);
        openSSHBuffer.putInt(algorithmBytes.length);
        openSSHBuffer.put(algorithmBytes);
        openSSHBuffer.put(buffer.array());

        // Base64 encode the OpenSSH format
        return "ssh-rsa " + Base64.getEncoder().encodeToString(openSSHBuffer.array()) + " imagebot@" + Times.format("yyyyMMddHHmmss", new Date());
    }

    public static String[] genOpenSshKey(String identify) {
        Ed25519KeyPairGenerator keyGen = new Ed25519KeyPairGenerator();
        keyGen.init(new Ed25519KeyGenerationParameters(new SecureRandom()));
        AsymmetricCipherKeyPair keyPair = keyGen.generateKeyPair();

        Ed25519PrivateKeyParameters privKey = (Ed25519PrivateKeyParameters) keyPair.getPrivate();
        Ed25519PublicKeyParameters pubKey = (Ed25519PublicKeyParameters) keyPair.getPublic();

        try {
            if (identify == null || identify.isEmpty()) {
                identify = "ib@" + Times.format("yyyyMMddHHmmss", new Date());
            }
            byte[] bytes = OpenSSHPublicKeyUtil.encodePublicKey(pubKey);
            String pubBase64 = Base64.getEncoder().encodeToString(bytes);
            String publicKey = "ssh-ed25519 " + pubBase64 + " " + identify;


            // Private key in OpenSSH (binary) format
            byte[] privBytes = OpenSSHPrivateKeyUtil.encodePrivateKey(privKey);

            // Wrap in PEM for human readability (optional)
            StringWriter stringWriter = new StringWriter();
            try (PemWriter pemWriter = new PemWriter(stringWriter)) {
                pemWriter.writeObject(new PemObject("OPENSSH PRIVATE KEY", privBytes));
            }

            String privatePem = stringWriter.toString();
            return new String[]{privatePem, publicKey};
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * 解析证书
     *
     * @param base64Cert
     * @return
     */
    public BizResult<X509Certificate> parse509Cert(String base64Cert) {
        if (Strings.isBlank(base64Cert)) {
            return BizResult.error(500, "证书内容为空");
        }
        try {
            base64Cert = Strings.trim(base64Cert);
            CertificateFactory fact = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) fact.generateCertificate(Lang.ins(base64Cert));
            return BizResult.success(certificate);
        } catch (CertificateException e) {
            return BizResult.error(500, e.getMessage());
        }
    }


    /**
     * 加密存储 Private Key
     *
     * @param keyPair
     * @param password
     * @return
     */
    public BizResult<String> toEncryptKeyPair(KeyPair keyPair, String password) {
        try {
            // We must use a PasswordBasedEncryption algorithm in order to encrypt the private key, you may use any common algorithm supported by openssl, you can check them in the openssl documentation http://www.openssl.org/docs/apps/pkcs8.html
            String MYPBEALG = "PBEWithSHA1AndDESede";
            int count = 20;// hash iteration count
            SecureRandom random = SecureRandom.getInstanceStrong();
            byte[] salt = new byte[8];
            random.nextBytes(salt);

            // Create PBE parameter set
            PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, count);
            PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
            SecretKeyFactory keyFac = SecretKeyFactory.getInstance(MYPBEALG);
            SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

            Cipher pbeCipher = Cipher.getInstance(MYPBEALG);

            // Initialize PBE Cipher with key and parameters
            pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);

            // Encrypt the encoded Private Key with the PBE key
            byte[] ciphertext = pbeCipher.doFinal(keyPair.getPrivate().getEncoded());

            // Now construct  PKCS #8 EncryptedPrivateKeyInfo object
            AlgorithmParameters algparms = AlgorithmParameters.getInstance(MYPBEALG);
            algparms.init(pbeParamSpec);
            EncryptedPrivateKeyInfo encinfo = new EncryptedPrivateKeyInfo(algparms, ciphertext);

            // and here we have it! a DER encoded PKCS#8 encrypted key!
            String pemPrivate = writePem("ENCRYPTED PRIVATE KEY", encinfo.getEncoded());
            return BizResult.success(pemPrivate);
        } catch (Exception e) {
            return BizResult.error(500, e.getMessage());
        }
    }

    /**
     * 判断证书是否是由 rootCert颁发
     *
     * @param cert
     * @param rootCert
     * @return
     */
    public BizResult<Boolean> isIssuedBy(X509Certificate cert, X509Certificate rootCert) {
        try {
            cert.verify(rootCert.getPublicKey());
            return BizResult.success(true);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            return BizResult.error(500, e.getMessage());
        } catch (CertificateException | NoSuchProviderException e) {
            e.printStackTrace();
            return BizResult.error(500, e.getMessage());
        }
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
