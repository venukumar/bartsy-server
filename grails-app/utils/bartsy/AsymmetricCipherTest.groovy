package bartsy
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec

import javax.crypto.Cipher
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;


class AsymmetricCipherTest {
	static String xform = "RSA";
	private static String encrypt(byte[] inpBytes, PublicKey key) {
		byte[] b
		String encoded
		try{
			Cipher cipher = Cipher.getInstance(xform);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			b= cipher.doFinal(inpBytes);
			Base64 b64 = new Base64();
			 encoded=b64.encode(b);
		}
		catch(Exception e){
			println "Exception in rsa encrypt::::"+e.getMessage()
		}
		return encoded
	}
	private static byte[] decrypt(byte[] inpBytes, PrivateKey key) {
		byte[] b
		try{
		Cipher cipher = Cipher.getInstance(xform);
		cipher.init(Cipher.DECRYPT_MODE, key);
		 b=cipher.doFinal(inpBytes);
		}
		catch(Exception e){
			println "Exception in rsa decrypt::::"+e.getMessage()
			e.printStackTrace();
		}
		return b
	}

	public static KeyPair keygen(){
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(1024); // 512 is the keysize.
		KeyPair kp = kpg.generateKeyPair();
		PublicKey pubk = kp.getPublic();
		PrivateKey prvk = kp.getPrivate();
		return kp;
	}
	private static String getHexString(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}
	public  static PublicKey getPemPublicKey(String filename, String algorithm) {
		KeyFactory kf
		X509EncodedKeySpec spec 
		PublicKey pukey
		try{
		File f = new File(filename);
		FileInputStream fis = new FileInputStream(f);
		DataInputStream dis = new DataInputStream(fis);
		byte[] keyBytes = new byte[(int) f.length()];
		dis.readFully(keyBytes);
		dis.close();

		String temp = new String(keyBytes);
		String privKeyPEM = temp.replace("-----BEGIN PUBLIC KEY-----\n", "");
		privKeyPEM = privKeyPEM.replace("-----END PUBLIC KEY-----", "");
		Base64 b64 = new Base64();
		byte [] decoded = b64.decode(privKeyPEM);
		 spec =
			  new X509EncodedKeySpec(privKeyPEM.getBytes());
		 kf = KeyFactory.getInstance(algorithm);
		 pukey=kf.generatePublic(spec)
		}catch (Exception e) {
			println "xception in public:"+e.getMessage()
		}
		return kf.generatePublic(spec);
		}
	public static PrivateKey getPemPrivateKey(String filename, String algorithm) {
		KeyFactory kf
		PKCS8EncodedKeySpec spec
		try{
		File f = new File(filename);
		FileInputStream fis = new FileInputStream(f);
		DataInputStream dis = new DataInputStream(fis);
		byte[] keyBytes = new byte[(int) f.length()];
		dis.readFully(keyBytes);
		dis.close();

		String temp = new String(keyBytes);
		String privKeyPEM = temp.replace("-----BEGIN PRIVATE KEY-----\n", "");
		privKeyPEM = privKeyPEM.replace("-----END PRIVATE KEY-----", "");

		Base64 b64 = new Base64();
		byte [] decoded = b64.decode(privKeyPEM);

		 spec = new PKCS8EncodedKeySpec(decoded);
		 kf = KeyFactory.getInstance(algorithm);
	}catch (Exception e) {
	println "xceptionin priv:"+e.getMessage()
	e.printStackTrace();
	}
		return kf.generatePrivate(spec);
	}

}

