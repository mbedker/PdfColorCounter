var colorCounterApp = angular.module('colorCounterApp', [
    'ngRoute',
    'colorCounterControllers',
    'colorCounterServices',
    'colorCounterDirectives'
]);

colorCounterApp.config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.
        when('/start', {
            templateUrl: '/assets/partials/submitPdf.html',
            controller: 'SubmitPdfCtrl'
        }).
        when('/main', {
            templateUrl: '/assets/partials/main-view.html',
            controller: 'MainViewCtrl'
            }).
        otherwise({
            redirectTo: '/start'
        });
}]);