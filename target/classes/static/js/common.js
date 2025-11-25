// js/common.js - VERSIÓN MEJORADA Y ROBUSTA

// Confirmación de cierre de sesión
function confirmarCerrarSesion(event) {
    event.preventDefault();
    
    // Verificar si SweetAlert2 está disponible
    if (typeof Swal === 'undefined') {
        // Si no está SweetAlert2, hacer logout directamente
        console.warn('SweetAlert2 no cargado, procediendo con logout');
        window.location.href = '/logout';
        return;
    }
    
    Swal.fire({
        title: '¿Estás seguro?',
        text: "¿Deseas cerrar tu sesión?",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#bed52f',
        cancelButtonColor: '#29313c',
        confirmButtonText: 'Sí, cerrar sesión',
        cancelButtonText: 'Cancelar',
        background: '#ffffff',
        color: '#29313c'
    }).then((result) => {
        if (result.isConfirmed) {
            window.location.href = '/logout';
        }
    });
}

// Función para marcar el enlace activo basado en la URL actual
function marcarEnlaceActivo() {
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.sidebar-nav .nav-link');
    
    navLinks.forEach(link => {
        // Remover active de todos primero
        link.classList.remove('active');
        
        // Agregar active al que coincida
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('active');
        }
    });
}

// Animación de números en las estadísticas
function animarEstadisticas() {
    const statNumbers = document.querySelectorAll('.stat-number');
    
    statNumbers.forEach((stat) => {
        const text = stat.textContent || '';
        const valorFinal = parseInt(text.replace(/[^0-9]/g, '')) || 0;
        
        if (valorFinal > 0) {
            animateValue(stat, 0, valorFinal, 1500);
        }
    });
    
    function animateValue(element, start, end, duration) {
        let startTimestamp = null;
        const step = (timestamp) => {
            if (!startTimestamp) startTimestamp = timestamp;
            const progress = Math.min((timestamp - startTimestamp) / duration, 1);
            
            const currentValue = Math.floor(progress * (end - start) + start);
            
            if (element.textContent.includes('%')) {
                element.textContent = currentValue + '%';
            } else if (element.textContent.includes('$')) {
                element.textContent = '$' + currentValue.toLocaleString();
            } else {
                element.textContent = currentValue.toLocaleString();
            }
            
            if (progress < 1) {
                window.requestAnimationFrame(step);
            }
        };
        window.requestAnimationFrame(step);
    }
}

// Inicializar funciones comunes cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    console.log('Common.js cargado correctamente');
    
    // Marcar enlace activo
    marcarEnlaceActivo();
    
    // Agregar confirmación a todos los botones de cerrar sesión
    const logoutButtons = document.querySelectorAll('a[href="/logout"]');
    console.log('Botones de logout encontrados:', logoutButtons.length);
    
    logoutButtons.forEach(button => {
        button.addEventListener('click', confirmarCerrarSesion);
    });
    
    // Animación de estadísticas (solo si existen)
    if (document.querySelector('.stat-number')) {
        setTimeout(() => {
            animarEstadisticas();
        }, 500);
    }
});

// También manejar cuando la página se carga completamente
window.addEventListener('load', function() {
    console.log('Página completamente cargada');
});