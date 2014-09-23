var colorCounterControllers = angular.module('colorCounterControllers', []);

colorCounterControllers.controller('SubmitPdfCtrl', ['$scope', '$location', 'fileUpload', '$http', function($scope, $location, fileUpload){

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

colorCounterControllers.controller('MainViewCtrl', ['$scope', 'getStatus', '$http', function($scope, getStatus, $http) {
    $scope.getPdfStatus = function(result, callback){
        getStatus.getPdfStatus(result, callback);
    };
    $scope.queryStatus = function(){
        var timerInterval = 1000,
            $this = this;
            filter = [];
        var table = $('#thumbnail-table');
        var tbody = table.find('tbody');
        var displayedPages = [];

        var containerHtml = '<tr>';
        for (var pageNumber = 1; pageNumber <= window.uploadResult.numberOfPages; pageNumber++){
           containerHtml = containerHtml + '<td><div id="page' + pageNumber + '" class = "page-wrapper"></div></td>';
        }
        containerHtml = containerHtml + '</tr>';
        tbody.append(containerHtml);
        //add wrappers, inject content, when its done parsing
        //setting width and height of wrapper beforehand to have room for
        //tbody.
        var timer = window.setInterval(function(){
            $this.getPdfStatus(window.uploadResult, function(data){
                if (data){
                    if (data.isCompleted) {
                window.clearInterval(timer);
                } else {
                    var pages = data.completedPages.sort(function(a, b){
                     return a.pageNumber > b.pageNumber;
                    });

                    for (var i = 0; i < pages.length; i++) {
                        var pg = pages[i];
                        if (filter.indexOf(pg.pageNumber)<=-1) {
                            var pageContainer = tbody.find('#page'+ pg.pageNumber);
                            pageContainer.append('<img src = "/pdf/thumbnail/' + data.sessionId + '/' + pg.pageNumber + '"></img><div>' + pg.pageNumber + " : " + pg.percentColor + '</div>');
                            filter = filter + pg.pageNumber;
                        }
                    }
                }   //This is the end of the logic
             }
        });
        }, (function(timerInterval) {
            if (timerInterval < 5000) {
                timerInterval = timerInterval + 1000;
                }
                return timerInterval;
                }(timerInterval)));
    };
}]);