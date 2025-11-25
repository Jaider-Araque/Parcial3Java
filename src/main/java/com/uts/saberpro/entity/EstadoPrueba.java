package com.uts.saberpro.entity;

public enum EstadoPrueba {
    PENDIENTE,      // Prueba registrada pero sin resultados
    APROBADO,       // Prueba aprobada
    REPROBADO,      // Prueba reprobada  
    ANULADA,        // Prueba anulada
    INCOMPLETA      // Prueba con resultados parciales
}