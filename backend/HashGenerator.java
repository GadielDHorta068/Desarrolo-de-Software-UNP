import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "password123";
        String hash = encoder.encode(password);
        System.out.println("Hash for password123: " + hash);
        
        // Verificar que el hash funciona
        boolean matches = encoder.matches(password, hash);
        System.out.println("Hash verification: " + matches);
    }
}