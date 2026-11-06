package com.mimari.seguridad;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Guarda y verifica claves con PBKDF2-HMAC-SHA256 (viene con el JDK, sin librerías).
 * Nunca se guarda la clave: solo "pbkdf2$iteraciones$sal$hash", con una sal distinta por usuario.
 */
public final class Claves {

    private static final int ITERACIONES = 310_000;
    private static final int BYTES_SAL = 16;
    private static final int BITS_HASH = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private Claves() { }

    public static String hashear(char[] clave) {
        byte[] sal = new byte[BYTES_SAL];
        ALEATORIO.nextBytes(sal);
        byte[] hash = derivar(clave, sal, ITERACIONES);
        return "pbkdf2$" + ITERACIONES + "$" + Base64.getEncoder().encodeToString(sal)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verificar(char[] clave, String almacenado) {
        try {
            String[] partes = almacenado.split("\\$");
            if (partes.length != 4 || !partes[0].equals("pbkdf2")) {
                return false;
            }
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] calculado = derivar(clave, sal, iteraciones);
            return MessageDigest.isEqual(esperado, calculado);   // comparación en tiempo constante
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Gasta el mismo tiempo que una verificación real (para que no se note si el usuario existe o no). */
    public static void gastarTiempo(char[] clave) {
        derivar(clave, new byte[BYTES_SAL], ITERACIONES);
    }

    private static byte[] derivar(char[] clave, byte[] sal, int iteraciones) {
        PBEKeySpec especificacion = new PBEKeySpec(clave, sal, iteraciones, BITS_HASH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(especificacion).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 no está disponible en este JDK.", e);
        } finally {
            especificacion.clearPassword();
        }
    }
}
