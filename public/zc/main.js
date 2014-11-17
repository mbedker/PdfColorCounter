



var createZclip = function($scope, buttonId, setFuction){
    $(document).ready(function() {
        var client = new ZeroClipboard($('#' + buttonId), {
            moviePath : '/assets/zc/ZeroClipboard.swf'
        });

    var client = new ZeroClipboard( document.getElementById("copy-button") );

    client.on( "ready", function( readyEvent ) {
      // alert( "ZeroClipboard SWF is ready!" );

      client.on( "aftercopy", function( event ) {
        // `this` === `client`
        // `event.target` === the element that was clicked
        event.target.style.display = "none";
        alert("Copied text to clipboard: " + event.data["text/plain"] );
      } );
    } );
}

var createSet = function () {
    var setRandom = [];
    for(var i = 0; i < 10; i++) {
        var digit = Math.random();
    };
};