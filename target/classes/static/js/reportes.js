// reportes.js - Funciones para la gestión de reportes (compatible con admin y coordinación)
class ReportesManager {
    constructor() {
        this.charts = {};
        this.basePath = this.determinarBasePath();
        this.init();
    }

    init() {
        this.bindEvents();
        this.cargarEstadisticas();
        this.inicializarGraficos();
        this.cargarReportesRecientes();
        console.log('ReportesManager inicializado para:', this.basePath);
    }

    // ✅ DETERMINAR RUTA BASE AUTOMÁTICAMENTE
    determinarBasePath() {
        const currentPath = window.location.pathname;
        
        if (currentPath.includes('/admin/reportes')) {
            return '/admin';
        } else if (currentPath.includes('/coordinacion/reportes')) {
            return '/coordinacion';
        } else {
            // Por defecto, usar coordinación
            console.warn('No se pudo determinar la ruta base, usando coordinación por defecto');
            return '/coordinacion';
        }
    }

    // ✅ CONSTRUCTOR DE URLS DINÁMICO
    construirUrl(endpoint) {
        return `${this.basePath}${endpoint}`;
    }

    bindEvents() {
        // Cambio en el tipo de reporte
        const tipoReporteSelect = document.getElementById('tipoReporte');
        if (tipoReporteSelect) {
            tipoReporteSelect.addEventListener('change', (e) => {
                this.actualizarFiltros(e.target.value);
            });
        }

        // Envío del formulario
        const reporteForm = document.getElementById('reporteForm');
        if (reporteForm) {
            reporteForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.generarReporte();
            });
        }

        // Cambio en formato para mostrar/ocultar botón de vista previa
        const formatoSelect = document.getElementById('formato');
        if (formatoSelect) {
            formatoSelect.addEventListener('change', (e) => {
                this.actualizarBotonVistaPrevia(e.target.value);
            });
        }
    }

    actualizarFiltros(tipoReporte) {
        const programaSelect = document.getElementById('programa');
        const anioSelect = document.getElementById('anio');

        if (!programaSelect || !anioSelect) return;

        // Resetear filtros
        programaSelect.innerHTML = '<option value="">Todos los programas</option>';
        anioSelect.innerHTML = '<option value="">Todos los años</option>';

        // Cargar opciones según el tipo de reporte
        this.cargarOpcionesFiltro(tipoReporte);
    }

    async cargarOpcionesFiltro(tipoReporte) {
        try {
            const url = this.construirUrl(`/reportes/opciones-filtro?tipo=${tipoReporte}`);
            const response = await fetch(url);
            const data = await response.json();

            if (data.success) {
                this.actualizarSelect('programa', data.programas);
                this.actualizarSelect('anio', data.anios);
            }
        } catch (error) {
            console.error('Error cargando opciones de filtro:', error);
        }
    }

    actualizarSelect(selectId, opciones) {
        const select = document.getElementById(selectId);
        if (!select) return;

        opciones.forEach(opcion => {
            const option = document.createElement('option');
            option.value = opcion;
            option.textContent = opcion;
            select.appendChild(option);
        });
    }

    actualizarBotonVistaPrevia(formato) {
        const btnVistaPrevia = document.querySelector('button[onclick*="mostrarReporteEnPantalla"]');
        if (btnVistaPrevia) {
            if (formato === 'html') {
                btnVistaPrevia.style.display = 'inline-block';
            } else {
                btnVistaPrevia.style.display = 'none';
            }
        }
    }

    async generarReporte() {
        const formData = new FormData(document.getElementById('reporteForm'));
        const formato = formData.get('formato');
        const tipoReporte = formData.get('tipoReporte');

        if (formato === 'html') {
            await this.mostrarReporteEnPantalla();
        } else {
            await this.descargarReporte(formData);
        }
    }

    async mostrarReporteEnPantalla() {
        const formData = new FormData(document.getElementById('reporteForm'));
        const tipoReporte = formData.get('tipoReporte');

        if (!tipoReporte) {
            Swal.fire('Error', 'Por favor seleccione un tipo de reporte', 'error');
            return;
        }

        try {
            Swal.fire({
                title: 'Generando reporte...',
                text: 'Por favor espere',
                allowOutsideClick: false,
                didOpen: () => {
                    Swal.showLoading();
                }
            });

            const url = this.construirUrl('/reportes/generar-html');
            const response = await fetch(url, {
                method: 'POST',
                body: formData
            });

            const data = await response.json();

            if (data.success) {
                Swal.close();
                this.mostrarReporteHTML(data.reporte);
                this.guardarReporteReciente(tipoReporte);
            } else {
                throw new Error(data.error);
            }
        } catch (error) {
            Swal.fire('Error', 'No se pudo generar el reporte: ' + error.message, 'error');
        }
    }

    mostrarReporteHTML(contenidoReporte) {
        const areaReporte = document.getElementById('areaReporte');
        const tituloReporte = document.getElementById('tituloReporte');
        const contenido = document.getElementById('contenidoReporte');

        if (!areaReporte || !tituloReporte || !contenido) return;

        tituloReporte.textContent = contenidoReporte.titulo;
        contenido.innerHTML = contenidoReporte.html;

        areaReporte.style.display = 'block';
        areaReporte.scrollIntoView({ behavior: 'smooth' });
    }

    cerrarReporte() {
        const areaReporte = document.getElementById('areaReporte');
        if (areaReporte) {
            areaReporte.style.display = 'none';
        }
    }

    async descargarReporte(formData) {
        try {
            Swal.fire({
                title: 'Generando archivo...',
                text: 'Preparando descarga',
                allowOutsideClick: false,
                didOpen: () => {
                    Swal.showLoading();
                }
            });

            const url = this.construirUrl('/reportes/descargar');
            const response = await fetch(url, {
                method: 'POST',
                body: formData
            });

            if (response.ok) {
                const blob = await response.blob();
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                
                const tipoReporte = formData.get('tipoReporte');
                const formato = formData.get('formato');
                const fecha = new Date().toISOString().split('T')[0];
                
                a.download = `reporte_${tipoReporte}_${fecha}.${formato}`;
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
                window.URL.revokeObjectURL(url);

                Swal.fire('Éxito', 'Reporte descargado correctamente', 'success');
                this.guardarReporteReciente(tipoReporte);
            } else {
                throw new Error('Error en la respuesta del servidor');
            }
        } catch (error) {
            Swal.fire('Error', 'No se pudo descargar el reporte: ' + error.message, 'error');
        }
    }

    // Métodos para reportes predefinidos
    async generarReporteRendimiento() {
        this.seleccionarReporte('rendimiento');
    }

    async generarReporteEstudiantes() {
        this.seleccionarReporte('estudiantes');
    }

    async generarReporteBeneficios() {
        this.seleccionarReporte('beneficios');
    }

    async generarReporteResultados() {
        this.seleccionarReporte('resultados');
    }

    async generarReporteSemestral() {
        this.seleccionarReporte('consolidado');
    }

    async generarReporteEgresados() {
        Swal.fire('Info', 'Funcionalidad de reporte de egresados en desarrollo', 'info');
    }

    // ✅ MÉTODO AUXILIAR PARA SELECCIONAR REPORTE
    async seleccionarReporte(tipo) {
        const tipoReporteSelect = document.getElementById('tipoReporte');
        const formatoSelect = document.getElementById('formato');
        
        if (tipoReporteSelect) tipoReporteSelect.value = tipo;
        if (formatoSelect) formatoSelect.value = 'html';
        
        await this.mostrarReporteEnPantalla();
    }

    // Gráficos y estadísticas
    async cargarEstadisticas() {
        try {
            const url = this.construirUrl('/reportes/estadisticas');
            const response = await fetch(url);
            const data = await response.json();

            if (data.success) {
                this.actualizarEstadisticas(data.estadisticas);
            }
        } catch (error) {
            console.error('Error cargando estadísticas:', error);
        }
    }

    actualizarEstadisticas(estadisticas) {
        // Actualizar las tarjetas de estadísticas si existen
        const elementos = {
            'totalEstudiantes': estadisticas.totalEstudiantes,
            'totalResultados': estadisticas.totalResultados,
            'totalBeneficios': estadisticas.totalBeneficios,
            'beneficiosActivos': estadisticas.beneficiosActivos
        };

        for (const [key, value] of Object.entries(elementos)) {
            const elemento = document.querySelector(`[data-estadistica="${key}"]`);
            if (elemento) {
                elemento.textContent = value;
            }
        }
    }

    inicializarGraficos() {
        this.inicializarChartBeneficios();
        this.inicializarChartResultados();
    }

    inicializarChartBeneficios() {
        const ctx = document.getElementById('chartBeneficios');
        if (!ctx) return;

        this.charts.beneficios = new Chart(ctx.getContext('2d'), {
            type: 'doughnut',
            data: {
                labels: ['Exoneración', '50% Descuento', '100% Descuento'],
                datasets: [{
                    data: [0, 0, 0],
                    backgroundColor: ['#4CAF50', '#2196F3', '#FF9800']
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    title: {
                        display: true,
                        text: 'Distribución de Beneficios'
                    }
                }
            }
        });

        this.actualizarChartBeneficios();
    }

    inicializarChartResultados() {
        const ctx = document.getElementById('chartResultados');
        if (!ctx) return;

        this.charts.resultados = new Chart(ctx.getContext('2d'), {
            type: 'bar',
            data: {
                labels: ['Saber PRO', 'Saber T&T'],
                datasets: [{
                    label: 'Aprobados',
                    data: [0, 0],
                    backgroundColor: '#4CAF50'
                }, {
                    label: 'Reprobados',
                    data: [0, 0],
                    backgroundColor: '#F44336'
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    title: {
                        display: true,
                        text: 'Resultados por Tipo de Prueba'
                    }
                }
            }
        });

        this.actualizarChartResultados();
    }

    async actualizarChartBeneficios() {
        try {
            const url = this.construirUrl('/reportes/estadisticas-beneficios');
            const response = await fetch(url);
            const data = await response.json();

            if (data.success && this.charts.beneficios) {
                this.charts.beneficios.data.datasets[0].data = [
                    data.exoneracion,
                    data.descuento50,
                    data.descuento100
                ];
                this.charts.beneficios.update();
            }
        } catch (error) {
            console.error('Error actualizando chart beneficios:', error);
        }
    }

    async actualizarChartResultados() {
        try {
            const url = this.construirUrl('/reportes/estadisticas-resultados');
            const response = await fetch(url);
            const data = await response.json();

            if (data.success && this.charts.resultados) {
                this.charts.resultados.data.datasets[0].data = [
                    data.saberProAprobados,
                    data.saberTytAprobados
                ];
                this.charts.resultados.data.datasets[1].data = [
                    data.saberProReprobados,
                    data.saberTytReprobados
                ];
                this.charts.resultados.update();
            }
        } catch (error) {
            console.error('Error actualizando chart resultados:', error);
        }
    }

    // Reportes recientes
    cargarReportesRecientes() {
        const reportes = JSON.parse(localStorage.getItem('reportesRecientes') || '[]');
        this.mostrarReportesRecientes(reportes);
    }

    guardarReporteReciente(tipoReporte) {
        const reportes = JSON.parse(localStorage.getItem('reportesRecientes') || '[]');
        
        const nuevoReporte = {
            id: Date.now(),
            tipo: tipoReporte,
            nombre: this.obtenerNombreReporte(tipoReporte),
            fecha: new Date().toLocaleDateString('es-ES'),
            hora: new Date().toLocaleTimeString('es-ES'),
            tamaño: '1.2 MB',
            basePath: this.basePath // Guardar también la ruta base
        };

        // Agregar al inicio y mantener máximo 5 reportes
        reportes.unshift(nuevoReporte);
        if (reportes.length > 5) {
            reportes.pop();
        }

        localStorage.setItem('reportesRecientes', JSON.stringify(reportes));
        this.mostrarReportesRecientes(reportes);
    }

    obtenerNombreReporte(tipo) {
        const nombres = {
            'rendimiento': 'Reporte de Rendimiento',
            'estudiantes': 'Estadísticas Estudiantiles',
            'beneficios': 'Reporte de Beneficios',
            'resultados': 'Resultados de Pruebas',
            'consolidado': 'Reporte Consolidado Semestral'
        };
        return nombres[tipo] || 'Reporte Generado';
    }

    mostrarReportesRecientes(reportes) {
        const lista = document.getElementById('listaReportesRecientes');
        if (!lista) return;
        
        if (reportes.length === 0) {
            lista.innerHTML = '<p class="text-muted text-center">No hay reportes recientes</p>';
            return;
        }

        lista.innerHTML = reportes.map(reporte => `
            <div class="activity-item">
                <div class="activity-icon" style="background: #2196f3;">
                    <i class="fas fa-file-pdf"></i>
                </div>
                <div class="activity-content">
                    <p><strong>${reporte.nombre}</strong></p>
                    <span class="activity-time">Generado el ${reporte.fecha} a las ${reporte.hora} - ${reporte.tamaño}</span>
                    <small class="text-muted">${reporte.basePath === '/admin' ? 'Admin' : 'Coordinación'}</small>
                </div>
                <button class="btn btn-outline btn-sm" onclick="reportesManager.descargarReporteReciente(${reporte.id})">
                    <i class="fas fa-download"></i> Descargar
                </button>
            </div>
        `).join('');
    }

    descargarReporteReciente(id) {
        Swal.fire('Info', 'La funcionalidad de re-descarga estará disponible pronto', 'info');
    }
}

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    window.reportesManager = new ReportesManager();
});

// Funciones globales para HTML
function mostrarReporteEnPantalla() {
    if (window.reportesManager) {
        window.reportesManager.mostrarReporteEnPantalla();
    }
}

function cerrarReporte() {
    if (window.reportesManager) {
        window.reportesManager.cerrarReporte();
    }
}