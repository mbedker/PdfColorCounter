var pdfColorCounterControllers = angular.module('pdfColorCounterControllers', []);

pdfColorCounterControllers.controller('startCtrl', ['$file', '$http']
    function startParsing($file) {
        $http.post('/pdf/start' + $file).success(function(data) {
        $sessionId = data.sessionId;
        }
        $http.get('pdf/status/' + $sessionId)
    }
    );
    function redirect($sessionId) {
        $http.get
    }
)
