var phonecatServices = angular.module('phonecatServices', ['ngResource']);

phonecatServices.factory('Phone', ['$resource',
    function($resource){
        return $resource('assets/phones/:phoneId.json', {},{
        query: {method: 'GET', params:{phoneId: 'phones'}, isArray:true}
        });
}]);