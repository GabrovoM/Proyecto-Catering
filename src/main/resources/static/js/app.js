$(document).ready(
	function() {

		// datos de la API
			async function fetchPedidosData(estado) {
		    try {
		        const response = await fetch(`/api/pedidos?estado=${estado}`);
		        const pedidosCount = await response.json();
		        console.log("Pedidos Count:", pedidosCount);
		        return pedidosCount;
		    } catch (error) {
		        console.error('Error:', error);
		        return {};
		    }
		}
		
		function prepareChartData(pedidosCount) {
		    const labels = Object.keys(pedidosCount);
		    const data = Object.values(pedidosCount);
		    const today = new Date();
		    const currentMonth = today.getMonth();
		    const currentYear = today.getFullYear();
		    const combined = labels.map((label, index) => ({
		        date: label,
		        count: data[index],
		        timestamp: new Date(label).getTime()
		    }));
		    const filtered = combined.filter(item => {
		        const itemDate = new Date(item.date);
		        return itemDate.getMonth() === currentMonth && itemDate.getFullYear() === currentYear;
		    });
		    filtered.sort((a, b) => a.timestamp - b.timestamp);
		    const sortedLabels = filtered.map(item => item.date);
		    const sortedData = filtered.map(item => item.count);
		    return { labels: sortedLabels, data: sortedData };
		}
		
		function renderChart(labels, data) {		    
		    const canvas = document.getElementById('pedidosChart');
		    if (!canvas) {		       
		        return; 
		    }		
		    const ctx = canvas.getContext('2d');
		    new Chart(ctx, {
		        type: 'line',
		        data: {
		            labels: labels,
		            datasets: [{
		                label: 'Número de pedidos finalizados',
		                data: data,
		                borderColor: 'rgba(75, 192, 192, 1)',
		                backgroundColor: 'rgba(75, 192, 192, 0.2)',
		                borderWidth: 1,
		                fill: true
		            }]
		        },
		        options: {
		            responsive: true,
		            plugins: {
		                legend: {
		                    display: true
		                },
		                tooltip: {
		                    mode: 'index',
		                    intersect: false
		                }
		            },
		            scales: {
		                x: {
		                    title: {
		                        display: true,
		                        text: 'Fecha de Envío'
		                    }
		                },
		                y: {
		                    title: {
		                        display: true,
		                        text: 'Número de Pedidos'
		                    },
		                    beginAtZero: true,
		                    ticks: {
		                        callback: function(value) {
		                            return Number.isInteger(value) ? value : null;
		                        }
		                    }
		                }
		            }
		        }
		    });
		}
		
		async function init() {
		    const estado = 'FINALIZADO';
		    const pedidosCount = await fetchPedidosData(estado);
		    const { labels, data } = prepareChartData(pedidosCount);
		    renderChart(labels, data);
		}
		init();		

		$('.navbar-toggler').on('click', function() {
			var navbarCollapse = $('#mobileNavbarNav');
			var navbarSupportedContent = $('#navbarSupportedContent');			
			navbarSupportedContent.css('padding','5px');
			var navbar = $('.navbar');
			var navbarNav = $('.navbar-nav');
			navbarCollapse.toggleClass('show');
			if (navbarCollapse.hasClass('show')) {
				navbarCollapse.css('max-height', navbarCollapse.prop('scrollHeight') + 'px');
				navbar.css('height', '575px');
			} else {
				navbarCollapse.css('max-height', '0');
				navbar.css('height', '60px');
			}
			navbarNav.toggleClass('show');
			if (navbarNav.hasClass('show')) {
				navbarNav.css('max-height', navbarNav.prop('scrollHeight') + 'px');
			} else {
				navbarNav.css('max-height', '0');
			}
		});

		if (typeof $.fn.countTo !== 'undefined') {
			$('.counter').each(function() {
				const targetNumber = $(this).data('to');
				if (targetNumber) {
					$(this).countTo({
						from: 0,
						to: targetNumber,
						speed: 2000,
						refreshInterval: 50,
					});
				}
			});
		} else {
			console.log('countTo no disponible');
		}

		$('.btn-toggle').click(function() {
			$(this).find('.btn').toggleClass('active');
			if ($(this).find('.btn-primary').length > 0) {
				$(this).find('.btn').toggleClass('btn-primary');
			}
			$(this).find('.btn').toggleClass('btn-default');
		});	

		
	});
	
	
	var currentDateElement = document.getElementById("currentDate");
	var relojElement = document.getElementById("reloj");
	if (currentDateElement) {
		var currentDate = new Date();
		var options = {
			weekday: 'long',
			day: '2-digit',
			month: 'numeric',
			year: 'numeric'
		};
		var formattedDate = currentDate.toLocaleDateString('es-ES', options);
		formattedDate = formattedDate.charAt(0).toUpperCase() + formattedDate.slice(1);
		currentDateElement.textContent = formattedDate;
	}
	if (relojElement) {
		var t = setInterval(obtenerHora, 500);
		function obtenerHora() {
			var fecha = new Date();
			var h = fecha.getHours();
			if (h < 10) {
				h = "0" + h;
			}
			var m = fecha.getMinutes();
			if (m < 10) {
				m = "0" + m;
			}
			var s = fecha.getSeconds();
			if (s < 10) {
				s = "0" + s;
			}
			relojElement.innerHTML = h + ":" + m + ":" + s;
		}
	}	
	
	function validarFilterPedidos() {
		document.getElementById("error-message").innerText = '';
		const fechaInicio = document.getElementById("f_ini").value;
		const fechaFin = document.getElementById("f_fin").value;
		const estado = document.getElementById("estado").value;	
		if (!fechaInicio && !fechaFin && !estado) {
			document.getElementById("error-message").innerText = 'Por favor, selecciona al menos un filtro.';
			return false;
		}	
		if ((fechaInicio && !fechaFin) || (!fechaInicio && fechaFin)) {
			document.getElementById("error-message").innerText = 'Por favor, selecciona fecha inicio y fecha fin.';
			return false;
		}	
		if (fechaInicio && fechaFin) {
			const fecha_ini = new Date(fechaInicio);
			const fecha_fin = new Date(fechaFin);
			if (fecha_ini > fecha_fin) {
				document.getElementById("error-message").innerText = 'La fecha de inicio debe ser anterior a la fecha de fin.';
				document.getElementById("f_ini").value = "";
				document.getElementById("f_fin").value = "";
				return false;
			}
		}	
		return true;
	}	
	
	$('#modal-delete_plato').on('show.bs.modal', function (event) {
			var button = $(event.relatedTarget); 
			var platoName = button.data('name'); 
			var platoId = button.data('id');
			var modal = $(this);
			modal.find('#plato-name').text(platoName); 
			modal.find('#confirm-delete').attr('href', '/admin/plato/delete/' + platoId); 
	});
	
		
	$('#modal-delete_pedido').on('show.bs.modal', function (event) {
		var button = $(event.relatedTarget);
		var pedidoNumero = button.data('numero');
		var pedidoId = button.data('id');
		var source = button.data('source'); 
		var modal = $(this);
		modal.find('#pedido-numero').text(pedidoNumero);
		modal.find('#confirm-delete').attr('href', '/admin/pedidos/delete/' + pedidoId + '?source=' + source); 
	});
	
	$('#modal-delete_menu').on('show.bs.modal', function (event) {
		var button = $(event.relatedTarget); 
		var menuName = button.data('name'); 
		var menuId = button.data('id');
		var modal = $(this);
		modal.find('#menu-name').text(menuName); 
		modal.find('#confirm-delete').attr('href', '/admin/menus/delete/' + menuId); 
	});	
	
	$('#modal-delete_ingr').on('show.bs.modal', function (event) {
		var button = $(event.relatedTarget); 
		var ingredientName = button.data('name'); 
		var ingredientId = button.data('id');
		var modal = $(this);
		modal.find('#ingredient-name').text(ingredientName); 
		modal.find('#confirm-delete').attr('href', '/admin/ingredientes/delete/' + ingredientId); 
	});
	
	$('#modal-delete').on('show.bs.modal', function (event) {
			var button = $(event.relatedTarget); 
			var categoriaName = button.data('name'); 
			var categoriaId = button.data('id');
			var modal = $(this);
			modal.find('#categoria-name').text(categoriaName); 
			modal.find('#confirm-delete').attr('href', '/admin/categoria/delete/' + categoriaId); 
	});	
	
	$('#modal-delete_usuario').on('show.bs.modal', function (event) {
			var button = $(event.relatedTarget); 
			var usuarioNombre = button.data('nombre'); 
			var usuarioId = button.data('id'); 
			var usuarioRole = button.data('role'); 			
			var modal = $(this);
			modal.find('#usuario-nombre').text(usuarioNombre);
			modal.find('#tipo-usuario').text(usuarioRole === 'cliente' ? 'Cliente' : 'Usuario');			
			var deleteUrl = usuarioRole === 'cliente'
				? '/admin/usuario/delete/cliente/' + usuarioId
				: '/admin/usuario/delete/usuario/' + usuarioId;
			modal.find('#confirm-delete').attr('href', deleteUrl); 
	});	

	document.addEventListener("DOMContentLoaded", () => {
	    const inputs = ["precioEnvioZona1", "precioEnvioZona2"];     
	    inputs.forEach(id => {
	        const input = document.getElementById(id);
	        if (input) {           
	            if (input.value) {
	                input.value = parseFloat(input.value).toFixed(2);
	            } 
	            input.addEventListener("blur", () => {
	                if (input.value) {
	                    input.value = parseFloat(input.value).toFixed(2); 
	                }
	            });         
	            input.addEventListener("input", () => {
	                const value = input.value;
	                if (!value || /^[0-9]*(\.[0-9]{0,2})?$/.test(value)) {
	                    input.dataset.previousValue = value; 
	                } else {                   
	                    input.value = input.dataset.previousValue || "";
	                }
	            });
	            input.dataset.previousValue = input.value;
	        }
	    });
	});
	
	window.onload = function () {
		sessionStorage.clear();
	}
	
	// cambiar_plato	
	function validateForm() {
		const selectedPlato = document.querySelector('input[name="selectedPlato"]:checked');
		if (!selectedPlato) {
			Swal.fire({
				icon: 'warning', 
				title: 'Oops...',
				text: 'Por favor, selecciona Plato antes de guardar.',
				confirmButtonText: 'OK'
			});
			return false;  
		}
		return true;  
	}	

	document.querySelector(".form-perfil").onsubmit = function (event) {
	    event.preventDefault();
	    const newPassword = document.getElementById("newPassword").value;
	    const confirmPassword = document.getElementById("confirmPassword").value;
	    const passwordRegex = /^(?=.*\d).{8,}$/;		  
	    if (newPassword || confirmPassword) {
	        if (!passwordRegex.test(newPassword)) {
	            Swal.fire({
	                title: "Error",
	                text: "La contraseña debe tener al menos 8 caracteres y contener al menos un número.",
	                icon: "error",
	                button: "OK",
	                focusConfirm: false,
	            }).then(() => {
	                setTimeout(() => document.getElementById("newPassword").focus(), 0);
	            });
	            return; 
	        }
	        if (newPassword !== confirmPassword) {
	            Swal.fire({
	                title: "Error",
	                text: "Las contraseñas no coinciden.",
	                icon: "error",
	                button: "OK",
	                focusConfirm: false,
	            }).then(() => {
	                setTimeout(() => document.getElementById("newPassword").focus(), 0);
	            });
	            return; 
	        }
	    }
	    this.submit(); 
	};		


