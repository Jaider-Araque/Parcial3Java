/**
 * Funcionalidades para la página de resultados del estudiante
 * Sistema Saber Pro UTS
 */

// Funciones globales
function descargarCertificado(pruebaId) {
    Swal.fire({
        title: 'Descargando certificado',
        text: 'Preparando tu certificado...',
        icon: 'info',
        showConfirmButton: false,
        timer: 1500
    });
    
    setTimeout(() => {
        // Simulación de descarga - implementar endpoint real
        Swal.fire({
            title: 'Función en desarrollo',
            text: 'La descarga de certificados estará disponible próximamente',
            icon: 'info',
            confirmButtonText: 'Entendido'
        });
    }, 1500);
}

function verGraficas(pruebaId) {
    Swal.fire({
        title: 'Vista de gráficas',
        text: 'Redirigiendo a vista detallada...',
        icon: 'info',
        showConfirmButton: false,
        timer: 1000
    });
    
    setTimeout(() => {
        // Simulación de redirección - implementar endpoint real
        Swal.fire({
            title: 'Función en desarrollo',
            text: 'La visualización de gráficas estará disponible próximamente',
            icon: 'info',
            confirmButtonText: 'Entendido'
        });
    }, 1000);
}

// Inicialización de la página
document.addEventListener('DOMContentLoaded', function() {
    console.log('Inicializando página de resultados del estudiante...');
    
    inicializarResultadosEstudiante();
    configurarAnimaciones();
    configurarEventos();
});

/**
 * Función principal de inicialización
 */
function inicializarResultadosEstudiante() {
    console.log('Configurando funcionalidades de resultados...');
    
    // Configurar navegación activa
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.sidebar-nav .nav-link');
    
    navLinks.forEach(link => {
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('active');
        }
    });
    
    // Mostrar información de debug en consola
    mostrarInfoResultados();
}

/**
 * Muestra información de debug en consola
 */
function mostrarInfoResultados() {
    const totalPruebas = document.querySelector('.stat-card:nth-child(1) .stat-number').textContent;
    const promedio = document.querySelector('.stat-card:nth-child(2) .stat-number').textContent;
    const mejoria = document.querySelector('.stat-card:nth-child(3) .stat-number').textContent;
    
    console.log('📊 Información de resultados cargada:');
    console.log('   - Total pruebas:', totalPruebas);
    console.log('   - Promedio:', promedio);
    console.log('   - Mejoría:', mejoria);
}

/**
 * Configura animaciones de la página
 */
function configurarAnimaciones() {
    setTimeout(() => {
        const statNumbers = document.querySelectorAll('.stat-number');
        statNumbers.forEach((stat) => {
            const text = stat.textContent;
            if (text && text !== '--' && !isNaN(parseInt(text.replace(/[^0-9]/g, '')))) {
                const valorFinal = parseInt(text.replace(/[^0-9]/g, '')) || 0;
                if (valorFinal > 0) {
                    animateValue(stat, 0, valorFinal, 1500);
                }
            }
        });
    }, 500);
}

/**
 * Anima un valor numérico
 */
function animateValue(element, start, end, duration) {
    let startTimestamp = null;
    const step = (timestamp) => {
        if (!startTimestamp) startTimestamp = timestamp;
        const progress = Math.min((timestamp - startTimestamp) / duration, 1);
        
        const currentValue = Math.floor(progress * (end - start) + start);
        
        if (element.textContent.includes('%')) {
            element.textContent = currentValue + '%';
        } else if (element.textContent.includes('#')) {
            element.textContent = '#' + currentValue;
        } else {
            element.textContent = currentValue;
        }
        
        if (progress < 1) {
            window.requestAnimationFrame(step);
        }
    };
    window.requestAnimationFrame(step);
}

/**
 * Configura eventos de la página
 */
function configurarEventos() {
    // Efectos hover para las tarjetas de resultados
    const resultItems = document.querySelectorAll('.result-item:not(.no-results)');
    resultItems.forEach(item => {
        item.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-3px)';
            this.style.boxShadow = '0 8px 25px rgba(0,0,0,0.1)';
        });
        
        item.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = 'none';
        });
    });
}

// Hacer funciones disponibles globalmente
window.descargarCertificado = descargarCertificado;
window.verGraficas = verGraficas;