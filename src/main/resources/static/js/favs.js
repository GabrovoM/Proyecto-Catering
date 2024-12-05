
	$(document).ready(function() {   
    var csrfToken = $('meta[name="_csrf"]').attr('content');
    var csrfHeader = $('meta[name="_csrf_header"]').attr('content');    
    $('.favme').off('click').on('click', function() {
        var id = $(this).data('id');
        var button = $(this);
        var gestionSegunStatusCode = {
            400: function() {
                console.log('Error de petición');
            },
            204: function() {
                button.toggleClass('active');
            }
        };
        if (button.hasClass('active')) {
            $.ajax({
                type: "POST",
                url: "/platos/" + id + "/removeFav",
                beforeSend: function(xhr) {
                    xhr.setRequestHeader(csrfHeader, csrfToken);
                },
                statusCode: gestionSegunStatusCode
            });
        } else {
            $.ajax({
                type: "POST",
                url: "/platos/" + id + "/addFav",
                beforeSend: function(xhr) {
                    xhr.setRequestHeader(csrfHeader, csrfToken);
                },
                statusCode: gestionSegunStatusCode
            });
        }
    });
});
