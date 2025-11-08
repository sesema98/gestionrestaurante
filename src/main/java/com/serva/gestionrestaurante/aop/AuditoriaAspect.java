package com.serva.gestionrestaurante.aop;

import com.serva.gestionrestaurante.entities.Auditoria;
import com.serva.gestionrestaurante.entities.Cliente;
import com.serva.gestionrestaurante.repositories.AuditoriaRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Aspect @Component
public class AuditoriaAspect {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaAspect(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Pointcut("execution(* com.serva.gestionrestaurante.services.*.guardar(..))")
    public void guardarOperacion() {}

    @Pointcut("execution(* com.serva.gestionrestaurante.services.*.eliminar(..))")
    public void eliminarOperacion() {}

    @AfterReturning(pointcut = "guardarOperacion()", returning = "ret")
    public void auditarGuardar(JoinPoint jp, Object ret) {
        Long id = null;
        if (ret instanceof Cliente c) id = c.getId();
        guardarAuditoria("clientes", id, "CREATE/UPDATE");
    }

    @AfterReturning("eliminarOperacion() && args(id)")
    public void auditarEliminar(Long id) { guardarAuditoria("clientes", id, "DELETE"); }

    private void guardarAuditoria(String tabla, Long recursoId, String accion) {
        String usuario = "anonimo";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            usuario = Optional.ofNullable(auth.getName()).orElse("anonimo");
        }
        Auditoria a = Auditoria.builder()
                .tabla(tabla).recursoId(recursoId).accion(accion)
                .fecha(LocalDateTime.now()).usuario(usuario).build();
        auditoriaRepository.save(a);
    }
}
