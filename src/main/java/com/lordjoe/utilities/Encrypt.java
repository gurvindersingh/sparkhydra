package com.lordjoe.utilities;

/**
 * Simple shell for blowfish encrypts up to 40 char strings into a 60 char encrypted string and back
 *   com.lordjoe.utilities.Encrypt
 * @author Steve LewisS
 */
public class Encrypt {
    public static final int CYPHER_LENGTH = 40;
    public static final String BASE_KEY = "LordJoe2000.";
    public static final String DEFAULT_KEY_ENCRYPTED = "HwpsFRwPmdaFM53lsbwBGZlhvB7QnN8vEiOcN2ShnTFQNSD1EDbyK9dd5T93j92CKvY+kfAuffH94K+dzvmVN/GqwjYLXadtswIx5Qr+7/9UA7PLlSTBAsGdKimcWI/GwZ0qKZxYj8bBnSopnFiPxsGdKimcWI/GwZ0qKZxYj8bBnSopnFiPxsGdKimcWI/GwZ0qKZxYj8bBnSopnFiPxsGdKimcWI/GwZ0qKZxYj8bBnSopnFiPxsGdKimcWI/GwZ0qKZxYj8bBnSopnFiPxsGdKimcWI/GwZ0qKZxYj8bBnSopnFiPxg==";
    public static  final String DEFAULT_KEY; //  = "What hath God wrought.In union thers is strength. In money glory and in neigher peace.";
    public static final Encrypt INTERNAL_ENCRYPTOR = new Encrypt(BASE_KEY);

    // Should always work SLewis
    static {
        INTERNAL_ENCRYPTOR.setCypherLength(2 * DEFAULT_KEY_ENCRYPTED.length());
        //  DEFAULT_KEY_ENCRYPTED = INTERNAL_ENCRYPTOR.encrypt(DEFAULT_KEY);
        // System.out.println("DEFAULT_KEY encrypts as " + DEFAULT_KEY_ENCRYPTED) ;
        DEFAULT_KEY = INTERNAL_ENCRYPTOR.decrypt(DEFAULT_KEY_ENCRYPTED).trim();
    }

    /**
     * encrypt a string with the default key
     *
     * @param in non-null string to encrypt
     * @return non-null encrypted string
     */
    public static String encryptString(String in) {
        return encryptString(in, null);
    }

    /**
     * encrypt a string with the default key
     *
     * @param in  non-null string to encrypt
     * @param key possibly null key null says use default
     * @return non-null encrypted string
     */
    public static String encryptString(String in, String key) {
        if (key == null)
            key = DEFAULT_KEY;
        Encrypt enc = new Encrypt(key);
        return enc.encrypt(in);
    }

    /**
     * decrypt a string with the default key
     *
     * @param in non-null string to decrypt
     * @return non-null decrypted string
     */
    public static String decryptString(String in) {
        return decryptString(in, null);
    }

    /**
     * decrypt a string with the default key
     *
     * @param in  non-null string to decrypt
     * @param key possibly null key null says use default
     * @return non-null decrypted string
     */
    public static String decryptString(String in, String key) {
        if (key == null)
            key = DEFAULT_KEY;
        Encrypt enc = new Encrypt(key);
        return enc.decrypt(in);
    }

    /**
     * decrypt a string with the default key
     *
     * @param in  non-null string to decrypt
     * @param key possibly null key null says use default
     * @return non-null decrypted string
     */
    public static byte[] decryptBytes(byte[] in, String key) {
        if (key == null)
            key = DEFAULT_KEY;
        Encrypt enc = new Encrypt(key);
        return enc.decryptBytes(in);
    }

    /**
     * decrypt a string with the default key
     *
     * @param in  non-null string to decrypt
     * @param key possibly null key null says use default
     * @return non-null decrypted string
     */
    public static byte[] encryptBytes(byte[] in, String key) {
        if (key == null)
            key = DEFAULT_KEY;
        Encrypt enc = new Encrypt(key);
        return enc.encryptBytes(in);
    }

    private String m_Key;
    private int m_CypherLength = CYPHER_LENGTH;

    public Encrypt() {
        this(DEFAULT_KEY);
    }

    public Encrypt(String key) {
        m_Key = key;
    }

    public int getCypherLength() {
        return m_CypherLength;
    }

    public void setCypherLength(int cypherLength) {
        m_CypherLength = cypherLength;
    }

    public String getKey() {
        return m_Key;
    }

    public void setKey(String key) {
        m_Key = key;
    }


    public String encrypt(String in) {
        Blowfish bf = new Blowfish();
        bf.engineInitEncrypt(m_Key);
        int l = in.length();
        byte[] data = in.getBytes();
        int CypherLength = getCypherLength();
        if (data.length > CypherLength)
            throw new IllegalArgumentException("encrypt length must be < " + CypherLength + " was  " + data.length);


        byte[] clear = new byte[CypherLength];
        for (int i = 0; i < clear.length; i++) {
            clear[i] = 32;
        }
        byte[] cypher = new byte[CypherLength];
        System.arraycopy(data, 0, clear, 0, data.length);
        bf.engineUpdate(clear, 0, CypherLength, cypher, 0);

        return (Base64.encode(cypher));

    }

    public String decrypt(String data) {
        final byte[] data1 = Base64.decode(data);
        final String s = decrypt(data1);
        return s;
    }


    public byte[] decryptBytes(byte[] cypher) {
        Blowfish bf = new Blowfish();
        bf.engineInitDecrypt(m_Key);
        byte[] clear = new byte[cypher.length];
        bf.engineUpdate(cypher, 0, cypher.length, clear, 0);
        return clear;
    }


    public byte[] encryptBytes(byte[] cypher) {
        Blowfish bf = new Blowfish();
        bf.engineInitDecrypt(m_Key);
        byte[] clear = new byte[cypher.length];
        bf.engineUpdate(cypher, 0, cypher.length, clear, 0);
        return clear;
    }


    public String decrypt(byte[] data) {
        Blowfish bf = new Blowfish();
        bf.engineInitDecrypt(m_Key);
        //    byte[] data = in.getBytes();
        int CypherLength = getCypherLength();
        if (data.length > CypherLength)
            throw new IllegalArgumentException("decrypt length must be < " + CypherLength);
        byte[] clear = new byte[CypherLength];
        byte[] cypher = new byte[CypherLength];
        System.arraycopy(data, 0, cypher, 0, data.length);
        bf.engineUpdate(cypher, 0, CypherLength, clear, 0);
        return (new String(clear).trim());
    }

    private static void usage() {
        System.out.println(
        "Encrypts any argument strings" );
    }


     public static void main(String[] args) {
        if(args.length == 0)   {
            usage();
            return;
        }
        Encrypt test = new Encrypt();
         for (int i = 0; i < args.length; i++) {
             String arg = args[i];
             System.out.println( encryptString(arg));

         }

     }


//    public static void main(String[] args) {
//        System.out.println("Key encrypts to:" + encryptString("ATTILLOAS KEY"));
//        System.out.println("Secret Key encrypts to:" + encryptString("ATTILLOAS SECRET KEY"));
//    }

}
 