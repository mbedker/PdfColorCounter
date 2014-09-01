var pdfColorCounterApp = angular.module('pdfColorCounterApp',[
    'ngRoute',
    'pdfColorCounterAppControllers'
]);

pdfColorCounterApp.config(['routeProvider',
    function($routeProvider) {
        $routeProvider.
        when('/welcome',{
            template.Url: '/app/partials/'
            controller: 'AppStartCtrl'
        }).
        when('/sessionId',{
            template.Url: '/app/partials/mainView.html'
            controller: 'mainViewCtrl';
        }).
        otherwise({
            redirectTo: '/welcome'
        });
}]);

