/**
 * estadisticasestudiante.js
 * Funcionalidad JavaScript para la página de estadísticas del estudiante
 * UTS - Sistema Saber Pro
 */

const estadisticasApp = {
    
    // Variables para almacenar datos
    charts: {},
    datosEstudiante: {},
    
    // Inicialización de la aplicación
    init: function() {
        console.log('📈 Inicializando aplicación de estadísticas...');
        this.cargarDatosEstudiante();
        this.inicializarGraficas();
        this.configurarEventListeners();
        this.animarEstadisticas();
    },
    
    // Cargar datos del estudiante desde Thymeleaf
    cargarDatosEstudiante: function() {
        console.log('📊 Cargando datos del estudiante...');
        
        // En una implementación real, estos datos vendrían del backend
        // Por ahora simulamos datos basados en la estructura Thymeleaf
        this.datosEstudiante = {
            promedio: parseInt(document.querySelector('.stat-number')?.textContent?.replace('%', '')) || 0,
            evolucionPuntajes: this.obtenerEvolucionDesdeHTML(),
            competencias: this.obtenerCompetenciasDesdeHTML(),
            ultimasPruebas: this.obtenerUltimasPruebasDesdeHTML()
        };
        
        console.log('Datos cargados:', this.datosEstudiante);
    },
    
    // Obtener datos de evolución desde el HTML
    obtenerEvolucionDesdeHTML: function() {
        // En una implementación real, esto vendría de thymeleaf: ${evolucionPuntajes}
        return [
            { periodo: '2024-1', puntaje: 85, tipo: 'SABER_PRO' },
            { periodo: '2023-2', puntaje: 78, tipo: 'SABER_TYT' },
            { periodo: '2023-1', puntaje: 70, tipo: 'SABER_TYT' },
            { periodo: '2022-2', puntaje: 65, tipo: 'SABER_PRO' }
        ];
    },
    
    // Obtener datos de competencias desde el HTML
    obtenerCompetenciasDesdeHTML: function() {
        // En una implementación real, esto vendría del backend
        return [
            { competencia: 'Comunicativa', puntaje: 88 },
            { competencia: 'Cuantitativa', puntaje: 82 },
            { competencia: 'Inglés', puntaje: 80 },
            { competencia: 'Ciudadanas', puntaje: 90 },
            { competencia: 'Lectura Crítica', puntaje: 85 }
        ];
    },
    
    // Obtener últimas pruebas desde el HTML
    obtenerUltimasPruebasDesdeHTML: function() {
        // Esto se llenaría con thymeleaf: ${ultimasPruebas}
        return [
            { tipoPrueba: 'SABER_PRO', puntajeGlobal: 215 },
            { tipoPrueba: 'SABER_TYT', puntajeGlobal: 165 },
            { tipoPrueba: 'SABER_PRO', puntajeGlobal: 195 }
        ];
    },
    
    // Inicializar gráficas con Chart.js
    inicializarGraficas: function() {
        console.log('📊 Inicializando gráficas...');
        
        this.inicializarGraficaEvolucion();
        this.inicializarGraficaCompetencias();
        
        // Ajustar gráficas al redimensionar
        window.addEventListener('resize', () => {
            Object.values(this.charts).forEach(chart => {
                if (chart) chart.resize();
            });
        });
    },
    
    // Gráfica de evolución del rendimiento
    inicializarGraficaEvolucion: function() {
        const ctx = document.getElementById('evolucionChart')?.getContext('2d');
        if (!ctx) return;
        
        const datos = this.datosEstudiante.evolucionPuntajes;
        
        this.charts.evolucion = new Chart(ctx, {
            type: 'line',
            data: {
                labels: datos.map(d => d.periodo),
                datasets: [{
                    label: 'Puntaje Global',
                    data: datos.map(d => d.puntaje),
                    borderColor: '#bed52f',
                    backgroundColor: 'rgba(190, 213, 47, 0.1)',
                    borderWidth: 3,
                    fill: true,
                    tension: 0.4,
                    pointBackgroundColor: '#bed52f',
                    pointBorderColor: '#ffffff',
                    pointBorderWidth: 2,
                    pointRadius: 6,
                    pointHoverRadius: 8
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: false,
                        min: 50,
                        max: 250,
                        title: {
                            display: true,
                            text: 'Puntaje'
                        }
                    },
                    x: {
                        title: {
                            display: true,
                            text: 'Periodo Académico'
                        }
                    }
                }
            }
        });
    },
    
    // Gráfica de distribución de competencias
    inicializarGraficaCompetencias: function() {
        const ctx = document.getElementById('competenciasChart')?.getContext('2d');
        if (!ctx) return;
        
        const datos = this.datosEstudiante.competencias;
        
        this.charts.competencias = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: datos.map(d => d.competencia),
                datasets: [{
                    label: 'Puntaje por Competencia',
                    data: datos.map(d => d.puntaje),
                    backgroundColor: [
                        'rgba(190, 213, 47, 0.8)',
                        'rgba(76, 175, 80, 0.8)',
                        'rgba(33, 150, 243, 0.8)',
                        'rgba(156, 39, 176, 0.8)',
                        'rgba(255, 152, 0, 0.8)'
                    ],
                    borderColor: [
                        '#bed52f',
                        '#4caf50',
                        '#2196f3',
                        '#9c27b0',
                        '#ff9800'
                    ],
                    borderWidth: 2,
                    borderRadius: 8
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return `Puntaje: ${context.parsed.y}%`;
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 100,
                        title: {
                            display: true,
                            text: 'Puntaje (%)'
                        }
                    },
                    x: {
                        title: {
                            display: true,
                            text: 'Competencias'
                        }
                    }
                }
            }
        });
    },
    
    // Configurar event listeners
    configurarEventListeners: function() {
        console.log('🎯 Configurando event listeners...');
        
        // Event listener para exportar estadísticas
        const btnExportar = document.getElementById('btnExportarEstadisticas');
        if (btnExportar) {
            btnExportar.addEventListener('click', () => this.exportarEstadisticas());
        }
        
        // Event listener para actualizar gráficas
        const btnActualizar = document.getElementById('btnActualizarGraficas');
        if (btnActualizar) {
            btnActualizar.addEventListener('click', () => this.actualizarGraficas());
        }
    },
    
    // Animar estadísticas al cargar
    animarEstadisticas: function() {
        console.log('🎨 Animando estadísticas...');
        
        setTimeout(() => {
            const statNumbers = document.querySelectorAll('.stat-number');
            
            statNumbers.forEach((stat) => {
                const text = stat.textContent;
                
                if (text && text !== 'N/A' && !isNaN(parseInt(text.replace(/[^0-9]/g, '')))) {
                    const valorFinal = parseInt(text.replace(/[^0-9]/g, '')) || 0;
                    
                    if (valorFinal > 0) {
                        this.animateValue(stat, 0, valorFinal, 1500);
                    }
                }
            });
        }, 500);
    },
    
    // Función para animar valores numéricos
    animateValue: function(element, start, end, duration) {
        let startTimestamp = null;
        
        const step = (timestamp) => {
            if (!startTimestamp) startTimestamp = timestamp;
            const progress = Math.min((timestamp - startTimestamp) / duration, 1);
            
            const currentValue = Math.floor(progress * (end - start) + start);
            
            // Preservar el formato original (%, #, etc.)
            const originalText = element.textContent;
            if (originalText.includes('%')) {
                element.textContent = currentValue + '%';
            } else if (originalText.includes('#')) {
                element.textContent = '#' + currentValue;
            } else if (originalText.includes('/')) {
                // Para formato "X/Y"
                const parts = originalText.split('/');
                if (parts.length === 2) {
                    element.textContent = currentValue + '/' + parts[1];
                }
            } else {
                element.textContent = currentValue;
            }
            
            if (progress < 1) {
                window.requestAnimationFrame(step);
            }
        };
        
        window.requestAnimationFrame(step);
    },
    
    // Actualizar gráficas con nuevos datos
    actualizarGraficas: function() {
        console.log('🔄 Actualizando gráficas...');
        
        Swal.fire({
            title: 'Actualizando datos',
            text: 'Cargando información más reciente...',
            icon: 'info',
            showConfirmButton: false,
            timer: 1000
        });
        
        // Simular recarga de datos
        setTimeout(() => {
            if (this.charts.evolucion) {
                this.charts.evolucion.destroy();
            }
            if (this.charts.competencias) {
                this.charts.competencias.destroy();
            }
            
            this.cargarDatosEstudiante();
            this.inicializarGraficas();
            
            Swal.fire({
                title: '¡Actualizado!',
                text: 'Las estadísticas se han actualizado correctamente.',
                icon: 'success',
                timer: 1500,
                showConfirmButton: false
            });
        }, 1000);
    },
    
    // Exportar estadísticas a PDF
    exportarEstadisticas: function() {
        console.log('📄 Exportando estadísticas...');
        
        Swal.fire({
            title: 'Exportar Estadísticas',
            text: '¿Deseas exportar tu reporte de estadísticas en formato PDF?',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#bed52f',
            cancelButtonColor: '#29313c',
            confirmButtonText: 'Exportar PDF',
            cancelButtonText: 'Cancelar'
        }).then((result) => {
            if (result.isConfirmed) {
                this.generarReportePDF();
            }
        });
    },
    
    // Generar reporte PDF (simulado)
    generarReportePDF: function() {
        Swal.fire({
            title: 'Generando reporte',
            text: 'Preparando tu documento PDF...',
            icon: 'info',
            showConfirmButton: false,
            timer: 2000
        });
        
        setTimeout(() => {
            // En una implementación real, aquí se generaría el PDF
            const enlaceDescarga = document.createElement('a');
            enlaceDescarga.href = '#';
            enlaceDescarga.download = `estadisticas-estudiante-${new Date().toISOString().split('T')[0]}.pdf`;
            enlaceDescarga.click();
            
            Swal.fire({
                title: '¡Reporte Listo!',
                text: 'Tu reporte de estadísticas ha sido generado exitosamente.',
                icon: 'success',
                confirmButtonText: 'Descargar',
                confirmButtonColor: '#bed52f'
            });
        }, 2000);
    },
    
    // Calcular métricas adicionales
    calcularMetricas: function() {
        const datos = this.datosEstudiante;
        
        return {
            tendencia: this.calcularTendencia(datos.evolucionPuntajes),
            fortalezas: this.identificarFortalezas(datos.competencias),
            areasMejora: this.identificarAreasMejora(datos.competencias)
        };
    },
    
    // Calcular tendencia de rendimiento
    calcularTendencia: function(evolucion) {
        if (evolucion.length < 2) return 'estable';
        
        const primerPuntaje = evolucion[evolucion.length - 1].puntaje;
        const ultimoPuntaje = evolucion[0].puntaje;
        
        const diferencia = ultimoPuntaje - primerPuntaje;
        
        if (diferencia > 10) return 'mejora-significativa';
        if (diferencia > 5) return 'mejora-moderada';
        if (diferencia > 0) return 'mejora-leve';
        if (diferencia === 0) return 'estable';
        return 'disminucion';
    },
    
    // Identificar fortalezas del estudiante
    identificarFortalezas: function(competencias) {
        return competencias
            .filter(c => c.puntaje >= 85)
            .map(c => c.competencia);
    },
    
    // Identificar áreas de mejora
    identificarAreasMejora: function(competencias) {
        return competencias
            .filter(c => c.puntaje < 75)
            .map(c => c.competencia);
    },
    
    // Método de utilidad para formatear números
    formatearNumero: function(numero) {
        return new Intl.NumberFormat('es-CO').format(numero);
    },
    
    // Método de utilidad para formatear porcentajes
    formatearPorcentaje: function(valor) {
        return new Intl.NumberFormat('es-CO', {
            style: 'percent',
            minimumFractionDigits: 1
        }).format(valor / 100);
    }
};

// Inicializar la aplicación cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    estadisticasApp.init();
});

// Exportar para uso global
window.estadisticasApp = estadisticasApp;