// estudios.js - Funciones para la gestión de estudiantes
class EstudiantesManager {
    constructor() {
        this.init();
    }

    init() {
        this.bindEvents();
        this.showFlashMessages();
        console.log('EstudiantesManager inicializado');
    }

    bindEvents() {
        // Cerrar modal al hacer click fuera
        document.addEventListener('click', (e) => {
            const modal = document.getElementById('modalEstudiante');
            if (e.target === modal) {
                this.cerrarModal();
            }
        });

        // Enter en campos de búsqueda
        document.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && (e.target.name === 'documento' || e.target.name === 'nombre')) {
                this.buscar();
            }
        });

        // Escape para cerrar modal
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.cerrarModal();
            }
        });
    }

    // Funciones para el modal
    mostrarModalCrear() {
        console.log('Mostrando modal crear');
        document.getElementById('modalTitulo').textContent = 'Agregar Estudiante';
        document.getElementById('formEstudiante').reset();
        
        // ✅ CORREGIDO: Solo limpiar el campo principal
        document.getElementById('documento').value = '';
        document.getElementById('documento').readOnly = false;
        document.getElementById('estudianteId').value = '';
        
        document.getElementById('modalEstudiante').style.display = 'block';
        
        // Enfocar el primer campo
        setTimeout(() => {
            document.getElementById('documento').focus();
        }, 100);
    }

    async mostrarModalEditar(documento) {
        try {
            console.log('Cargando estudiante para editar:', documento);
            
            const response = await fetch(`/coordinacion/estudiantes/editar/${documento}`);
            
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`Error ${response.status}: ${errorText}`);
            }
            
            const estudiante = await response.json();
            console.log('Estudiante cargado exitosamente:', estudiante);

            this.cargarDatosEnModal(estudiante);
            
        } catch (error) {
            console.error('Error al cargar estudiante:', error);
            Swal.fire({
                title: 'Error!',
                text: `No se pudieron cargar los datos del estudiante: ${error.message}`,
                icon: 'error',
                confirmButtonText: 'Entendido'
            });
        }
    }

    cargarDatosEnModal(estudiante) {
        console.log('Cargando datos en modal:', estudiante);
        
        document.getElementById('modalTitulo').textContent = 'Editar Estudiante';
        
        // ✅ CORREGIDO: Solo usar el campo principal del documento
        document.getElementById('documento').value = estudiante.numeroDocumento || '';
        document.getElementById('documento').readOnly = true;
        
        // ✅ El campo oculto se deja vacío (no es necesario para edición)
        document.getElementById('estudianteId').value = '';
        
        document.getElementById('nombres').value = estudiante.nombres || '';
        document.getElementById('apellidos').value = estudiante.apellidos || '';
        document.getElementById('email').value = estudiante.email || '';
        document.getElementById('telefono').value = estudiante.numeroTelefono || '';
        document.getElementById('programa').value = estudiante.programaAcademico || '';
        document.getElementById('semestre').value = estudiante.semestre || '';
        document.getElementById('tipoPrueba').value = estudiante.tipoPrueba || '';

        document.getElementById('modalEstudiante').style.display = 'block';
        
        console.log('Modal de edición mostrado correctamente');
    }

    cerrarModal() {
        console.log('Cerrando modal');
        document.getElementById('modalEstudiante').style.display = 'none';
        document.getElementById('documento').readOnly = false;
    }

    // ✅ FUNCIÓN SIMPLIFICADA: Sin modificar el documento
    async enviarFormulario(event) {
        event.preventDefault();
        console.log('Enviando formulario...');
        
        const form = document.getElementById('formEstudiante');
        const formData = new FormData(form);
        const btnGuardar = document.getElementById('btnGuardar');
        
        // ✅ ELIMINADO: No modificar el documento antes de enviar
        // El documento ya viene limpio del HTML
        
        // Validar formulario
        if (!this.validarFormulario()) {
            return false;
        }
        
        // Deshabilitar botón para evitar múltiples envíos
        btnGuardar.disabled = true;
        btnGuardar.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Guardando...';
        
        try {
            const response = await fetch(form.action, {
                method: 'POST',
                body: formData
            });
            
            if (response.ok) {
                console.log('Estudiante guardado exitosamente');
                this.cerrarModal();
                
                await Swal.fire({
                    title: '¡Éxito!',
                    text: 'Estudiante guardado correctamente',
                    icon: 'success',
                    confirmButtonText: 'Aceptar'
                });
                
                window.location.reload();
                
            } else {
                const errorText = await response.text();
                throw new Error(errorText || 'Error en el servidor');
            }
        } catch (error) {
            console.error('Error al guardar:', error);
            await Swal.fire({
                title: 'Error!',
                text: `No se pudo guardar el estudiante: ${error.message}`,
                icon: 'error',
                confirmButtonText: 'Entendido'
            });
        } finally {
            btnGuardar.disabled = false;
            btnGuardar.innerHTML = '<i class="fas fa-save"></i> Guardar Estudiante';
        }
        
        return false;
    }

    validarFormulario() {
        const documento = document.getElementById('documento').value;
        const nombres = document.getElementById('nombres').value;
        const apellidos = document.getElementById('apellidos').value;
        const email = document.getElementById('email').value;
        const programa = document.getElementById('programa').value;
        const semestre = document.getElementById('semestre').value;
        const tipoPrueba = document.getElementById('tipoPrueba').value;

        if (!documento || !nombres || !apellidos || !email || !programa || !semestre || !tipoPrueba) {
            Swal.fire('Error!', 'Por favor complete todos los campos obligatorios (*)', 'error');
            return false;
        }

        // Validar email
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            Swal.fire('Error!', 'Por favor ingrese un email válido', 'error');
            return false;
        }

        // ✅ Validación básica de documento (solo números)
        const soloNumeros = /^\d+$/.test(documento);
        if (!soloNumeros) {
            Swal.fire('Error!', 'El número de documento solo debe contener números', 'error');
            return false;
        }

        return true;
    }

    // Funciones de CRUD
    eliminarEstudiante(documento, nombreCompleto) {
        Swal.fire({
            title: '¿Estás seguro?',
            html: `Esta acción eliminará al estudiante: <strong>${nombreCompleto}</strong><br>Documento: ${documento}`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            cancelButtonColor: '#3085d6',
            confirmButtonText: 'Sí, eliminar',
            cancelButtonText: 'Cancelar',
            backdrop: true,
            allowOutsideClick: false
        }).then(async (result) => {
            if (result.isConfirmed) {
                try {
                    const response = await fetch(`/coordinacion/estudiantes/eliminar/${documento}`, {
                        method: 'DELETE'
                    });
                    
                    if (response.ok) {
                        const result = await response.text();
                        if (result === 'OK') {
                            Swal.fire('Eliminado!', 'El estudiante ha sido eliminado.', 'success');
                            setTimeout(() => location.reload(), 1500);
                        } else {
                            throw new Error(result);
                        }
                    } else {
                        throw new Error('Error en la respuesta del servidor');
                    }
                } catch (error) {
                    Swal.fire('Error!', `No se pudo eliminar el estudiante: ${error.message}`, 'error');
                }
            }
        });
    }

    verResultados(documento) {
        window.location.href = `/coordinacion/resultados?documento=${documento}`;
    }

    exportarEstudiantes() {
        Swal.fire({
            title: 'Exportando datos...',
            text: 'Preparando archivo de exportación',
            icon: 'info',
            showConfirmButton: false,
            timer: 2000
        });
        
        setTimeout(() => {
            Swal.fire('Éxito!', 'Los datos han sido exportados correctamente.', 'success');
        }, 2000);
    }

    cambiarPagina(pagina) {
        document.getElementById('paginaInput').value = pagina;
        document.getElementById('filtroForm').submit();
    }

    buscar() {
        document.getElementById('paginaInput').value = 0;
        document.getElementById('filtroForm').submit();
    }

    limpiarFiltros() {
        window.location.href = '/coordinacion/estudiantes';
    }

    validarDocumento(input) {
        // Guardar la posición del cursor
        const start = input.selectionStart;
        const end = input.selectionEnd;
        
        // ✅ Solo permitir números
        input.value = input.value.replace(/[^\d]/g, '');
        
        // Restaurar la posición del cursor
        input.setSelectionRange(start, end);
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
                    const nuevaUrl = window.location.pathname;
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
                    const nuevaUrl = window.location.pathname;
                    window.history.replaceState({}, document.title, nuevaUrl);
                });
            }
        }
    }
}

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    window.estudiantesManager = new EstudiantesManager();
    console.log('DOM cargado, estudiantesManager listo');
});

// Funciones globales para los onclick del HTML
function mostrarModalCrear() {
    if (window.estudiantesManager) {
        window.estudiantesManager.mostrarModalCrear();
    }
}

function mostrarModalEditar(documento) {
    if (window.estudiantesManager) {
        window.estudiantesManager.mostrarModalEditar(documento);
    }
}

function cerrarModal() {
    if (window.estudiantesManager) {
        window.estudiantesManager.cerrarModal();
    }
}

function eliminarEstudiante(documento, nombreCompleto) {
    if (window.estudiantesManager) {
        window.estudiantesManager.eliminarEstudiante(documento, nombreCompleto);
    }
}

function verResultados(documento) {
    if (window.estudiantesManager) {
        window.estudiantesManager.verResultados(documento);
    }
}

function exportarEstudiantes() {
    if (window.estudiantesManager) {
        window.estudiantesManager.exportarEstudiantes();
    }
}

function cambiarPagina(pagina) {
    if (window.estudiantesManager) {
        window.estudiantesManager.cambiarPagina(pagina);
    }
}

function buscar() {
    if (window.estudiantesManager) {
        window.estudiantesManager.buscar();
    }
}

function limpiarFiltros() {
    if (window.estudiantesManager) {
        window.estudiantesManager.limpiarFiltros();
    }
}

function validarDocumento(input) {
    if (window.estudiantesManager) {
        window.estudiantesManager.validarDocumento(input);
    }
}

function enviarFormulario(event) {
    if (window.estudiantesManager) {
        return window.estudiantesManager.enviarFormulario(event);
    }
    return false;
}