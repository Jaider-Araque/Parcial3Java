// resultados.js - Funciones para la gestión de resultados
class ResultadosManager {
    constructor() {
        this.init();
    }

    init() {
        this.bindEvents();
        this.showFlashMessages();
        console.log('ResultadosManager inicializado');
    }

    bindEvents() {
        // Cerrar modal al hacer click fuera
        document.addEventListener('click', (e) => {
            const modal = document.getElementById('modalResultado');
            if (e.target === modal) {
                this.cerrarModal();
            }
        });

        // Enter en campo de búsqueda
        document.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && e.target.name === 'documento') {
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

    // Funciones para el modal de resultados
    mostrarModalResultado() {
        console.log('Mostrando modal para nuevo resultado');
        
        // Crear contenido del modal dinámicamente
        const modalContent = `
            <div class="modal-content">
                <div class="modal-header">
                    <h3 id="modalTituloResultado">Registrar Nuevo Resultado</h3>
                    <button class="modal-close" onclick="cerrarModalResultado()">&times;</button>
                </div>
                <form id="formResultado" th:action="@{/coordinacion/resultados/guardar}" method="post" onsubmit="return enviarFormularioResultado(event)">
                    <div class="modal-body">
                        <input type="hidden" id="resultadoId" name="id">
                        
                        <div class="form-grid">
                            <div class="form-group">
                                <label class="form-label">Estudiante *</label>
                                <select class="form-control" id="estudianteSelect" name="estudiante.numeroDocumento" required>
                                    <option value="">Seleccione un estudiante</option>
                                    <option th:each="estudiante : ${estudiantes}" 
                                            th:value="${estudiante.numeroDocumento}"
                                            th:text="${estudiante.nombres + ' ' + estudiante.apellidos + ' - ' + estudiante.numeroDocumento}">
                                    </option>
                                </select>
                            </div>
                            
                            <div class="form-group">
                                <label class="form-label">Tipo de Prueba *</label>
                                <select class="form-control" id="tipoPruebaSelect" name="tipoPrueba" required>
                                    <option value="">Seleccione tipo</option>
                                    <option value="SABER_PRO">Saber PRO</option>
                                    <option value="SABER_TYT">Saber T&T</option>
                                </select>
                            </div>
                            
                            <div class="form-group">
                                <label class="form-label">Puntaje Global *</label>
                                <input type="number" class="form-control" id="puntajeGlobal" name="puntajeGlobal" 
                                       required min="0" max="300" placeholder="0-300"
                                       oninput="validarPuntaje(this)">
                                <small class="form-text text-muted">Puntaje entre 0 y 300 puntos</small>
                            </div>
                            
                            <div class="form-group">
                                <label class="form-label">Año de la Prueba *</label>
                                <input type="number" class="form-control" id="anioPrueba" name="anioPrueba" 
                                       required min="2000" max="2030" 
                                       th:value="${T(java.time.LocalDateTime).now().year}">
                            </div>
                            
                            <div class="form-group">
                                <label class="form-label">Periodo *</label>
                                <select class="form-control" id="periodoSelect" name="periodo" required>
                                    <option value="">Seleccione periodo</option>
                                    <option value="1">1</option>
                                    <option value="2">2</option>
                                </select>
                            </div>
                            
                            <div class="form-group">
                                <label class="form-label">Observaciones</label>
                                <textarea class="form-control" id="observaciones" name="observaciones" 
                                          rows="3" placeholder="Observaciones adicionales..."></textarea>
                            </div>
                        </div>
                        
                        <!-- Sección de Competencias -->
                        <div class="form-section">
                            <h4 class="section-title">Competencias</h4>
                            <div class="competencias-dinamicas" id="competenciasContainer">
                                <div class="competencia-input-group">
                                    <div class="d-flex gap-2 align-end">
                                        <div class="form-group flex-1">
                                            <label class="form-label">Competencia</label>
                                            <input type="text" class="form-control competencia-nombre" 
                                                   placeholder="Nombre de la competencia" name="competencias[]">
                                        </div>
                                        <div class="form-group">
                                            <label class="form-label">Puntaje</label>
                                            <input type="number" class="form-control competencia-puntaje" 
                                                   min="0" max="300" placeholder="0-300" name="puntajes[]">
                                        </div>
                                        <button type="button" class="btn btn-outline btn-sm" data-action="add" title="Agregar competencia">
                                            <i class="fas fa-plus"></i>
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline" onclick="cerrarModalResultado()">Cancelar</button>
                        <button type="submit" class="btn btn-primary" id="btnGuardarResultado">
                            <i class="fas fa-save"></i> Guardar Resultado
                        </button>
                    </div>
                </form>
            </div>
        `;
        
        document.getElementById('modalResultado').innerHTML = modalContent;
        document.getElementById('modalResultado').style.display = 'block';
        
        // Enfocar el primer campo
        setTimeout(() => {
            const estudianteSelect = document.getElementById('estudianteSelect');
            if (estudianteSelect) estudianteSelect.focus();
            this.manejarEventosCompetencias();
        }, 100);
    }

    async mostrarModalEditarResultado(id) {
        try {
            console.log('Cargando resultado para editar:', id);
            
            const response = await fetch('/coordinacion/resultados/editar/' + id);
            
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error('Error ' + response.status + ': ' + errorText);
            }
            
            const resultado = await response.json();
            console.log('Resultado cargado exitosamente:', resultado);

            this.cargarDatosEnModal(resultado);
            
        } catch (error) {
            console.error('Error al cargar resultado:', error);
            Swal.fire({
                title: 'Error!',
                text: 'No se pudieron cargar los datos del resultado: ' + error.message,
                icon: 'error',
                confirmButtonText: 'Entendido'
            });
        }
    }

    cargarDatosEnModal(resultado) {
        console.log('Cargando datos en modal:', resultado);
        
        // Crear contenido del modal para edición
        const modalContent = `
            <div class="modal-content">
                <div class="modal-header">
                    <h3 id="modalTituloResultado">Editar Resultado</h3>
                    <button class="modal-close" onclick="cerrarModalResultado()">&times;</button>
                </div>
                <form id="formResultado" th:action="@{/coordinacion/resultados/actualizar}" method="post" onsubmit="return enviarFormularioResultado(event)">
                    <div class="modal-body">
                        <input type="hidden" id="resultadoId" name="id" value="${resultado.id}">
                        
                        <div class="form-grid">
                            <div class="form-group">
                                <label class="form-label">Estudiante</label>
                                <input type="text" class="form-control" value="${resultado.estudiante.nombres} ${resultado.estudiante.apellidos} - ${resultado.estudiante.numeroDocumento}" readonly>
                                <input type="hidden" name="estudiante.numeroDocumento" value="${resultado.estudiante.numeroDocumento}">
                            </div>
                            
                            <div class="form-group">
                                <label class="form-label">Tipo de Prueba *</label>
                                <select class="form-control" id="tipoPruebaSelect" name="tipoPrueba" required>
                                    <option value="SABER_PRO" ${resultado.tipoPrueba === 'SABER_PRO' ? 'selected' : ''}>Saber PRO</option>
                                    <option value="SABER_TYT" ${resultado.tipoPrueba === 'SABER_TYT' ? 'selected' : ''}>Saber T&T</option>
                                </select>
                            </div>
                            
                            <div class="form-group">
                                <label class="form-label">Puntaje Global *</label>
                                <input type="number" class="form-control" id="puntajeGlobal" name="puntajeGlobal" 
                                       value="${resultado.puntajeGlobal}" required min="0" max="300"
                                       oninput="validarPuntaje(this)">
                                <small class="form-text text-muted">Puntaje entre 0 y 300 puntos</small>
                            </div>
                            
                            <div class="form-group">
                                <label class="form-label">Año de la Prueba *</label>
                                <input type="number" class="form-control" id="anioPrueba" name="anioPrueba" 
                                       value="${resultado.anioPrueba}" required min="2000" max="2030">
                            </div>
                            
                            <div class="form-group">
                                <label class="form-label">Periodo *</label>
                                <select class="form-control" id="periodoSelect" name="periodo" required>
                                    <option value="1" ${resultado.periodo === 1 ? 'selected' : ''}>1</option>
                                    <option value="2" ${resultado.periodo === 2 ? 'selected' : ''}>2</option>
                                </select>
                            </div>
                            
                            <div class="form-group">
                                <label class="form-label">Observaciones</label>
                                <textarea class="form-control" id="observaciones" name="observaciones" 
                                          rows="3">${resultado.observaciones || ''}</textarea>
                            </div>
                        </div>
                        
                        <!-- Sección de Competencias -->
                        <div class="form-section">
                            <h4 class="section-title">Competencias</h4>
                            <div class="competencias-dinamicas" id="competenciasContainer">
                                ${this.generarCamposCompetencias(resultado.competencias)}
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline" onclick="cerrarModalResultado()">Cancelar</button>
                        <button type="submit" class="btn btn-primary" id="btnGuardarResultado">
                            <i class="fas fa-save"></i> Actualizar Resultado
                        </button>
                    </div>
                </form>
            </div>
        `;
        
        document.getElementById('modalResultado').innerHTML = modalContent;
        document.getElementById('modalResultado').style.display = 'block';
        
        setTimeout(() => {
            this.manejarEventosCompetencias();
        }, 100);
        
        console.log('Modal de edición mostrado correctamente');
    }

    generarCamposCompetencias(competencias) {
        if (!competencias || Object.keys(competencias).length === 0) {
            return `
                <div class="competencia-input-group">
                    <div class="d-flex gap-2 align-end">
                        <div class="form-group flex-1">
                            <label class="form-label">Competencia</label>
                            <input type="text" class="form-control competencia-nombre" 
                                   placeholder="Nombre de la competencia" name="competencias[]">
                        </div>
                        <div class="form-group">
                            <label class="form-label">Puntaje</label>
                            <input type="number" class="form-control competencia-puntaje" 
                                   min="0" max="300" placeholder="0-300" name="puntajes[]">
                        </div>
                        <button type="button" class="btn btn-outline btn-sm" data-action="add" title="Agregar competencia">
                            <i class="fas fa-plus"></i>
                        </button>
                    </div>
                </div>
            `;
        }

        let html = '';
        let index = 0;
        
        for (const [competencia, puntaje] of Object.entries(competencias)) {
            const esPrimero = index === 0;
            const dataAction = esPrimero ? 'add' : 'delete';
            const icono = esPrimero ? 'fa-plus' : 'fa-trash';
            const claseBoton = esPrimero ? 'btn-outline' : 'btn-danger';
            const titulo = esPrimero ? 'Agregar competencia' : 'Eliminar competencia';
            
            html += `
                <div class="competencia-input-group">
                    <div class="d-flex gap-2 align-end">
                        <div class="form-group flex-1">
                            <label class="form-label">${esPrimero ? 'Competencia' : ''}</label>
                            <input type="text" class="form-control competencia-nombre" 
                                   value="${competencia}" name="competencias[]" placeholder="Nombre de la competencia">
                        </div>
                        <div class="form-group">
                            <label class="form-label">${esPrimero ? 'Puntaje' : ''}</label>
                            <input type="number" class="form-control competencia-puntaje" 
                                   value="${puntaje}" min="0" max="300" placeholder="0-300" name="puntajes[]">
                        </div>
                        <button type="button" class="btn btn-sm ${claseBoton}" 
                                data-action="${dataAction}" 
                                title="${titulo}">
                            <i class="fas ${icono}"></i>
                        </button>
                    </div>
                </div>
            `;
            index++;
        }
        
        return html;
    }

    manejarEventosCompetencias() {
        const container = document.getElementById('competenciasContainer');
        if (!container) {
            console.warn('No se encontró el contenedor de competencias');
            return;
        }
        
        container.addEventListener('click', (e) => {
            if (e.target.closest('button[data-action]')) {
                const boton = e.target.closest('button[data-action]');
                const accion = boton.getAttribute('data-action');
                
                if (accion === 'add') {
                    this.agregarCompetencia();
                } else if (accion === 'delete') {
                    this.eliminarCompetencia(boton);
                }
            }
        });
    }

    agregarCompetencia() {
        const container = document.getElementById('competenciasContainer');
        if (!container) {
            console.error('No se puede agregar competencia: contenedor no encontrado');
            return;
        }
        
        const nuevoGrupo = document.createElement('div');
        nuevoGrupo.className = 'competencia-input-group mt-2';
        nuevoGrupo.innerHTML = `
            <div class="d-flex gap-2 align-end">
                <div class="form-group flex-1">
                    <input type="text" class="form-control competencia-nombre" 
                           placeholder="Nombre de la competencia" name="competencias[]">
                </div>
                <div class="form-group">
                    <input type="number" class="form-control competencia-puntaje" 
                           min="0" max="300" placeholder="0-300" name="puntajes[]">
                </div>
                <button type="button" class="btn btn-outline btn-sm btn-danger" data-action="delete" title="Eliminar competencia">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        `;
        container.appendChild(nuevoGrupo);
    }

    eliminarCompetencia(boton) {
        const grupo = boton.closest('.competencia-input-group');
        if (grupo) {
            grupo.remove();
        }
    }

    cerrarModal() {
        console.log('Cerrando modal de resultados');
        const modal = document.getElementById('modalResultado');
        if (modal) {
            modal.style.display = 'none';
            modal.innerHTML = '';
        }
    }

    // Función para enviar el formulario con AJAX
    async enviarFormularioResultado(event) {
        event.preventDefault();
        console.log('Enviando formulario de resultado...');
        
        const form = document.getElementById('formResultado');
        if (!form) {
            console.error('No se encontró el formulario');
            return false;
        }
        
        const formData = new FormData(form);
        const btnGuardar = document.getElementById('btnGuardarResultado');
        
        // Procesar competencias dinámicas
        this.procesarCompetencias(formData);
        
        // Validar formulario
        if (!this.validarFormularioResultado()) {
            return false;
        }
        
        // Deshabilitar botón para evitar múltiples envíos
        if (btnGuardar) {
            btnGuardar.disabled = true;
            btnGuardar.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Guardando...';
        }
        
        try {
            const response = await fetch(form.action, {
                method: 'POST',
                body: formData
            });
            
            if (response.ok) {
                console.log('Resultado guardado exitosamente');
                // Cerrar modal
                this.cerrarModal();
                
                // Mostrar mensaje de éxito
                await Swal.fire({
                    title: '¡Éxito!',
                    text: 'Resultado guardado correctamente',
                    icon: 'success',
                    confirmButtonText: 'Aceptar'
                });
                
                // Recargar la página para ver los cambios
                window.location.reload();
                
            } else {
                const errorText = await response.text();
                throw new Error(errorText || 'Error en el servidor');
            }
        } catch (error) {
            console.error('Error al guardar:', error);
            await Swal.fire({
                title: 'Error!',
                text: 'No se pudo guardar el resultado: ' + error.message,
                icon: 'error',
                confirmButtonText: 'Entendido'
            });
        } finally {
            // Rehabilitar botón
            if (btnGuardar) {
                btnGuardar.disabled = false;
                btnGuardar.innerHTML = '<i class="fas fa-save"></i> Guardar Resultado';
            }
        }
        
        return false;
    }

    procesarCompetencias(formData) {
        // Eliminar campos de competencias existentes
        formData.delete('competencias');
        formData.delete('puntajes');
        
        // Procesar competencias dinámicas
        const competenciasNombres = document.querySelectorAll('.competencia-nombre');
        const competenciasPuntajes = document.querySelectorAll('.competencia-puntaje');
        
        const competenciasMap = {};
        
        for (let i = 0; i < competenciasNombres.length; i++) {
            const nombre = competenciasNombres[i].value.trim();
            const puntaje = competenciasPuntajes[i].value;
            
            if (nombre && puntaje) {
                competenciasMap[nombre] = parseInt(puntaje);
            }
        }
        
        // Agregar competencias como JSON
        if (Object.keys(competenciasMap).length > 0) {
            formData.append('competencias', JSON.stringify(competenciasMap));
        }
    }

    validarFormularioResultado() {
        const estudiante = document.getElementById('estudianteSelect');
        const tipoPrueba = document.getElementById('tipoPruebaSelect');
        const puntajeGlobal = document.getElementById('puntajeGlobal');
        const anioPrueba = document.getElementById('anioPrueba');
        const periodo = document.getElementById('periodoSelect');

        if (!estudiante || !estudiante.value || !tipoPrueba || !tipoPrueba.value || 
            !puntajeGlobal || !puntajeGlobal.value || !anioPrueba || !anioPrueba.value || 
            !periodo || !periodo.value) {
            Swal.fire('Error!', 'Por favor complete todos los campos obligatorios (*)', 'error');
            return false;
        }

        // Validar puntaje global
        const puntaje = parseInt(puntajeGlobal.value);
        if (puntaje < 0 || puntaje > 300) {
            Swal.fire('Error!', 'El puntaje global debe estar entre 0 y 300 puntos', 'error');
            return false;
        }

        // Validar año
        const anio = parseInt(anioPrueba.value);
        const anioActual = new Date().getFullYear();
        if (anio < 2000 || anio > anioActual + 1) {
            Swal.fire('Error!', 'El año debe estar entre 2000 y ' + (anioActual + 1), 'error');
            return false;
        }

        return true;
    }

    // Funciones de CRUD
    eliminarResultado(id, nombreEstudiante) {
        Swal.fire({
            title: '¿Estás seguro?',
            html: 'Esta acción eliminará el resultado del estudiante: <strong>' + nombreEstudiante + '</strong>',
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
                    const response = await fetch('/coordinacion/resultados/eliminar/' + id, {
                        method: 'DELETE'
                    });
                    
                    if (response.ok) {
                        const result = await response.text();
                        if (result === 'OK') {
                            Swal.fire('Eliminado!', 'El resultado ha sido eliminado.', 'success');
                            setTimeout(() => location.reload(), 1500);
                        } else {
                            throw new Error(result);
                        }
                    } else {
                        throw new Error('Error en la respuesta del servidor');
                    }
                } catch (error) {
                    Swal.fire('Error!', 'No se pudo eliminar el resultado: ' + error.message, 'error');
                }
            }
        });
    }

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

// Funciones globales para manejo de competencias dinámicas
function agregarCompetencia() {
    if (window.resultadosManager) {
        window.resultadosManager.agregarCompetencia();
    } else {
        console.error('ResultadosManager no está inicializado');
    }
}

function eliminarCompetencia(boton) {
    if (window.resultadosManager) {
        window.resultadosManager.eliminarCompetencia(boton);
    } else {
        console.error('ResultadosManager no está inicializado');
    }
}

function validarPuntaje(input) {
    const valor = parseInt(input.value);
    if (valor < 0) input.value = 0;
    if (valor > 300) input.value = 300;
}

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    window.resultadosManager = new ResultadosManager();
    console.log('DOM cargado, ResultadosManager listo');
});

// Funciones globales para los onclick del HTML
function mostrarModalResultado() {
    if (window.resultadosManager) {
        window.resultadosManager.mostrarModalResultado();
    } else {
        console.error('ResultadosManager no está inicializado');
    }
}

function editarResultado(id) {
    if (window.resultadosManager) {
        window.resultadosManager.mostrarModalEditarResultado(id);
    } else {
        console.error('ResultadosManager no está inicializado');
    }
}

function cerrarModalResultado() {
    if (window.resultadosManager) {
        window.resultadosManager.cerrarModal();
    } else {
        console.error('ResultadosManager no está inicializado');
    }
}

function eliminarResultado(id, nombreEstudiante) {
    if (window.resultadosManager) {
        window.resultadosManager.eliminarResultado(id, nombreEstudiante);
    } else {
        console.error('ResultadosManager no está inicializado');
    }
}

function cambiarPagina(pagina) {
    if (window.resultadosManager) {
        window.resultadosManager.cambiarPagina(pagina);
    } else {
        console.error('ResultadosManager no está inicializado');
    }
}

function buscar() {
    if (window.resultadosManager) {
        window.resultadosManager.buscar();
    } else {
        console.error('ResultadosManager no está inicializado');
    }
}

function enviarFormularioResultado(event) {
    if (window.resultadosManager) {
        return window.resultadosManager.enviarFormularioResultado(event);
    } else {
        console.error('ResultadosManager no está inicializado');
        return false;
    }
}