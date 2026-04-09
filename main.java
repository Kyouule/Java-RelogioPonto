import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.*;

// =====================
// MAIN
// =====================
@SpringBootApplication
public class Main implements CommandLineRunner {

    @Autowired
    private FuncionarioRepository funcionarioRepo;

    @Autowired
    private DepartamentoRepository departamentoRepo;

    @Autowired
    private RegistroPontoRepository registroRepo;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {

        int funcionarioId = 1;
        LocalDate data = LocalDate.parse("2026-03-18");

        // ===== BUSCAR FUNCIONÁRIO =====
        Funcionario f = funcionarioRepo.findById(funcionarioId).orElse(null);

        if (f == null) {
            System.out.println("Funcionário não encontrado");
            return;
        }

        // ===== BUSCAR REGISTROS =====
        List<RegistroPonto> registros =
                registroRepo.findByFuncionarioIdAndDataOrderByHora(funcionarioId, data);

        System.out.println("RELATÓRIO DE PONTO");
        System.out.println("Funcionário: " + f.getNome());
        System.out.println("Departamento: " + f.getDepartamento().getNome());
        System.out.println("----------------------------");

        Duration total = Duration.ZERO;

        for (int i = 0; i < registros.size(); i += 2) {

            LocalTime entrada = registros.get(i).getHora();
            LocalTime saida = registros.get(i + 1).getHora();

            Duration periodo = Duration.between(entrada, saida);
            total = total.plus(periodo);

            System.out.println(entrada + " - " + saida + " = " + formatar(periodo));
        }

        System.out.println("----------------------------");
        System.out.println("Total: " + formatar(total));
    }

    private String formatar(Duration d) {
        long horas = d.toHours();
        long minutos = d.toMinutes() % 60;
        return String.format("%02d:%02d", horas, minutos);
    }
}

// =====================
// ENTIDADES
// =====================

@Entity
@Table(name = "Funcionario")
class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;

    @ManyToOne
    @JoinColumn(name = "departamento_id")
    private Departamento departamento;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Departamento getDepartamento() { return departamento; }
    public void setDepartamento(Departamento departamento) { this.departamento = departamento; }
}

@Entity
@Table(name = "Departamento")
class Departamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}

@Entity
@Table(name = "RegistroPonto")
class RegistroPonto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "funcionario_id")
    private Funcionario funcionario;

    private LocalDate data;
    private LocalTime hora;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Funcionario getFuncionario() { return funcionario; }
    public void setFuncionario(Funcionario funcionario) { this.funcionario = funcionario; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
}

// =====================
// REPOSITORIES
// =====================

@Repository
interface FuncionarioRepository extends JpaRepository<Funcionario, Integer> {
}

@Repository
interface DepartamentoRepository extends JpaRepository<Departamento, Integer> {
}

@Repository
interface RegistroPontoRepository extends JpaRepository<RegistroPonto, Integer> {

    List<RegistroPonto> findByFuncionarioIdAndDataOrderByHora(Integer funcionarioId, LocalDate data);

}
