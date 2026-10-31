package infrastructure.api;

import service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailAPI {
    private final EmailService emailService;

    public EmailAPI(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/enviar")
    public ResponseEntity<String> enviarCorreo(@RequestBody EnviarCorreoRequest request) {
        emailService.enviarCorreo(request.destinatario(), request.asunto(), request.cuerpo());
        return ResponseEntity.ok("Correo enviado exitosamente");
    }

    public record EnviarCorreoRequest(
            String destinatario,
            String asunto,
            String cuerpo
    ) {}
}
