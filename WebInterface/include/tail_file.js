var tailFileTimer;


function loadNextTail(fname,highlights,offset,delay) {
        clearTimeout(tailFileTimer);
        tailFileTimer = setTimeout(function(){doLoadNextTail(fname,highlights,offset)}, delay);
}

function doLoadNextTail(fname,highlights,offset) {
        $.get("./tail_file.cgi?fname="+fname+"&highlights="+highlights+"&offset="+offset, function(data) {
                var block=$(document.documentElement);
                        var vb=$("#view_block");

                        // If we find a view_block, use it instead
                        if (vb.length > 0) {
                                block=vb;
                        }

                        var docElement = $(document)[0].documentElement;
                        var winElement = $(window)[0];
                        var posA=docElement.scrollHeight - winElement.innerHeight;
                        var posB=winElement.pageYOffset;

                        var debug="<hr>posA="+posA+"<br>posB="+posB+"<br><hr>";
                        $("#nextchunk").replaceWith(data);

                        if (posB>=posA) {
                                $('html, body').scrollTop($(document).height());
                        }
        });
}

