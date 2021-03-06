var colorCounterServices = angular.module('colorCounterServices', ['ngResource']);

colorCounterServices.service('fileUpload', ['$http', function ($http) {
    this.uploadFileToUrl = function(file, uploadUrl, callback){
        var fd = new FormData();
        fd.append('pdfFile', file);
        $http.post(uploadUrl, fd, {
            transformRequest: angular.identity,
            headers: {'Content-Type': undefined}
        })
        .success(function(data){
            if (typeof callback === 'function'){
                callback(data);
            }
        })
        .error(function(){
        alert('error')
        });
    }
}]);

colorCounterServices.service('getStatus', ['$http', function($http){
    this.getPdfStatus = function(result, callback) {
        if(result){
        var url = 'pdf/status/' + result.sessionId;
        $http.get(url,{
            transformRequest: angular.identity,
            })
            .success(function(data){
                if (typeof callback === 'function'){
                    callback(data);
                }
            })
            .error(function(xhr){
                console.log(xhr);
            });
        } else {
            callback({
                message: 'The upload result was not provided'
            });
        }
    };
}]);

