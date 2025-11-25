package com.uts.saberpro.service;

import com.uts.saberpro.entity.EstadoPrueba;
import com.uts.saberpro.entity.Estudiante;
import com.uts.saberpro.entity.ResultadoPrueba;
import com.uts.saberpro.entity.TipoPrueba;
import com.uts.saberpro.repository.ResultadoPruebaRepository;
import com.uts.saberpro.repository.EstudianteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResultadoPruebaService {

    private final ResultadoPruebaRepository resultadoPruebaRepository;
    private final EstudianteRepository estudianteRepository;

    public ResultadoPruebaService(ResultadoPruebaRepository resultadoPruebaRepository, 
                                 EstudianteRepository estudianteRepository) {
        this.resultadoPruebaRepository = resultadoPruebaRepository;
        this.estudianteRepository = estudianteRepository;
    }

    public List<ResultadoPrueba> obtenerTodosResultados() {
        return resultadoPruebaRepository.findAll();
    }

    public Optional<ResultadoPrueba> obtenerResultadoPorId(Long id) {
        return resultadoPruebaRepository.findById(id);
    }

    public List<ResultadoPrueba> obtenerResultadosPorEstudiante(String estudianteId) {
        // Buscar el estudiante primero, luego sus resultados
        Optional<Estudiante> estudiante = estudianteRepository.findById(estudianteId);
        if (estudiante.isPresent()) {
            return resultadoPruebaRepository.findByEstudiante(estudiante.get());
        }
        return List.of(); // Retorna lista vacía si no encuentra el estudiante
    }

    public ResultadoPrueba guardarResultado(ResultadoPrueba resultado) {
        return resultadoPruebaRepository.save(resultado);
    }

    public void eliminarResultado(Long id) {
        resultadoPruebaRepository.deleteById(id);
    }

    public long contarResultados() {
        return resultadoPruebaRepository.count();
    }

    public long contarResultadosAprobados() {
        return resultadoPruebaRepository.findByEstado(EstadoPrueba.APROBADO).size();
    }

    public long contarResultadosPorTipo(TipoPrueba tipoPrueba) {
        List<ResultadoPrueba> resultados = resultadoPruebaRepository.findAll();
        return resultados.stream()
                .filter(resultado -> resultado.getTipoPrueba() == tipoPrueba)
                .count();
    }

    public double obtenerPromedioGlobal() {
        List<ResultadoPrueba> resultados = resultadoPruebaRepository.findAll();
        if (resultados.isEmpty()) return 0.0;
        
        return resultados.stream()
                .mapToInt(ResultadoPrueba::getPuntajeGlobal)
                .average()
                .orElse(0.0);
    }

    public long contarEstudiantesConResultados() {
        List<ResultadoPrueba> resultados = resultadoPruebaRepository.findAll();
        return resultados.stream()
                .map(resultado -> resultado.getEstudiante().getNumeroDocumento())
                .distinct()
                .count();
    }

    // Métodos adicionales útiles
    public List<ResultadoPrueba> obtenerResultadosPorEstado(EstadoPrueba estado) {
        return resultadoPruebaRepository.findByEstado(estado);
    }

    public List<ResultadoPrueba> obtenerResultadosPorAnioYPeriodo(Integer anio, Integer periodo) {
        return resultadoPruebaRepository.findByAnioPruebaAndPeriodo(anio, periodo);
    }

    public Optional<ResultadoPrueba> obtenerUltimoResultadoEstudiante(Estudiante estudiante, TipoPrueba tipoPrueba) {
        return resultadoPruebaRepository.findTopByEstudianteAndTipoPruebaOrderByFechaRegistroDesc(estudiante, tipoPrueba);
    }

    public boolean estudianteTieneResultados(Estudiante estudiante) {
        return resultadoPruebaRepository.countByEstudianteAndTipoPrueba(estudiante, estudiante.getTipoPrueba()) > 0;
    }
}