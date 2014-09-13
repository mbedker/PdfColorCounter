var phonecatControllers = angular.module('phonecatControllers', []);

phonecatControllers.controller('PhoneListCtrl', ['$scope', 'Phone', function($scope, Phone) {
    $scope.phones = Phone.query();
    $scope.orderProp = 'age';
  }]);

phonecatControllers.controller('PhoneDetailCtrl', ['$scope', '$routeParams', '$http',
    function($scope, $routeParams, $http) {
        $http.get('assets/phones/' + $routeParams.phoneId + '.json').success(function(data) {
        $scope.phone = data;
        $scope.mainImageUrl = data.images[0];
        });
        $scope.setImage = function(imageUrl) {
            $scope.mainImageUrl = imageUrl;
            }
    }]);

phonecatControllers.controller('SubmitPdfCtrl', ['$scope', '$location', 'fileUpload', '$http', function($scope, $location, fileUpload){

    $scope.uploadFile = function(callback){
        var file = $scope.myFile;
        var uploadUrl = "/pdf/start";
        fileUpload.uploadFileToUrl(file, uploadUrl, function(d){
           if(callback && (typeof callback === 'function')) {
                callback(d);
            }
            if (d) {
                //window.onbeforeunload = function(){} to handel before page unloads
                $scope.uploadResult = d;
                window.uploadResult = d;
                if(window.sessionStorage) {
                window.sessionStorage.setItem("uploadResult", d);
                }
            $location.path ('/main');
            }
        });
    };
}]);

phonecatControllers.controller('MainViewCtrl', ['$scope', 'getStatus', '$http', function($scope, getStatus, $http) {
    $scope.getPdfStatus = function(result, callback){
        getStatus.getPdfStatus(result, callback);
    };
    $scope.queryStatus = function(){
        var i = 1000,
            $this = this;
            filter = [];
        var table = $('#thumbnailsContainer');
        var tbody = table.find('tbody');
        var timer = window.setInterval(function(){
            $this.getPdfStatus(window.uploadResult, function(data){
                if (data){
                    if (data.isCompleted) {
                window.clearInterval(timer);
                } else {
                    var pages = data.completedPages.sort(function(a, b){
                     return a.pageNumber > b.pageNumber;
                    });
                    for (var idx = 0; idx < pages.length; idx++){
                        var pg = pages[idx];
                        if(filter.indexOf(pg.pageNumber) <= -1){
                            //It doesn't exist in the array

                            var tr = $('<tr />');
                            var td = $('<td />');
                            var img = $('<img src = "/pdf/thumbnail/' + data.sessionId + '/' + pg.pageNumber + '" />');
                            td.append(img);
                            tr.append(td);
                            tbody.append(tr);
                            filter.push(pg.pageNumber);
                            //This is the end of the logic
                        }
                    }
                    }
                }
            });
        }, (function(i) {
            if (i < 5000) {
                i = i + 1000;
                }
                return i;
                }(i)));
    };
}]);