package com.uts.saberpro.repository;

import com.uts.saberpro.entity.ImportacionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImportacionLogRespository extends JpaRepository<ImportacionLog, Long> {
    List<ImportacionLog> findByTipoImportacionOrderByFechaImportacionDesc(String tipoImportacion);
    List<ImportacionLog> findAllByOrderByFechaImportacionDesc();
}