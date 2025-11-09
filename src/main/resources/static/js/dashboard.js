(() => {
    const estadosMesa = ['Disponible', 'Ocupada', 'Reservada'];
    const body = document.body;
    if (!body) {
        return;
    }

    const refs = {
        mesaModalEl: document.getElementById('mesaModal'),
        clienteModalEl: document.getElementById('clienteModal'),
        clientesTable: document.querySelector('#clientesTable tbody'),
        clientesEmptyRow: document.getElementById('clientesEmptyRow'),
        filterInput: document.getElementById('clienteFilter'),
        alertBox: document.getElementById('dashboardAlert'),
        mesaModalTitle: document.getElementById('mesaModalTitle'),
        mesaModalSub: document.getElementById('mesaModalSub'),
        mesaModalEstado: document.getElementById('mesaModalEstado'),
        mesaModalCapacidad: document.getElementById('mesaModalCapacidad'),
        mesaModalUbicacion: document.getElementById('mesaModalUbicacion'),
        mesaClienteInfo: document.getElementById('mesaClienteInfo'),
        mesaClienteNombre: document.getElementById('mesaClienteNombre'),
        mesaClienteDni: document.getElementById('mesaClienteDni'),
        mesaClienteContacto: document.getElementById('mesaClienteContacto'),
        mesaModalWarning: document.getElementById('mesaModalWarning'),
        mesaModalHint: document.getElementById('mesaModalHint'),
        asignarBtn: document.getElementById('asignarBtn'),
        editarBtn: document.getElementById('editarClienteBtn'),
        liberarBtn: document.getElementById('liberarBtn'),
        mesaEstadoSelect: document.getElementById('mesaEstadoSelect'),
        assignExistingWrapper: document.getElementById('assignExistingWrapper'),
        clienteExistenteSelect: document.getElementById('clienteExistenteSelect'),
        asignarExistenteBtn: document.getElementById('asignarClienteExistenteBtn'),
        sinClientesDisponibles: document.getElementById('sinClientesDisponibles'),
        clienteForm: document.getElementById('clienteForm'),
        clienteModalAlert: document.getElementById('clienteModalAlert'),
        clienteModalTitle: document.getElementById('clienteModalTitle'),
        clienteSubmitBtn: document.getElementById('clienteSubmitBtn')
    };

    const mesaModal = refs.mesaModalEl ? new bootstrap.Modal(refs.mesaModalEl) : null;
    const clienteModal = refs.clienteModalEl ? new bootstrap.Modal(refs.clienteModalEl) : null;
    const getMesaCards = () => Array.from(document.querySelectorAll('.mesa-card'));
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
    const canManage = body.dataset.staff === 'true';
    const isAdmin = body.dataset.admin === 'true';

    let mesaActual = null;

    document.addEventListener('click', handleClickGlobal);
    document.addEventListener('change', handleChangeGlobal);
    refs.filterInput?.addEventListener('input', filtrarClientes);

    if (refs.clientesTable) {
        refs.clientesTable.addEventListener('click', handleAccionesCliente);
    }

    refs.asignarBtn?.addEventListener('click', () => {
        if (!mesaActual) return;
        abrirFormularioCliente({ mesaId: mesaActual.mesa.id }, 'nuevo');
    });

    refs.editarBtn?.addEventListener('click', () => {
        if (!mesaActual || !mesaActual.cliente) return;
        abrirFormularioCliente({
            id: mesaActual.cliente.id,
            nombres: mesaActual.cliente.nombres,
            apellidos: mesaActual.cliente.apellidos,
            dni: mesaActual.cliente.dni,
            telefono: mesaActual.cliente.telefono,
            correo: mesaActual.cliente.correo,
            mesaId: mesaActual.mesa.id
        }, 'editar');
    });

    refs.liberarBtn?.addEventListener('click', liberarMesa);
    refs.mesaEstadoSelect?.addEventListener('change', manejarCambioEstadoMesaModal);
    refs.asignarExistenteBtn?.addEventListener('click', asignarClienteExistente);

    refs.clienteForm?.addEventListener('submit', async (event) => {
        event.preventDefault();
        refs.clienteModalAlert?.classList.add('d-none');
        if (!refs.clienteForm.reportValidity()) {
            return;
        }
        const payload = obtenerPayloadFormulario();
        try {
            const response = await fetch('/api/clientes', {
                method: 'POST',
                headers: buildHeaders(),
                body: JSON.stringify(payload)
            });
            const body = await safeParse(response);
            if (!response.ok) {
                mostrarErrorFormulario(body);
                manejarError(response.status, body);
                return;
            }
            clienteModal?.hide();
            mesaModal?.hide();
            mesaActual = body;
            actualizarMesaCard(body.mesa);
            upsertClientRow(body.cliente);
            actualizarEstadisticas();
            mostrarAlerta('Cliente guardado correctamente.', 'success');
        } catch (error) {
            console.error(error);
            mostrarAlerta('No se pudo guardar el cliente.', 'danger');
        }
    });

    function handleClickGlobal(event) {
        const mesaCard = event.target.closest('.mesa-card');
        if (mesaCard) {
            event.preventDefault();
            abrirMesa(mesaCard.dataset.id, mesaCard.dataset.fallback);
            return;
        }

        if (!canManage) {
            return;
        }

        const row = event.target.closest('tr[data-cliente-row]');
        if (row && !event.target.closest('[data-action]')) {
            event.preventDefault();
            abrirFormularioCliente(extraerClienteRow(row), 'editar');
        }
    }

    function handleChangeGlobal(event) {
        const select = event.target.closest('.mesa-estado-inline');
        if (select) {
            manejarCambioEstadoInline(select);
        }
    }

    function handleAccionesCliente(event) {
        const target = event.target.closest('[data-action]');
        if (!target) return;
        const row = target.closest('tr[data-cliente-row]');
        if (!row) return;
        if (target.dataset.action === 'edit') {
            abrirFormularioCliente(extraerClienteRow(row), 'editar');
        } else if (target.dataset.action === 'delete') {
            eliminarCliente(row);
        }
    }

    async function abrirMesa(id, fallbackUrl) {
        try {
            const response = await fetch(`/api/mesas/${id}`);
            const body = await safeParse(response);
            if (!response.ok) {
                manejarError(response.status, body);
                if (fallbackUrl && canManage) {
                    window.location.href = fallbackUrl;
                }
                return;
            }
            mesaActual = body;
            poblarMesaModal(body);
            mesaModal?.show();
        } catch (error) {
            console.error(error);
            if (fallbackUrl && canManage) {
                window.location.href = fallbackUrl;
                return;
            }
            mostrarAlerta('No fue posible cargar los datos de la mesa.', 'danger');
        }
    }

    function poblarMesaModal(data) {
        if (!refs.mesaModalTitle) return;
        const { mesa, cliente } = data;
        refs.mesaModalTitle.textContent = `Mesa ${mesa.numero}`;
        refs.mesaModalSub.textContent = mesa.ubicacion || 'Sin ubicación';
        refs.mesaModalEstado.textContent = mesa.estado;
        refs.mesaModalCapacidad.textContent = `${mesa.capacidad} personas`;
        refs.mesaModalUbicacion.textContent = mesa.ubicacion || 'Sin ubicación';

        if (refs.mesaEstadoSelect) {
            refs.mesaEstadoSelect.value = mesa.estado;
            refs.mesaEstadoSelect.dataset.mesaId = mesa.id;
        }

        if (cliente) {
            refs.mesaClienteInfo?.classList.remove('d-none');
            if (refs.mesaClienteNombre) refs.mesaClienteNombre.textContent = `${cliente.nombres} ${cliente.apellidos}`;
            if (refs.mesaClienteDni) refs.mesaClienteDni.textContent = cliente.dni || '-';
            if (refs.mesaClienteContacto) refs.mesaClienteContacto.textContent = cliente.telefono || cliente.correo || '-';
        } else {
            refs.mesaClienteInfo?.classList.add('d-none');
        }

        const disponible = mesa.estado.toLowerCase() === 'disponible';
        if (refs.assignExistingWrapper) {
            if (canManage && disponible) {
                refs.assignExistingWrapper.classList.remove('d-none');
                poblarClientesDisponiblesSelect();
            } else {
                refs.assignExistingWrapper.classList.add('d-none');
            }
        }

        if (disponible) {
            refs.mesaModalHint.textContent = 'Esta mesa está libre. Puedes asignarla desde aquí.';
            if (canManage) {
                refs.asignarBtn?.classList.remove('d-none');
                refs.editarBtn?.classList.add('d-none');
                refs.liberarBtn?.classList.add('d-none');
            }
        } else if (cliente) {
            refs.mesaModalHint.textContent = 'Mesa ocupada. Puedes editar o liberar la reserva.';
            if (canManage) {
                refs.asignarBtn?.classList.add('d-none');
                refs.editarBtn?.classList.remove('d-none');
                refs.liberarBtn?.classList.remove('d-none');
            }
        } else {
            refs.mesaModalHint.textContent = 'Mesa reservada. Asigna un cliente cuando llegue.';
            if (canManage) {
                refs.asignarBtn?.classList.remove('d-none');
                refs.editarBtn?.classList.add('d-none');
                refs.liberarBtn?.classList.add('d-none');
            }
        }

        if (!canManage) {
            refs.asignarBtn?.classList.add('d-none');
            refs.editarBtn?.classList.add('d-none');
            refs.liberarBtn?.classList.add('d-none');
        }

        refs.mesaModalWarning?.classList.add('d-none');
    }

    function obtenerClientesDisponibles() {
        if (!refs.clientesTable) return [];
        return Array.from(refs.clientesTable.querySelectorAll('tr[data-cliente-row]'))
            .map(extraerClienteRow)
            .filter(cliente => !cliente.mesaId);
    }

    function poblarClientesDisponiblesSelect() {
        if (!refs.clienteExistenteSelect) return;
        const disponibles = obtenerClientesDisponibles();
        refs.clienteExistenteSelect.innerHTML = '<option value="" selected disabled>Selecciona un cliente sin mesa</option>';
        if (disponibles.length === 0) {
            refs.clienteExistenteSelect.disabled = true;
            refs.sinClientesDisponibles?.classList.remove('d-none');
            return;
        }
        refs.clienteExistenteSelect.disabled = false;
        refs.sinClientesDisponibles?.classList.add('d-none');
        disponibles.forEach(cliente => {
            const option = document.createElement('option');
            option.value = cliente.id;
            option.textContent = `${cliente.nombres || 'Sin nombre'} ${cliente.apellidos || 'Sin apellido'}`.trim();
            refs.clienteExistenteSelect.append(option);
        });
    }

    async function asignarClienteExistente(event) {
        event.preventDefault();
        if (!refs.clienteExistenteSelect || !mesaActual) return;
        const clienteId = refs.clienteExistenteSelect.value;
        if (!clienteId) {
            mostrarAlerta('Selecciona un cliente disponible.', 'warning');
            return;
        }
        try {
            const response = await fetch(`/api/clientes/${clienteId}/mesa`, {
                method: 'POST',
                headers: buildHeaders(),
                body: JSON.stringify({ mesaId: mesaActual.mesa.id })
            });
            const body = await safeParse(response);
            if (!response.ok) {
                manejarError(response.status, body);
                return;
            }
            mesaActual = body;
            actualizarMesaCard(body.mesa);
            upsertClientRow(body.cliente);
            actualizarEstadisticas();
            poblarMesaModal(body);
            mostrarAlerta('Cliente asignado a la mesa.', 'success');
        } catch (error) {
            console.error(error);
            mostrarAlerta('No se pudo asignar el cliente.', 'danger');
        }
    }

    async function manejarCambioEstadoMesaModal(event) {
        if (!mesaActual) return;
        const select = event.target;
        select.disabled = true;
        const clientePrevio = mesaActual.cliente ? JSON.parse(JSON.stringify(mesaActual.cliente)) : null;
        try {
            const data = await actualizarEstadoRemoto(mesaActual.mesa.id, select.value);
            mesaActual = data;
            actualizarMesaCard(data.mesa);
            if (clientePrevio && (!data.cliente || clientePrevio.id !== data.cliente.id)) {
                clientePrevio.mesa = null;
                clientePrevio.mesaId = '';
                upsertClientRow(clientePrevio);
            }
            if (data.cliente) {
                upsertClientRow(data.cliente);
            }
            actualizarEstadisticas();
            poblarMesaModal(data);
            mostrarAlerta('Estado de mesa actualizado.', 'success');
        } catch (error) {
            select.value = mesaActual?.mesa?.estado || estadosMesa[0];
        } finally {
            select.disabled = false;
        }
    }

    async function manejarCambioEstadoInline(select) {
        if (!canManage) {
            mostrarAlerta('No tienes permisos para modificar estados.', 'warning');
            select.value = select.dataset.current || select.value;
            return;
        }
        const mesaId = select.dataset.mesaId;
        if (!mesaId) return;
        const valorAnterior = select.dataset.current || select.value;
        select.disabled = true;
        const row = select.closest('tr[data-cliente-row]');
        const clientePrevio = select.dataset.clienteId && row ? extraerClienteRow(row) : null;
        try {
            const data = await actualizarEstadoRemoto(mesaId, select.value);
            if (clientePrevio && (!data.cliente || clientePrevio.id !== data.cliente.id)) {
                clientePrevio.mesa = null;
                clientePrevio.mesaId = '';
                upsertClientRow(clientePrevio);
            }
            if (data.cliente) {
                upsertClientRow(data.cliente);
            }
            select.dataset.current = select.value;
            actualizarMesaCard(data.mesa);
            if (mesaActual && mesaActual.mesa.id === data.mesa.id) {
                mesaActual = data;
                poblarMesaModal(data);
            }
            actualizarEstadisticas();
            mostrarAlerta('Estado actualizado.', 'success');
        } catch (error) {
            select.value = valorAnterior;
        } finally {
            select.disabled = false;
        }
    }

    async function actualizarEstadoRemoto(mesaId, estado) {
        const response = await fetch(`/api/mesas/${mesaId}/estado`, {
            method: 'POST',
            headers: buildHeaders(),
            body: JSON.stringify({ estado })
        });
        const body = await safeParse(response);
        if (!response.ok) {
            manejarError(response.status, body);
            throw new Error(body?.message || 'Error al actualizar estado');
        }
        return body;
    }

    function abrirFormularioCliente(cliente, modo) {
        if (!clienteModal) return;
        refs.clienteModalTitle.textContent = modo === 'editar' ? 'Editar cliente' : 'Registrar cliente';
        refs.clienteSubmitBtn.textContent = modo === 'editar' ? 'Actualizar' : 'Guardar';
        document.getElementById('clienteId').value = cliente.id || '';
        document.getElementById('clienteNombres').value = cliente.nombres || '';
        document.getElementById('clienteApellidos').value = cliente.apellidos || '';
        document.getElementById('clienteDni').value = cliente.dni || '';
        document.getElementById('clienteTelefono').value = cliente.telefono || '';
        document.getElementById('clienteCorreo').value = cliente.correo || '';

        construirOpcionesMesa(cliente.mesaId || mesaActual?.mesa?.id);
        refs.clienteModalAlert?.classList.add('d-none');
        clienteModal.show();
    }

    function construirOpcionesMesa(preseleccion) {
        const select = document.getElementById('clienteMesaSelect');
        if (!select) return;
        select.innerHTML = '';
        let primerDisponible = null;
        getMesaCards().forEach(card => {
            const option = document.createElement('option');
            option.value = card.dataset.id;
            option.textContent = `Mesa ${card.dataset.numero} (${card.dataset.estado})`;
            const estado = (card.dataset.estado || '').toLowerCase();
            if (estado !== 'disponible' && option.value !== String(preseleccion || '')) {
                option.disabled = true;
            }
            if (estado === 'disponible' && !primerDisponible) {
                primerDisponible = option.value;
            }
            select.append(option);
        });
        const valor = preseleccion ? String(preseleccion) : primerDisponible;
        if (valor) {
            select.value = valor;
        }
    }

    async function liberarMesa() {
        if (!mesaActual) return;
        try {
            const response = await fetch(`/api/mesas/${mesaActual.mesa.id}/liberar`, {
                method: 'POST',
                headers: buildHeaders(false)
            });
            if (!response.ok) {
                const body = await safeParse(response);
                manejarError(response.status, body);
                return;
            }
            const data = await response.json();
            actualizarMesaCard(data.mesa);
            eliminarFilaPorMesa(data.mesa.id);
            actualizarEstadisticas();
            mesaModal?.hide();
            mostrarAlerta('La mesa fue liberada.', 'success');
        } catch (error) {
            console.error(error);
            mostrarAlerta('No se pudo liberar la mesa.', 'danger');
        }
    }

    async function eliminarCliente(row) {
        const clienteId = row.dataset.clienteId;
        if (!clienteId) return;
        if (!confirm('¿Eliminar este cliente y liberar su mesa?')) return;

        const mesaId = row.dataset.mesaId;
        try {
            const response = await fetch(`/api/clientes/${clienteId}`, {
                method: 'DELETE',
                headers: buildHeaders(false)
            });
            if (!response.ok) {
                const body = await safeParse(response);
                manejarError(response.status, body);
                return;
            }
            row.remove();
            if (mesaId) {
                const card = document.querySelector(`.mesa-card[data-id="${mesaId}"]`);
                if (card) {
                    actualizarMesaCard({
                        id: Number(mesaId),
                        numero: card.dataset.numero,
                        capacidad: Number(card.dataset.capacidad || 0),
                        estado: 'Disponible',
                        ubicacion: card.dataset.ubicacion
                    });
                }
            }
            actualizarEstadisticas();
            toggleEmptyState();
            mostrarAlerta('Cliente eliminado correctamente.', 'success');
        } catch (error) {
            console.error(error);
            mostrarAlerta('No se pudo eliminar el cliente.', 'danger');
        }
    }

    function obtenerPayloadFormulario() {
        return {
            id: valueOrNull(document.getElementById('clienteId').value),
            dni: document.getElementById('clienteDni').value.trim(),
            nombres: document.getElementById('clienteNombres').value.trim(),
            apellidos: document.getElementById('clienteApellidos').value.trim(),
            telefono: document.getElementById('clienteTelefono').value.trim(),
            correo: document.getElementById('clienteCorreo').value.trim(),
            mesaId: Number(document.getElementById('clienteMesaSelect').value)
        };
    }

    function upsertClientRow(cliente) {
        if (!refs.clientesTable || !cliente) return;
        let row = refs.clientesTable.querySelector(`tr[data-cliente-id="${cliente.id}"]`);
        const exists = Boolean(row);
        if (!row) {
            row = document.createElement('tr');
            row.setAttribute('data-cliente-row', 'true');
            refs.clientesTable.prepend(row);
        }
        row.dataset.clienteId = cliente.id;
        row.dataset.clienteNombres = cliente.nombres || '';
        row.dataset.clienteApellidos = cliente.apellidos || '';
        row.dataset.clienteDni = cliente.dni || '';
        row.dataset.clienteTelefono = cliente.telefono || '';
        row.dataset.clienteCorreo = cliente.correo || '';
        row.dataset.mesaId = cliente.mesa?.id || '';
        row.dataset.mesaNumero = cliente.mesa?.numero || '';
        row.dataset.mesaEstado = cliente.mesa?.estado || '';

        const estadoBadge = cliente.mesa
            ? `<span class="badge-estado cliente-estado ${cliente.mesa.estado.toLowerCase()}">${cliente.mesa.estado}</span>`
            : '<span class="badge-estado sin-mesa">Sin mesa</span>';

        row.innerHTML = `
            <td>
                <div class="fw-semibold">${cliente.nombres || ''} ${cliente.apellidos || ''}</div>
                <small class="text-muted">${cliente.correo || ''}</small>
            </td>
            <td>${cliente.dni || '-'}</td>
            <td>${cliente.mesa ? `Mesa ${cliente.mesa.numero}` : 'Sin mesa'}</td>
            <td>
                ${cliente.mesa ? `
                <select class="form-select form-select-sm mesa-estado-inline"
                        data-mesa-id="${cliente.mesa.id}"
                        data-cliente-id="${cliente.id}"
                        data-context="dashboard"
                        data-current="${cliente.mesa.estado}">
                    ${estadosMesa.map(estado => `
                        <option value="${estado}" ${estado === cliente.mesa.estado ? 'selected' : ''}>${estado}</option>
                    `).join('')}
                </select>` : '<span class="badge-estado sin-mesa">Sin mesa</span>'}
            </td>
            ${canManage ? `
            <td class="text-center">
                <div class="btn-group btn-group-sm">
                    <button type="button" class="btn btn-outline-primary" data-action="edit">Editar</button>
                    <button type="button" class="btn btn-outline-danger" data-action="delete">Eliminar</button>
                </div>
            </td>` : ''}
        `;

        if (!exists) {
            toggleEmptyState();
        }
    }

    function eliminarFilaPorMesa(mesaId) {
        if (!refs.clientesTable || !mesaId) return;
        const row = refs.clientesTable.querySelector(`tr[data-cliente-row][data-mesa-id="${mesaId}"]`);
        if (row) {
            row.dataset.mesaId = '';
            row.dataset.mesaNumero = '';
            row.dataset.mesaEstado = '';
            row.querySelector('td:nth-child(3)').textContent = 'Sin mesa';
            row.querySelector('td:nth-child(4)').innerHTML = '<span class="badge-estado sin-mesa">Sin mesa</span>';
        }
    }

    function toggleEmptyState() {
        if (!refs.clientesEmptyRow || !refs.clientesTable) return;
        const rows = refs.clientesTable.querySelectorAll('tr[data-cliente-row]');
        if (rows.length === 0) {
            refs.clientesEmptyRow.classList.remove('d-none');
        } else {
            refs.clientesEmptyRow.classList.add('d-none');
        }
    }

    function actualizarMesaCard(mesa) {
        const card = document.querySelector(`.mesa-card[data-id="${mesa.id}"]`);
        if (!card) return;
        card.dataset.estado = mesa.estado;
        card.dataset.numero = mesa.numero;
        card.dataset.capacidad = mesa.capacidad;
        card.dataset.ubicacion = mesa.ubicacion || 'Sin ubicación';

        card.querySelector('.mesa-state').textContent = mesa.estado;
        card.querySelector('.mesa-number').textContent = `Mesa ${mesa.numero}`;
        card.querySelector('.mesa-capacidad').textContent = `${mesa.capacidad} personas`;
        card.querySelector('.mesa-ubicacion').textContent = mesa.ubicacion || 'Sin ubicación';

        card.classList.remove('status-disponible', 'status-ocupada', 'status-reservada');
        card.classList.add(`status-${mesa.estado.toLowerCase()}`);
    }

    function actualizarEstadisticas() {
        const stats = { total: 0, disponible: 0, ocupada: 0, reservada: 0 };
        getMesaCards().forEach(card => {
            stats.total += 1;
            const estado = (card.dataset.estado || '').toLowerCase();
            if (estado === 'disponible') stats.disponible += 1;
            else if (estado === 'ocupada') stats.ocupada += 1;
            else if (estado === 'reservada') stats.reservada += 1;
        });
        const clientesActivos = refs.clientesTable ? refs.clientesTable.querySelectorAll('tr[data-cliente-row]').length : 0;
        setText('statTotal', stats.total);
        setText('statDisponibles', stats.disponible);
        setText('statOcupadas', stats.ocupada);
        setText('statReservadas', stats.reservada);
        setText('statClientes', clientesActivos);
    }

    function filtrarClientes(event) {
        if (!refs.clientesTable) return;
        const term = event.target.value.trim().toLowerCase();
        const rows = refs.clientesTable.querySelectorAll('tr[data-cliente-row]');
        rows.forEach(row => {
            const text = row.textContent.toLowerCase();
            row.classList.toggle('d-none', term && !text.includes(term));
        });
    }

    function buildHeaders(includeJson = true) {
        const headers = {};
        if (includeJson) {
            headers['Content-Type'] = 'application/json';
        }
        if (csrfHeader && csrfToken) {
            headers[csrfHeader] = csrfToken;
        }
        return headers;
    }

    async function safeParse(response) {
        const text = await response.text();
        if (!text) return null;
        try {
            return JSON.parse(text);
        } catch (error) {
            return { message: text };
        }
    }

    function mostrarAlerta(message, type) {
        if (!refs.alertBox) {
            console.log(`[${type}] ${message}`);
            return;
        }
        refs.alertBox.className = `alert alert-${type} alert-floating`;
        refs.alertBox.textContent = message;
        refs.alertBox.classList.remove('d-none');
        clearTimeout(refs.alertTimeout);
        refs.alertTimeout = setTimeout(() => {
            refs.alertBox.classList.add('d-none');
        }, 3500);
    }

    function manejarError(status, body) {
        let message = body?.message || 'Ocurrió un error inesperado.';
        if (status === 403) {
            message = 'No tienes permisos suficientes para esta acción.';
        } else if (status === 409) {
            message = body?.message || 'La operación no es válida.';
        }
        mostrarAlerta(message, status === 403 ? 'warning' : 'danger');
    }

    function mostrarErrorFormulario(body) {
        if (!refs.clienteModalAlert) return;
        refs.clienteModalAlert.textContent = body?.message || 'Revisa los datos ingresados.';
        refs.clienteModalAlert.classList.remove('d-none');
    }

    function extraerClienteRow(row) {
        if (!row) return {};
        return {
            id: row.dataset.clienteId || '',
            nombres: row.dataset.clienteNombres || '',
            apellidos: row.dataset.clienteApellidos || '',
            dni: row.dataset.clienteDni || '',
            telefono: row.dataset.clienteTelefono || '',
            correo: row.dataset.clienteCorreo || '',
            mesaId: row.dataset.mesaId || ''
        };
    }

    function setText(id, value) {
        const el = document.getElementById(id);
        if (el && value !== undefined && value !== null) {
            el.textContent = value;
        }
    }

    function valueOrNull(value) {
        return value ? value : null;
    }
})();
