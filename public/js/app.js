var phonecatApp = angular.module('phonecatApp', [
    'ngRoute',
    'phonecatControllers',
    'phonecatFilters',
    'phonecatServices',
    'phonecatDirectives'
]);

phonecatApp.config(['$routeProvider',
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