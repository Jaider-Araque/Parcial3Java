// beneficios.js - Funciones para la gestión de beneficios
class BeneficiosManager {
    constructor() {
        this.init();
    }

    init() {
        this.bindEvents();
        this.showFlashMessages();
        console.log('BeneficiosManager inicializado');
    }

    bindEvents() {
        // Enter en campo de búsqueda
        document.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && e.target.name === 'documento') {
                this.buscar();
            }
        });
    }

    // Funciones de búsqueda y paginación
    cambiarPagina(pagina) {
        console.log('Cambiando a página:', pagina);
        const paginaInput = document.getElementById('paginaInput');
        const filtroForm = document.getElementById('filtroForm');
        
        if (paginaInput && filtroForm) {
            paginaInput.value = pagina;
            filtroForm.submit();
        } else {
            console.error('No se encontraron los elementos necesarios para cambiar de página');
        }
    }

    buscar() {
        const paginaInput = document.getElementById('paginaInput');
        const filtroForm = document.getElementById('filtroForm');
        
        if (paginaInput && filtroForm) {
            paginaInput.value = 0;
            filtroForm.submit();
        } else {
            console.error('No se encontraron los elementos necesarios para la búsqueda');
        }
    }

    // ✅ Función para calcular beneficios automáticos
    async calcularBeneficiosAutomaticos() {
        const result = await Swal.fire({
            title: '¿Calcular beneficios automáticos?',
            html: 'Esta acción calculará beneficios para todos los estudiantes con pruebas <strong>APROBADAS</strong> según el Acuerdo 01-009.<br><br>' +
                  '<strong>Condiciones:</strong><br>' +
                  '• Solo pruebas APROBADAS generan beneficios<br>' +
                  '• Saber T&T: 120-200 puntos<br>' +
                  '• Saber PRO: 180-300 puntos<br>' +
                  '• No aplica para pruebas reprobadas o anuladas',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#28a745',
            cancelButtonColor: '#6c757d',
            confirmButtonText: 'Sí, calcular',
            cancelButtonText: 'Cancelar',
            backdrop: true,
            allowOutsideClick: false
        });

        if (result.isConfirmed) {
            try {
                Swal.fire({
                    title: 'Calculando...',
                    text: 'Procesando beneficios automáticos',
                    allowOutsideClick: false,
                    didOpen: () => {
                        Swal.showLoading();
                    }
                });

                const response = await fetch('/coordinacion/beneficios/calcular-automaticos', {
                    method: 'POST'
                });
                
                const data = await response.json();
                
                if (data.success) {
                    Swal.fire({
                        title: '¡Éxito!',
                        html: `Beneficios calculados exitosamente<br><br>
                              <strong>Beneficios creados:</strong> ${data.beneficiosCreados}<br>
                              <strong>Condiciones aplicadas:</strong><br>
                              • Solo pruebas APROBADAS<br>
                              • Según puntajes del Acuerdo 01-009`,
                        icon: 'success',
                        confirmButtonText: 'Aceptar'
                    }).then(() => {
                        // Recargar la página para ver los cambios
                        window.location.reload();
                    });
                } else {
                    throw new Error(data.error || 'Error en el servidor');
                }
            } catch (error) {
                console.error('Error al calcular beneficios:', error);
                Swal.fire({
                    title: 'Error!',
                    text: 'No se pudieron calcular los beneficios automáticos: ' + error.message,
                    icon: 'error',
                    confirmButtonText: 'Entendido'
                });
            }
        }
    }

    // Funciones de CRUD (solo activar/desactivar beneficios existentes)
    async desactivarBeneficio(id, nombreEstudiante) {
        const result = await Swal.fire({
            title: '¿Estás seguro?',
            html: 'Esta acción desactivará el beneficio del estudiante: <strong>' + nombreEstudiante + '</strong>',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            cancelButtonColor: '#3085d6',
            confirmButtonText: 'Sí, desactivar',
            cancelButtonText: 'Cancelar',
            backdrop: true,
            allowOutsideClick: false
        });

        if (result.isConfirmed) {
            try {
                const response = await fetch('/coordinacion/beneficios/desactivar/' + id, {
                    method: 'PUT'
                });
                
                if (response.ok) {
                    const result = await response.text();
                    if (result === 'OK') {
                        Swal.fire('Desactivado!', 'El beneficio ha sido desactivado.', 'success');
                        setTimeout(() => location.reload(), 1500);
                    } else {
                        throw new Error(result);
                    }
                } else {
                    throw new Error('Error en la respuesta del servidor');
                }
            } catch (error) {
                Swal.fire('Error!', 'No se pudo desactivar el beneficio: ' + error.message, 'error');
            }
        }
    }

    async activarBeneficio(id, nombreEstudiante) {
        const result = await Swal.fire({
            title: '¿Estás seguro?',
            html: 'Esta acción activará el beneficio del estudiante: <strong>' + nombreEstudiante + '</strong>',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#28a745',
            cancelButtonColor: '#6c757d',
            confirmButtonText: 'Sí, activar',
            cancelButtonText: 'Cancelar',
            backdrop: true,
            allowOutsideClick: false
        });

        if (result.isConfirmed) {
            try {
                const response = await fetch('/coordinacion/beneficios/activar/' + id, {
                    method: 'PUT'
                });
                
                if (response.ok) {
                    const result = await response.text();
                    if (result === 'OK') {
                        Swal.fire('Activado!', 'El beneficio ha sido activado.', 'success');
                        setTimeout(() => location.reload(), 1500);
                    } else {
                        throw new Error(result);
                    }
                } else {
                    throw new Error('Error en la respuesta del servidor');
                }
            } catch (error) {
                Swal.fire('Error!', 'No se pudo activar el beneficio: ' + error.message, 'error');
            }
        }
    }

    showFlashMessages() {
        const urlParams = new URLSearchParams(window.location.search);
        
        if (urlParams.has('exito')) {
            const exito = urlParams.get('exito');
            if (exito) {
                Swal.fire({
                    title: 'Éxito!',
                    text: exito,
                    icon: 'success',
                    confirmButtonText: 'Aceptar'
                }).then(() => {
                    // Limpiar URL después de mostrar el mensaje
                    const nuevaUrl = window.location.pathname + window.location.search.replace(/[?&]exito=[^&]+/, '').replace(/^&/, '?');
                    window.history.replaceState({}, document.title, nuevaUrl);
                });
            }
        }
        
        if (urlParams.has('error')) {
            const error = urlParams.get('error');
            if (error) {
                Swal.fire({
                    title: 'Error!',
                    text: error,
                    icon: 'error',
                    confirmButtonText: 'Entendido'
                }).then(() => {
                    // Limpiar URL después de mostrar el mensaje
                    const nuevaUrl = window.location.pathname + window.location.search.replace(/[?&]error=[^&]+/, '').replace(/^&/, '?');
                    window.history.replaceState({}, document.title, nuevaUrl);
                });
            }
        }
    }
}

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    window.beneficiosManager = new BeneficiosManager();
    console.log('DOM cargado, BeneficiosManager listo');
});

// Funciones globales para los onclick del HTML
function calcularBeneficiosAutomaticos() {
    if (window.beneficiosManager) {
        window.beneficiosManager.calcularBeneficiosAutomaticos();
    } else {
        console.error('BeneficiosManager no está inicializado');
    }
}

function desactivarBeneficio(id, nombreEstudiante) {
    if (window.beneficiosManager) {
        window.beneficiosManager.desactivarBeneficio(id, nombreEstudiante);
    } else {
        console.error('BeneficiosManager no está inicializado');
    }
}

function activarBeneficio(id, nombreEstudiante) {
    if (window.beneficiosManager) {
        window.beneficiosManager.activarBeneficio(id, nombreEstudiante);
    } else {
        console.error('BeneficiosManager no está inicializado');
    }
}

function cambiarPagina(pagina) {
    if (window.beneficiosManager) {
        window.beneficiosManager.cambiarPagina(pagina);
    } else {
        console.error('BeneficiosManager no está inicializado');
    }
}

function buscar() {
    if (window.beneficiosManager) {
        window.beneficiosManager.buscar();
    } else {
        console.error('BeneficiosManager no está inicializado');
    }
}