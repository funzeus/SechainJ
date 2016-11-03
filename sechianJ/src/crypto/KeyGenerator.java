package crypto;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.EllipticCurve;
import java.security.SecureRandom;
import java.security.Security;

import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;



//Generate EC key pair 
public class KeyGenerator {
	
	
	public static KeyPair getKeyGen(){
		
		Security.addProvider(new BouncyCastleProvider());
		
		EllipticCurve curve = new EllipticCurve(
				new ECFieldFp(new BigInteger("883423532389192164791648750360308885314476597252960362792450860609699839")),
				new BigInteger("7fffffffffffffffffffffff7fffffffffff8000000000007ffffffffffc", 16),
				new BigInteger("6b016c3bdcf18941d0d654921475ca71a9db2fb27d1d37796185c2942c0a", 16));
		
		ECParameterSpec ecSpec = new ECParameterSpec(
				curve,
				ECPointUtil.decodePoint(curve, Hex.decode("020ffa963cdca8816ccc33b8642bedf905c3d358573d3f27fbbd3b3cb9aaaf")),
				new BigInteger("883423532389192164791648750360308884807550341691627752275345424702807307"),
				1);
		
		try {
			KeyPairGenerator g = KeyPairGenerator.getInstance("ECDSA", "BC");
			g.initialize(ecSpec, new SecureRandom());
			KeyPair keyPair = g.generateKeyPair();
			
			System.out.println(keyPair.getPrivate());
			System.out.println(keyPair.getPublic());
			return keyPair;
		} catch (Exception e){
			throw new AssertionError(e);
		}
	}
	
	public static byte[] decode (String hex) {
        String[] list=hex.split("(?<=\\G.{2})");
        ByteBuffer buffer= ByteBuffer.allocate(list.length);
        
        for(String str: list){
            buffer.put(Byte.parseByte(str, hex.length()));
        }
        System.out.println(buffer.array());
        return buffer.array();

	}
}
