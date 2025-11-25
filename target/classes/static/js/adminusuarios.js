/**
 * adminusuarios.js
 * Funcionalidad para la gestión de usuarios en el panel de administración
 */

const adminUsuariosApp = {
    
    init: function() {
        console.log('👥 Inicializando gestión de usuarios...');
        this.configurarEventListeners();
    },
    
    configurarEventListeners: function() {
        // Event listener para botones de eliminar (solo los que no están disabled)
        document.querySelectorAll('.btn-delete:not(.disabled)').forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                adminUsuariosApp.confirmarEliminacion(this);
            });
        });
    },
    
    confirmarEliminacion: function(button) {
        const username = button.getAttribute('data-username');
        const nombre = button.getAttribute('data-nombre');
        const rol = button.getAttribute('data-rol');
        
        Swal.fire({
            title: '¿Eliminar Usuario?',
            html: `¿Estás seguro de que deseas eliminar al usuario <strong>${nombre}</strong>?<br><br>
                  <small>Esta acción no se puede deshacer.</small>`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            cancelButtonColor: '#3085d6',
            confirmButtonText: 'Sí, eliminar',
            cancelButtonText: 'Cancelar'
        }).then((result) => {
            if (result.isConfirmed) {
                this.eliminarUsuario(username);
            }
        });
    },
    
    eliminarUsuario: function(username) {
        // Mostrar loading
        Swal.fire({
            title: 'Eliminando usuario...',
            text: 'Por favor espere',
            icon: 'info',
            showConfirmButton: false,
            allowOutsideClick: false,
            didOpen: () => {
                Swal.showLoading();
            }
        });
        
        // Enviar solicitud de eliminación
        fetch(`/admin/usuarios/${username}/eliminar`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            }
        })
        .then(response => {
            if (response.redirected) {
                window.location.href = response.url;
            }
        })
        .catch(error => {
            console.error('Error:', error);
            Swal.fire({
                title: 'Error',
                text: 'Ocurrió un error al eliminar el usuario',
                icon: 'error',
                confirmButtonText: 'Entendido'
            });
        });
    },

    activarUsuario: function(button) {
        const username = button.getAttribute('data-username');
        Swal.fire({
            title: '¿Activar usuario?',
            text: 'El usuario podrá acceder al sistema nuevamente',
            icon: 'question',
            showCancelButton: true,
            confirmButtonText: 'Sí, activar',
            cancelButtonText: 'Cancelar'
        }).then((result) => {
            if (result.isConfirmed) {
                this.enviarFormulario(`/admin/usuarios/${username}/activar`);
            }
        });
    },

    desactivarUsuario: function(button) {
        const username = button.getAttribute('data-username');
        Swal.fire({
            title: '¿Desactivar usuario?',
            text: 'El usuario no podrá acceder al sistema',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Sí, desactivar',
            cancelButtonText: 'Cancelar'
        }).then((result) => {
            if (result.isConfirmed) {
                this.enviarFormulario(`/admin/usuarios/${username}/desactivar`);
            }
        });
    },

    enviarFormulario: function(url) {
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = url;
        document.body.appendChild(form);
        form.submit();
    }
};

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    adminUsuariosApp.init();
});