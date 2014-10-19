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
                $scope.pdfSession = d;
                window.pdfSession = d;
                if (window.localStorage) {
                    window.localStorage.setItem("pdfSession", JSON.stringify(d));
                }
            $location.path ('/main');
            }
        });
    };
}]);

colorCounterControllers.controller('MainViewCtrl', ['$scope', 'getStatus', '$http', function($scope, getStatus, $http) {
        var table = $('#thumbnail-table');
        var tbody = table.find('tbody');
    $scope.getPdfStatus = function(result, callback){
        getStatus.getPdfStatus(result, callback);
    };
    $scope.queryStatus = function(){
        var timerInterval = 1000,
            $this = this;
            filter = [];
        var displayedPages = [];
        window.selectedPages = [];
        var colorPages = [];

        $scope.pageColorPercentMap = {};

        var containerHtml = '<tr>';
        for (var pageNumber = 1; pageNumber <= window.pdfSession.numberOfPages; pageNumber++){
           containerHtml = containerHtml + '<td><div class="page-wrapper"><div id="' + pageNumber + '" class="selectable unselected"></div></div></td>';
        }
        containerHtml = containerHtml + '</tr>';
        tbody.append(containerHtml);
        //add wrappers, inject content, when its done parsing
        //setting width and height of wrapper beforehand to have room for
        //tbody.

        var onSelectPageFunction = function() {
            var element = $(this);
            element.toggleClass('selected');
            element.toggleClass('unselected')

            var pageNum = element.attr('id');
            if (element.hasClass('selected')) {
                window.selectedPages.push(pageNum);
            } else {
                window.selectedPages.splice( $.inArray(pageNum, window.selectedPages), 1 );
            }

            console.log(window.selectedPages); // To be removed
        }

        var timer = window.setInterval(function(){
            $this.getPdfStatus(window.pdfSession, function(data){
                if (data) {
                window.pdfStatus = data;
                    for (var i = 0; i < data.completedPages.length; i++) {
                        var pg = data.completedPages[i];

                        if (filter.indexOf(pg.pageNumber)<=-1) {
                            var pageContainer = tbody.find('#'+ pg.pageNumber);
                            pageContainer.append('<img src="/pdf/thumbnail/' + data.sessionId + '/' + pg.pageNumber + '"></img><div>' + pg.pageNumber + " : " + pg.percentColor + '</div>');
                            pageContainer.click(onSelectPageFunction);
                            pageContainer.onChange
                            filter.push(pg.pageNumber);
                            $scope.pageColorPercentMap[pg.pageNumber] = pg.percentColor;

                        }
                    }
                    if (data.isComplete)
                        window.clearInterval(timer);
                }   //This is the end of the logic
        });
        }, (function(timerInterval) {
            if (timerInterval < 5000) {
                timerInterval = timerInterval + 1000;
                }
                return timerInterval;
                }(timerInterval)));
    };

        ZeroClipboard.config( {moviePath: 'assets/zeroClipbaord/ZeroClipboard.swf'});
        var client = new ZeroClipboard($('#bw-print-set'));

        client.on( 'load', function(client) {

          client.on( 'datarequested', function(client, text) {
            var text = $scope.copyBWPages;
            client.setText(text);
            console.log(text);
          });

          // callback triggered on successful copying
          client.on( 'complete', function(client, args) {
            console.log("Text copied to clipboard: \n" + args.text );
          });
        });

        // In case of error - such as Flash not being available
        client.on( 'wrongflash noflash', function() {
          ZeroClipboard.destroy();
        } );



        $scope.copyBWPages = function(){
        var bwPages = [];
//           var table = $('#thumbnail-table');
//         var tbody = table.find('tbody');
            for (i = 0; i < window.pdfSession.numberOfPages; i++){
                var page = tbody.find('#' + i);
                if (page.hasClass('unselected')) {
                    bwPages.push(i);
                }
        }
        console.log(bwPages);
        }

        $(function(){
            $("#slider-range").slider({
            range: true,
            min: 0,
            max: 100,
            values: [ 0, 100 ],
            orientation: 'vertical',
            slide: function( event, ui ) {
                $( "#amount" ).val( ui.values[ 0 ] + "% - " + ui.values[ 1 ] + "%" );
                },
            change: function (event, ui) {
                var upperRange = ui.values[1];
                var lowerRange = ui.values[0];
                console.log( upperRange + ':' + lowerRange);

                if (!window.pdfStatus)
                    return;

                //var tbody = $('#thumbnail-table tbody');
                for (var i = 0; i < window.pdfStatus.completedPages.length; i++) {
                    var pg = window.pdfStatus.completedPages[i];
                    var element = tbody.find('#' + pg.pageNumber);

                    if (pg.percentColor > upperRange) {
                        console.log('hide ' + pg.pageNumber);
                        element.parent().parent().hide();
                        window.selectedPages.push(pg.pageNumber);
                    } else if (pg.percentColor < lowerRange) {
                        console.log('hide ' + pg.pageNumber);
                        element.parent().parent().hide();
                        window.selectedPages.splice( $.inArray(pg.pageNumber, window.selectedPages), 1 );
                    } else {
                        console.log('show ' + pg.pageNumber);
                        element.removeClass('selected');
                        window.selectedPages.splice( $.inArray(pg.pageNumber, window.selectedPages), 1 );
                        element.parent().parent().show();
                    }
                }
            }
            });
            $( "#amount" ).val(  $( "#slider-range" ).slider( "values", 0 ) +
                "% - " + $( "#slider-range" ).slider( "values", 1 ) + "%" );
        });

}]);
