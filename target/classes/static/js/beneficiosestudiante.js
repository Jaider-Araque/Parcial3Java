/**
 * beneficiosestudiante.js
 * Funcionalidad JavaScript para la página de beneficios del estudiante
 * UTS - Sistema Saber Pro
 */

const beneficiosApp = {
    
    // Inicialización de la aplicación
    init: function() {
        console.log('🚀 Inicializando aplicación de beneficios...');
        this.animarEstadisticas();
        this.configurarEventListeners();
    },
    
    // Configurar event listeners
    configurarEventListeners: function() {
        console.log('🎯 Configurando event listeners...');
        
        // Event listeners para botones de descarga
        document.querySelectorAll('.btn-icon[title="Descargar Certificado"]').forEach(btn => {
            btn.addEventListener('click', function(e) {
                const beneficioId = this.getAttribute('data-beneficio-id');
                if (beneficioId) {
                    beneficiosApp.descargarCertificadoBeneficio(beneficioId);
                }
            });
        });
        
        // Event listeners para botones de solicitud
        document.querySelectorAll('.btn-primary.btn-sm').forEach(btn => {
            btn.addEventListener('click', function(e) {
                const pruebaId = this.getAttribute('data-prueba-id');
                if (pruebaId) {
                    beneficiosApp.solicitarBeneficio(pruebaId);
                }
            });
        });
    },
    
    // Descargar certificado de beneficio
    descargarCertificadoBeneficio: function(beneficioId) {
        console.log(`📄 Descargando certificado para beneficio: ${beneficioId}`);
        
        Swal.fire({
            title: 'Descargando certificado',
            text: 'Preparando tu certificado de beneficio...',
            icon: 'info',
            showConfirmButton: false,
            timer: 1500
        });
        
        setTimeout(() => {
            window.location.href = '/estudiante/beneficios/descargar/' + beneficioId;
        }, 1500);
    },
    
    // Solicitar beneficio para prueba aprobada
    solicitarBeneficio: function(pruebaId) {
        console.log(`📝 Solicitando beneficio para prueba: ${pruebaId}`);
        
        Swal.fire({
            title: 'Solicitar Beneficio',
            text: '¿Deseas solicitar el beneficio para esta prueba?',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#bed52f',
            cancelButtonColor: '#29313c',
            confirmButtonText: 'Sí, solicitar',
            cancelButtonText: 'Cancelar',
            customClass: {
                confirmButton: 'swal2-confirm-btn',
                cancelButton: 'swal2-cancel-btn'
            }
        }).then((result) => {
            if (result.isConfirmed) {
                this.procesarSolicitudBeneficio(pruebaId);
            }
        });
    },
    
    // Procesar la solicitud de beneficio
    procesarSolicitudBeneficio: function(pruebaId) {
        console.log(`🔄 Procesando solicitud para prueba: ${pruebaId}`);
        
        // Mostrar loading
        Swal.fire({
            title: 'Procesando solicitud',
            text: 'Estamos enviando tu solicitud a coordinación...',
            icon: 'info',
            showConfirmButton: false,
            allowOutsideClick: false,
            didOpen: () => {
                Swal.showLoading();
            }
        });
        
        // Simular llamada AJAX (en producción sería real)
        setTimeout(() => {
            this.mostrarResultadoSolicitud(true, pruebaId);
        }, 2000);
    },
    
    // Mostrar resultado de la solicitud
    mostrarResultadoSolicitud: function(exitoso, pruebaId) {
        if (exitoso) {
            Swal.fire({
                title: '¡Solicitud Exitosa!',
                text: 'Tu solicitud de beneficio ha sido enviada a coordinación y será procesada en un máximo de 48 horas.',
                icon: 'success',
                confirmButtonColor: '#bed52f',
                confirmButtonText: 'Entendido',
                customClass: {
                    confirmButton: 'swal2-confirm-btn'
                }
            }).then(() => {
                // Recargar la página para actualizar los datos
                window.location.reload();
            });
        } else {
            Swal.fire({
                title: 'Error en la Solicitud',
                text: 'No pudimos procesar tu solicitud. Por favor, intenta nuevamente o contacta a soporte.',
                icon: 'error',
                confirmButtonColor: '#dc3545',
                confirmButtonText: 'Reintentar',
                customClass: {
                    confirmButton: 'swal2-error-btn'
                }
            }).then(() => {
                this.solicitarBeneficio(pruebaId);
            });
        }
    },
    
    // Animación de estadísticas
    animarEstadisticas: function() {
        console.log('🎨 Animando estadísticas...');
        
        setTimeout(() => {
            const statNumbers = document.querySelectorAll('.stat-number');
            
            statNumbers.forEach((stat) => {
                const text = stat.textContent;
                
                // Verificar si es un número válido
                if (text && text !== '--' && !isNaN(parseInt(text.replace(/[^0-9]/g, '')))) {
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
            
            // Formatear según si es moneda o número
            if (element.textContent.includes('$')) {
                element.textContent = '$' + currentValue.toLocaleString();
            } else {
                element.textContent = currentValue.toLocaleString();
            }
            
            if (progress < 1) {
                window.requestAnimationFrame(step);
            }
        };
        
        window.requestAnimationFrame(step);
    },
    
    // Validar elegibilidad para beneficios
    validarElegibilidad: function(puntaje, tipoPrueba) {
        const rangos = {
            'SABER_TYT': [
                { min: 120, max: 150, beneficio: 'EXONERACION_TRABAJO_GRADO', nota: 4.5, descuento: 0 },
                { min: 151, max: 170, beneficio: 'DESCUENTO_50_DERECHOS_GRADO', nota: 4.7, descuento: 50 },
                { min: 171, max: 500, beneficio: 'DESCUENTO_100_DERECHOS_GRADO', nota: 5.0, descuento: 100 }
            ],
            'SABER_PRO': [
                { min: 180, max: 210, beneficio: 'EXONERACION_TRABAJO_GRADO', nota: 4.5, descuento: 0 },
                { min: 211, max: 240, beneficio: 'DESCUENTO_50_DERECHOS_GRADO', nota: 4.7, descuento: 50 },
                { min: 241, max: 500, beneficio: 'DESCUENTO_100_DERECHOS_GRADO', nota: 5.0, descuento: 100 }
            ]
        };
        
        const rangoPrueba = rangos[tipoPrueba];
        if (!rangoPrueba) return null;
        
        return rangoPrueba.find(rango => puntaje >= rango.min && puntaje <= rango.max);
    },
    
    // Calcular valor estimado del beneficio
    calcularValorBeneficio: function(porcentajeDescuento, notaAsignada) {
        let valor = 0;
        
        // Valor por descuento en derechos de grado (estimado: $1,000,000)
        if (porcentajeDescuento > 0) {
            valor += (porcentajeDescuento / 100) * 1000000;
        }
        
        // Valor por exoneración de trabajo de grado (estimado: $500,000)
        if (notaAsignada >= 4.5) {
            valor += 500000;
        }
        
        return valor;
    },
    
    // Mostrar detalles del beneficio
    mostrarDetallesBeneficio: function(beneficioId) {
        console.log(`🔍 Mostrando detalles del beneficio: ${beneficioId}`);
        
        // En una implementación real, harías una llamada AJAX para obtener los detalles
        Swal.fire({
            title: 'Detalles del Beneficio',
            html: `
                <div class="beneficio-detalle">
                    <p><strong>ID:</strong> ${beneficioId}</p>
                    <p><strong>Estado:</strong> Activo</p>
                    <p><strong>Fecha de Asignación:</strong> ${new Date().toLocaleDateString()}</p>
                    <p><strong>Valor Estimado:</strong> $${this.calcularValorBeneficio(50, 4.7).toLocaleString()}</p>
                </div>
            `,
            icon: 'info',
            confirmButtonText: 'Cerrar',
            confirmButtonColor: '#bed52f'
        });
    },
    
    // Exportar datos de beneficios
    exportarBeneficios: function() {
        console.log('📊 Exportando datos de beneficios...');
        
        Swal.fire({
            title: 'Exportar Beneficios',
            text: '¿Deseas exportar tu historial de beneficios en formato PDF?',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#bed52f',
            cancelButtonColor: '#29313c',
            confirmButtonText: 'Exportar PDF',
            cancelButtonText: 'Cancelar'
        }).then((result) => {
            if (result.isConfirmed) {
                window.location.href = '/estudiante/beneficios/exportar/pdf';
            }
        });
    },
    
    // Método de utilidad para formatear moneda
    formatearMoneda: function(valor) {
        return new Intl.NumberFormat('es-CO', {
            style: 'currency',
            currency: 'COP',
            minimumFractionDigits: 0
        }).format(valor);
    },
    
    // Método de utilidad para formatear fecha
    formatearFecha: function(fecha) {
        return new Date(fecha).toLocaleDateString('es-CO', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    }
};

// Inicializar la aplicación cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    beneficiosApp.init();
});

// Exportar para uso global (si es necesario)
window.beneficiosApp = beneficiosApp;