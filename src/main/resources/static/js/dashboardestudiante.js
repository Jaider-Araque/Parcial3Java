/**
 * Dashboard Estudiante - Funcionalidades específicas para el panel del estudiante
 * Sistema Saber Pro UTS
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('Inicializando dashboard del estudiante...');
    inicializarDashboardEstudiante();
});

/**
 * Función principal de inicialización del dashboard
 */
function inicializarDashboardEstudiante() {
    configurarNavegacionActiva();
    configurarEventosDashboard();
    inicializarAnimaciones();
    configurarCerrarSesion();
}

/**
 * Configura la navegación activa en el sidebar
 */
function configurarNavegacionActiva() {
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.sidebar-nav .nav-link');
    
    navLinks.forEach(link => {
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('active');
        }
        
        // Agregar efecto hover a los enlaces
        link.addEventListener('mouseenter', function() {
            if (!this.classList.contains('active')) {
                this.style.backgroundColor = 'var(--gris-medio)';
            }
        });
        
        link.addEventListener('mouseleave', function() {
            if (!this.classList.contains('active')) {
                this.style.backgroundColor = '';
            }
        });
    });
}

/**
 * Configura los eventos del dashboard
 */
function configurarEventosDashboard() {
    // Efectos hover para las tarjetas de menú
    const menuCards = document.querySelectorAll('.menu-card');
    menuCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px) scale(1.02)';
            this.style.boxShadow = '0 8px 25px rgba(0,0,0,0.15)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
            this.style.boxShadow = '';
        });
    });
    
    // Efectos hover para las tarjetas de estadísticas
    const statCards = document.querySelectorAll('.stat-card');
    statCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-3px)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });
    
    // Efectos para items de actividad
    const activityItems = document.querySelectorAll('.activity-item');
    activityItems.forEach(item => {
        item.addEventListener('mouseenter', function() {
            this.style.transform = 'translateX(8px)';
            this.style.boxShadow = '0 4px 12px rgba(0,0,0,0.1)';
        });
        
        item.addEventListener('mouseleave', function() {
            this.style.transform = 'translateX(0)';
            this.style.boxShadow = '';
        });
    });
}

/**
 * Inicializa animaciones del dashboard
 */
function inicializarAnimaciones() {
    // Animación de aparición para las tarjetas de estadísticas
    const statCards = document.querySelectorAll('.stat-card');
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
                
                // Animar contadores si existen
                const statNumber = entry.target.querySelector('.stat-number');
                if (statNumber) {
                    animarContadorEstudiante(statNumber);
                }
            }
        });
    }, { threshold: 0.1 });
    
    statCards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        card.style.transition = `all 0.6s ease ${index * 0.1}s`;
        observer.observe(card);
    });
    
    // Animación para las tarjetas de menú
    const menuCards = document.querySelectorAll('.menu-card');
    menuCards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(30px)';
        card.style.transition = `all 0.5s ease ${(index * 0.1) + 0.3}s`;
        
        setTimeout(() => {
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, 100 + (index * 100));
    });
}

/**
 * Anima los contadores de las estadísticas
 */
function animarContadorEstudiante(elemento) {
    const texto = elemento.textContent;
    const valorFinal = parseInt(texto.replace('#', '').replace('%', ''));
    
    if (isNaN(valorFinal)) return;
    
    let valorInicial = 0;
    const duracion = 1500;
    const incremento = valorFinal / (duracion / 16);
    let current = valorInicial;
    
    const timer = setInterval(() => {
        current += incremento;
        if (current >= valorFinal) {
            current = valorFinal;
            clearInterval(timer);
        }
        
        if (texto.includes('#')) {
            elemento.textContent = '#' + Math.round(current);
        } else if (texto.includes('%')) {
            elemento.textContent = Math.round(current) + '%';
        } else {
            elemento.textContent = Math.round(current);
        }
    }, 16);
}

/**
 * Configura la confirmación para cerrar sesión
 */
function configurarCerrarSesion() {
    const logoutLink = document.querySelector('a[href="/logout"]');
    if (logoutLink) {
        logoutLink.addEventListener('click', function(e) {
            e.preventDefault();
            mostrarConfirmacionCerrarSesion();
        });
    }
}

/**
 * Muestra confirmación para cerrar sesión
 */
function mostrarConfirmacionCerrarSesion() {
    Swal.fire({
        title: '¿Cerrar Sesión?',
        text: '¿Estás seguro de que deseas salir del sistema?',
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Sí, Cerrar Sesión',
        cancelButtonText: 'Cancelar',
        background: '#1a1a1a',
        color: 'white'
    }).then((result) => {
        if (result.isConfirmed) {
            // Mostrar mensaje de despedida
            Swal.fire({
                title: '¡Hasta Pronto!',
                text: 'Cerrando sesión...',
                icon: 'success',
                timer: 1500,
                showConfirmButton: false,
                background: '#1a1a1a',
                color: 'white'
            }).then(() => {
                window.location.href = '/logout';
            });
        }
    });
}

/**
 * Muestra notificación al estudiante
 */
function mostrarNotificacionEstudiante(mensaje, tipo = 'info') {
    const config = {
        'success': { icon: 'success', title: '¡Éxito!' },
        'error': { icon: 'error', title: 'Error' },
        'warning': { icon: 'warning', title: 'Advertencia' },
        'info': { icon: 'info', title: 'Información' }
    }[tipo] || { icon: 'info', title: 'Información' };
    
    Swal.fire({
        title: config.title,
        text: mensaje,
        icon: config.icon,
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: 4000,
        timerProgressBar: true,
        background: '#1a1a1a',
        color: 'white'
    });
}

/**
 * Actualiza el tiempo de la última actividad
 */
function actualizarTiempoActividad() {
    const tiempoElementos = document.querySelectorAll('.activity-time');
    const ahora = new Date();
    
    tiempoElementos.forEach(elemento => {
        const texto = elemento.textContent;
        // Aquí puedes implementar lógica para mostrar "hace X tiempo"
        console.log('Actualizando tiempo de actividad:', texto);
    });
}

/**
 * Función para refrescar datos del dashboard
 */
function refrescarDashboard() {
    console.log('Refrescando datos del dashboard...');
    // Aquí puedes agregar lógica para actualizar datos via AJAX
    mostrarNotificacionEstudiante('Datos actualizados', 'success');
}

// Hacer funciones disponibles globalmente
window.mostrarNotificacionEstudiante = mostrarNotificacionEstudiante;
window.refrescarDashboard = refrescarDashboard;

// Inicializar cada 30 segundos (opcional)
// setInterval(actualizarTiempoActividad, 30000);