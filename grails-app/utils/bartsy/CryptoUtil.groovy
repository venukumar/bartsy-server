package bartsy

import java.security.PrivateKey;
import java.io.*;
import java.security.*;
import java.security.spec.*;
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import javax.xml.bind.DatatypeConverter;


class CryptoUtil {
	
    static Process p;

    public static void createRSAKeys(privKeyFile,publicKeyFile){
        //to generate private key
        p=Runtime.getRuntime().exec("openssl genpkey -algorithm RSA -out "+privKeyFile);
        p.waitFor();
        //to generate public key
        p=Runtime.getRuntime().exec("openssl rsa -in "+privKeyFile+" -pubout -out "+publicKeyFile);
        p.waitFor();
        return
    }
	 
    public static void createCAKeys(caKey,caCert){
        //Create/Retain Keys and Self-signed certificate for CA
        String[] createCA=["openssl","req","-new","-x509","-newkey","rsa:1024","-keyout",caKey,"-nodes","-sha1","-out",caCert,"-days","365","-subj","/C=US/ST=Florida/L=West Palm Beach/O=Bartsy Inc./OU=Support/CN=CA.Bartsy.com/emailAddress=Support@bartsy.com"]
        p=Runtime.getRuntime().exec(createCA);
        p.waitFor();
        return
    }
	 
    public static void createUserSignedCert(String CSRFileName,String certFileName, String caCert, String caPrivateKey){
        //Sign the user CSR, and return cert
        String[] createCert=["openssl","x509","-req","-days","365","-in",CSRFileName,"-CA",caCert,"-CAkey",caPrivateKey,"-set_serial","01","-out",certFileName]
        p=Runtime.getRuntime().exec(createCert);
        p.waitFor();
        return
    }
    public static void generatePublicKeyFromCert(String certFileName,String publicKeyFileName){
        def cryptoPath = "web-app/"
        //GEN PUBLIC KEY
        p=Runtime.getRuntime().exec("openssl x509 -in "+cryptoPath+certFileName+" -pubkey -noout");
        p.waitFor();	
        InputStream ins = p.getInputStream();
        int c;
        StringBuffer sb=new StringBuffer();
        while ((c = ins.read()) != -1) {
            System.out.print((char)c);
            sb.append((char)c);
        }
        ins.close();
		
        String str = sb.toString();
        if (str !=null){
            byte[] buf = str.getBytes();
            OutputStream os = new FileOutputStream(cryptoPath+""+publicKeyFileName);
            os.write(buf);
            os.close();
        }
        return
    }
	 

    public static void encrypt(String pubkey,String data){
        def cryptoPath = "web-app/"
        p=Runtime.getRuntime().exec("openssl rsautl -encrypt -inkey "+cryptoPath+"userCryptoDir/"+pubkey+" -pubin -in "+cryptoPath+"userCryptoDir/"+data+" -out "+cryptoPath+"userCryptoDir/encfile.txt");
        p.waitFor();
        return
    }
		 
    public static void decrypt(String privateKeyFileName,String encryptedFileName,String decryptedFileName){
        def cryptoPath = "web-app/"
        p=Runtime.getRuntime().exec("openssl rsautl -decrypt -inkey "+cryptoPath+"userCryptoDir/"+privateKeyFileName+" -in "+cryptoPath+"userCryptoDir/"+encryptedFileName +"-out "+cryptoPath+"userCryptoDir/"+decryptedFileName);
        p.waitFor();
        return
    }
    public static void createCSR(String privateKeyFileName,String userCSRFileName,subj){
        //To Generate CSR 
        String[] CSR=["openssl","req","-out",userCSRFileName,"-key",privateKeyFileName, "-new", "-subj",""+subj]
        p=Runtime.getRuntime().exec(CSR);
        p.waitFor();
        return
    }
		
    public static String createAESKey(String secretPwd){
        //VERIFY PUBLIC KEY
        println "inside"
        p=Runtime.getRuntime().exec( "openssl enc -aes-128-cbc -k "+secretPwd +" -P -md sha1");
        p.waitFor();
        InputStream ins = p.getInputStream();
        int c;
        StringBuffer sb=new StringBuffer();
        while ((c = ins.read()) != -1) {
            System.out.print((char)c);
            sb.append((char)c);
        }
        ins.close();
		   
        String str = sb.toString();
        return str;
    }
		
    public static String rsaEncrypt(String PublicKeyFileName,String KeyFileName){
        //Encrypt the AES key
        def cryptoPath = "web-app/"
        p=Runtime.getRuntime().exec("openssl rsautl -encrypt -inkey "+cryptoPath+PublicKeyFileName+" -pubin -in "+cryptoPath+"userCryptoDir/"+KeyFileName+" -out "+cryptoPath+"userCryptoDir/encrypt.txt");
        p.waitFor();
	
        return
    }
		
    public static String rsaDecrypt(String privateKeyFileName,String encryptedKeyFileName){
        def cryptoPath = "web-app/"
        //Decrypt the AES key
        //openssl rsautl -decrypt -inkey sec_privateKey.pem -in file.txt -out decrypted.txt
        p=Runtime.getRuntime().exec("openssl rsautl -decrypt -inkey "+cryptoPath+privateKeyFileName+" -in "+cryptoPath+encryptedKeyFileName);
        p.waitFor();
        InputStream ins = p.getInputStream();
        int c;
        StringBuffer sb=new StringBuffer();
        while ((c = ins.read()) != -1) {
            //  System.out.print((char)c);
            sb.append((char)c);
        }
        ins.close();
        String decrypted = sb.toString();
        println "decrypted key******:"+decrypted
			
        return decrypted;
        return
		
    }
		
    public static String aesEncrypt(String message,String key,String iv){
        def cryptoPath = "web-app/"
        //Encrypt with AES key
        p=Runtime.getRuntime().exec( "openssl enc -aes-128-cbc -in "+cryptoPath+"userCryptoDir/"+message+" -K "+key+" -iv "+iv);
        p.waitFor();
        InputStream ins = p.getInputStream();
        int c;
        StringBuffer sb=new StringBuffer();
        while ((c = ins.read()) != -1) {
            //  System.out.print((char)c);
            sb.append((char)c);
        }
        ins.close();
        String encrypted = sb.toString();
       // println "encrypted key******:"+encrypted
        return encrypted;
    }

    public static void aesDecrypt(String decryptedFilename,String key,String iv,String encryptedFileName){
        def cryptoPath = "web-app/"
        //Decrypt the AES key
        p=Runtime.getRuntime().exec("openssl enc -aes-128-cbc -d -in "+cryptoPath+"userCryptoDir/"+encryptedFileName+" -K "+key+" -iv "+iv+" -out "+cryptoPath+"userCryptoDir/"+decryptedFilename);
        p.waitFor();
        return
    }
    public static byte[] HexToByte(String hex) {
        int len = hex.length();
        if (len % 2 == 1) {
            hex = "0" + hex;
            len++;
        }
        byte[] value = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            value[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character
                .digit(hex.charAt(i + 1), 16));
	
        }
        //println "hex2byte::"+value
        return value;
    }
		
    public static byte[] hexStringToByteArray(String s) {
      /*  int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                + Character.digit(s.charAt(i+1), 16));
        }
        return data;*/
		return DatatypeConverter.parseHexBinary(s);
		
    }
		
    public static RSAPublicKey readPublicKey(String certFile) {
		
        InputStream inStream;
        RSAPublicKey pubkey = null;
        try {
            inStream = new FileInputStream(certFile);
		   
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert =(X509Certificate)cf.generateCertificate(inStream);
            inStream.close();
		
            // Read the public key from certificate file
            pubkey = (RSAPublicKey) cert.getPublicKey();
            System.out.println( "key:"+pubkey);
		  
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CertificateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return pubkey;
    }
    public static String convertStringToHex(String str) {
			
        char[] chars = str.toCharArray();
			
        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            hex.append(Integer.toHexString((int) chars[i]));
        }
			
        return hex.toString();
    }
			
				
    private static String getHexString(byte[] b) {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }
		
				
    public static String signverificationinput(String signedFile,String originalFile,String publicKey){
        def cryptoPath = "web-app/"
        String signedtmp = signedFile.originalFilename.toString()
        String usermesgtmp = originalFile.originalFilename.toString()
					
        signedFile.transferTo( new File(cryptoPath+"userCryptoDir/", signedtmp))
        originalFile.transferTo( new File( cryptoPath+"userCryptoDir/", usermesgtmp))
						
        //def certInstance = UserCerts.findByUser(userInstance)
        //certInstance.setUser(userInstance)
				
        String publickeyFilename=publicKey
        //userInstance.setUserPublicKeyFilePath(publickeyFilename)
        // creates user certificate
        String verification=CryptoUtil.verifySignature(usermesgtmp, signedtmp,publickeyFilename)
    }
				
				
				
				
    private static PublicKey readPublicKey1(String input,
        String algorithm) throws Exception {
        FileInputStream pubKeyStream = new FileInputStream(input);
        int pubKeyLength = pubKeyStream.available();
        byte[] pubKeyBytes = new byte[pubKeyLength];
        pubKeyStream.read(pubKeyBytes);
        pubKeyStream.close();
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
        //System.out.println();
        //System.out.println("Public Key Info: ");
        //System.out.println("Algorithm = "+pubKey.getAlgorithm());
        //System.out.println("Saved File = "+input);
        //System.out.println("Length = "+pubKeyBytes.length);
        //System.out.println("toString = "+pubKey.toString());
        return pubKey;
    }
    private static byte[] readSignature(String input)
    throws Exception {
        FileInputStream signStream = new FileInputStream(input);
        int signLength = signStream.available();
        byte[] signBytes = new byte[signLength];
        //println signLength
        signStream.read(signBytes);
        signStream.close();
        return  signBytes;
    }
    private static boolean verify(String input, String algorithm,
        byte[] sign, PublicKey pubKey) throws Exception {
        Signature sg = Signature.getInstance(algorithm);
        sg.initVerify(pubKey);
       // System.out.println();
       // System.out.println("Signature Object Info: ");
        //System.out.println("Algorithm = "+sg.getAlgorithm());
        //System.out.println("Provider = "+sg.getProvider());
        FileInputStream in1 = new FileInputStream(input);
        int bufSize = 1024;
        byte[] buffer = new byte[bufSize];
        int n = in1.read(buffer,0,bufSize);
        int count = 0;
        while (n!=-1) {
            count += n;
            sg.update(buffer,0,n);
            n = in1.read(buffer,0,bufSize);
        }
        in1.close();
        boolean ok = sg.verify(sign);
        //System.out.println("Verify Processing Info: ");
       // System.out.println("Number of input bytes = "+count);
       // System.out.println("Verification result = "+ok);
        return ok;
    }
				
}
