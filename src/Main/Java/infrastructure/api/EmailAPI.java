package infrastructure.api;

import infrastructure.email.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/correos")
public class EmailAPI {
    private final EmailService emailService;

    public EmailAPI(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping
    public ResponseEntity<Void> enviarCorreo(@RequestBody EnviarCorreoRequest request) {
        emailService.enviarCorreo(request.destinatario(), request.asunto(), request.mensaje());
        return ResponseEntity.accepted().build();
    }

    public record EnviarCorreoRequest(String destinatario, String asunto, String mensaje) {}
}
