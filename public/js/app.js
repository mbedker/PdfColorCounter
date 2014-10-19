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
            templateUrl: '/assets/partials/submit-pdf.html',
            controller: 'SubmitPdfCtrl'
        }).
        when('/main', {
            templateUrl: '/assets/partials/main-view.html',
            controller: 'MainViewCtrl'
            }).
        otherwise({
            redirectTo: '/start'
        });
}]).run( function($rootScope, $location) {
          // register listener to watch route changes
          $rootScope.$on( "$routeChangeStart", function(event, next, current) {
              if (!window.pdfSession) {
                  if (window.localStorage && window.localStorage.getItem("pdfSession")) {
                      // Load previous session if we have it.
                      window.pdfSession = JSON.parse(window.localStorage.getItem("pdfSession"));
                  } else {
                      // If we don't have a pdf session in memory we need to upload a pdf file
                      if (next.originalPath == "/start") {
                          // already going to #start, no redirect needed
                      } else {
                          // not going to #start, we should redirect now
                          $location.path( "/start" );
                      }
                  }
              }
          });
      });