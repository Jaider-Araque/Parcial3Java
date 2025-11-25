package com.uts.saberpro.service;

import com.uts.saberpro.entity.Estudiante;
import com.uts.saberpro.entity.TipoPrueba;
import com.uts.saberpro.repository.EstudianteRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class EstudianteService {
    
    private final EstudianteRepository estudianteRepository;
    
    public EstudianteService(EstudianteRepository estudianteRepository) {
        this.estudianteRepository = estudianteRepository;
    }
    
    public List<Estudiante> obtenerTodosEstudiantes() {
        return estudianteRepository.findAll();
    }
    
    public Optional<Estudiante> obtenerEstudiantePorDocumento(String numeroDocumento) {
        return estudianteRepository.findByNumeroDocumento(numeroDocumento);
    }
    
    public Estudiante guardarEstudiante(Estudiante estudiante) {
        return estudianteRepository.save(estudiante);
    }
    
    public void eliminarEstudiante(String numeroDocumento) {
        estudianteRepository.deleteById(numeroDocumento);
    }
    
    public boolean existeEstudiante(String numeroDocumento) {
        return estudianteRepository.existsByNumeroDocumento(numeroDocumento);
    }
    
    public List<Estudiante> obtenerEstudiantesPorPrograma(String programa) {
        return estudianteRepository.findByProgramaAcademico(programa);
    }
    
    // ✅ CORREGIDO: Usar el enum externo TipoPrueba
    public List<Estudiante> obtenerEstudiantesPorTipoPrueba(TipoPrueba tipoPrueba) {
        return estudianteRepository.findByTipoPrueba(tipoPrueba);
    }
    
    // ✅ AGREGAR: Método para contar estudiantes activos
    public long contarEstudiantesActivos() {
        return estudianteRepository.count();
    }
    
    // ✅ AGREGAR: Método para buscar por email
    public Optional<Estudiante> obtenerEstudiantePorEmail(String email) {
        return estudianteRepository.findByEmail(email);
    }
    
    // ✅ AGREGAR: Método para obtener estudiantes por semestre
    public List<Estudiante> obtenerEstudiantesPorSemestre(Integer semestre) {
        return estudianteRepository.findBySemestre(semestre);
    }
}