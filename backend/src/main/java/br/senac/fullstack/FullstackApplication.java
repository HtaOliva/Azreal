package br.senac.fullstack;

import br.senac.fullstack.repository.UsuarioRepository;
import javax.sql.DataSource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import br.senac.fullstack.config.SecurityConfig;


@SpringBootApplication
@Import(SecurityConfig.class)
public class FullstackApplication {

    public static void main(String[] args) {
        SpringApplication.run(FullstackApplication.class, args);
    }
    // Esse método roda automaticamente logo após a aplicação iniciar
    @Bean
    @ConditionalOnBean(DataSource.class)
    public CommandLineRunner testarConexao(UsuarioRepository usuarioRepository) {
        return args -> {
            try {
                long total = usuarioRepository.count();
                System.out.println("==================================================");
                System.out.println(" CONEXÃO COM O SUPABASE BEM-SUCEDIDA! 🎉");
                System.out.println(" Total de usuários cadastrados no banco: " + total);
                System.out.println("==================================================");
            } catch (Exception e) {
                System.err.println("==================================================");
                System.err.println(" ERRO AO CONECTAR COM O SUPABASE: ❌");
                System.err.println(e.getMessage());
                System.err.println("==================================================");
            }
        };
    }
}
