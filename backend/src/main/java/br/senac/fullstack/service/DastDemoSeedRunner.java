package br.senac.fullstack.service;

import br.senac.fullstack.dto.AceiteTermoRequestDTO;
import br.senac.fullstack.dto.AmbienteValidacaoRequestDTO;
import br.senac.fullstack.dto.DesafioValidacaoRequestDTO;
import br.senac.fullstack.dto.StartupRequestDTO;
import br.senac.fullstack.model.Estagio;
import br.senac.fullstack.model.ModalidadeTeste;
import br.senac.fullstack.model.PorteOrganizacao;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Profile("dast")
public class DastDemoSeedRunner implements ApplicationRunner {

    private static final String SEED_ORIGIN = "localhost";
    private static final String STARTUP_EMAIL = "contato@techpulse.com.br";
    private static final String AMBIENTE_EMAIL = "inovacao@novaera.com.br";
    private static final String SETOR_VAREJO = "Varejo";
    private static final String CIDADE_RECIFE = "Recife";
    private static final String ESTADO_PE = "PE";

    private final AceiteTermoService aceiteTermoService;
    private final StartupService startupService;
    private final AmbienteValidacaoService ambienteValidacaoService;
    private final DesafioValidacaoService desafioValidacaoService;
    private final ObjectProvider<UserDetailsManager> userDetailsManagerProvider;
    private final ObjectProvider<PasswordEncoder> passwordEncoderProvider;
    private final String demoAccessCode;

    @Autowired
    public DastDemoSeedRunner(
            AceiteTermoService aceiteTermoService,
            StartupService startupService,
            AmbienteValidacaoService ambienteValidacaoService,
            DesafioValidacaoService desafioValidacaoService,
            ObjectProvider<UserDetailsManager> userDetailsManagerProvider,
            ObjectProvider<PasswordEncoder> passwordEncoderProvider,
            @Value("${app.dast.demo-credential:123456}") String demoAccessCode) {
        this.aceiteTermoService = aceiteTermoService;
        this.startupService = startupService;
        this.ambienteValidacaoService = ambienteValidacaoService;
        this.desafioValidacaoService = desafioValidacaoService;
        this.userDetailsManagerProvider = userDetailsManagerProvider;
        this.passwordEncoderProvider = passwordEncoderProvider;
        this.demoAccessCode = demoAccessCode;
    }

    @Override
    public void run(ApplicationArguments args) {
        var aceite = aceiteTermoService.registrar(
                new AceiteTermoRequestDTO("LGPD-2026.1", SEED_ORIGIN, true),
                null,
                SEED_ORIGIN);

        startupService.criar(new StartupRequestDTO(
                "TechPulse Soluções",
                STARTUP_EMAIL,
                demoAccessCode,
                "Plataforma de recuperação inteligente de carrinhos e engajamento em checkout web.",
                SETOR_VAREJO,
                LocalDate.of(2024, 3, 15),
                Estagio.MVP_FUNCIONAL,
                List.of("Ana Silva", "Lucas Oliveira"),
                "Recife / PE",
                "Brasil",
                true,
                "Porto Digital",
                aceite.id(),
                ESTADO_PE,
                CIDADE_RECIFE,
                "Validar redução da taxa de abandono de carrinho em ambiente real de varejo.",
                List.of(),
                List.of(ModalidadeTeste.REMOTO, ModalidadeTeste.HIBRIDO)));

        var ambiente = ambienteValidacaoService.criar(new AmbienteValidacaoRequestDTO(
                "Supermercados Nova Era Ltda",
                "Supermercados Nova Era",
                SETOR_VAREJO,
                PorteOrganizacao.MEDIO,
                ESTADO_PE,
                CIDADE_RECIFE,
                AMBIENTE_EMAIL,
                "(81) 99999-0000",
                "https://www.linkedin.com/company/novaera-varejo",
                demoAccessCode,
                aceite.id()));

        desafioValidacaoService.criar(new DesafioValidacaoRequestDTO(
                ambiente.id(),
                "Alta taxa de abandono de carrinho no checkout web",
                "Buscamos solução para reduzir o abandono de carrinho em 500 sessões piloto no e-commerce.",
                "Clientes do e-commerce regional",
                500,
                ModalidadeTeste.REMOTO,
                30));

        desafioValidacaoService.criar(new DesafioValidacaoRequestDTO(
                ambiente.id(),
                "Ruptura de gôndola nos horários de pico",
                "Teste em 2 lojas piloto com acesso a dados anonimizados de PDV para reposição preventiva.",
                "Operação de loja física",
                200,
                ModalidadeTeste.HIBRIDO,
                30));

        registrarUsuario(STARTUP_EMAIL, demoAccessCode, "STARTUP");
        registrarUsuario(AMBIENTE_EMAIL, demoAccessCode, "AMBIENTE_VALIDACAO");
    }

    private void registrarUsuario(String email, String credencial, String role) {
        UserDetailsManager manager = userDetailsManagerProvider.getIfAvailable();
        PasswordEncoder encoder = passwordEncoderProvider.getIfAvailable();
        if (manager != null && encoder != null && !manager.userExists(email)) {
            manager.createUser(User.withUsername(email)
                    .password(encoder.encode(credencial))
                    .roles(role)
                    .build());
        }
    }
}
