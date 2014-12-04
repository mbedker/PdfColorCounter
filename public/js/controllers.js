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
    var selectorToggle = function(item, class1, class2){
        item.toggleClass(class1);
        item.toggleClass(class2);
    }
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
            selectorToggle(element, 'selected', 'unselected');
            var pageNum = element.attr('id');
            if (element.hasClass('selected')) {
                window.selectedPages.push(pageNum);
            } else {
                window.selectedPages.splice( $.inArray(pageNum, window.selectedPages), 1 );
            }
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


      $scope.getCopySets= function(){
            var bwPages = [];
            var colorPages = [];
            var table = $('#thumbnail-table');
            var tbody = table.find('tbody');
            for (i = 1; i < window.pdfSession.numberOfPages; i++){
                var page = tbody.find('#' + i);
                if (page.hasClass('selectable selected')) {
                    colorPages.push(i);
                } else {
                    bwPages.push(i);
                }
            }
            console.log("bw pages : " + bwPages);
            console.log("color pages : "  + colorPages);
            getPrintString(bwPages);
        };

        var getPrintString = function(pages){
            var printString="";
            for (var i = 0; i < pages.length; i++) {
                var digit = pages[i];
                var nextDigit = pages[i + 1];
                stringEnd = printString.length - 1;
                var lastChar = printString.charAt(stringEnd);
                if ((digit + 1) == nextDigit) {
                    /*The output of this block is to create a string that can be easily copied into a print selected pages window
                    Example:
                    var pages = [1, 2, 3, 5, 6, 7, 9, 12, 13, 14, 15, 20];
                    output = 1-3,5-7,9,12-15,20
                    */
                    if (lastChar == "," || printString == "") {
                        printString = printString + digit + "-";
                    } else {
                        printString = printString;
                    }
                } else if (i == (pages.length - 1)) {
                    printString = printString + digit;
                } else {
                    if (printString.charAt(lastChar) == "-") {
                        printString = printString + digit;
                        //ends a "-" sequence
                    } else {
                        printString = printString + digit + ",";
                    }
                }
            }
        };

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

            var tbody = $('#thumbnail-table tbody');
            for (var i = 0; i < window.pdfStatus.completedPages.length; i++) {
                var pg = window.pdfStatus.completedPages[i];
                var element = tbody.find('#' + pg.pageNumber);

                if (pg.percentColor > upperRange) {
                    element.parent().parent().hide();
                    if(element.hasClass('unselected')){
                        selectorToggle(element, 'selected', 'unselected');
                        }
                    window.selectedPages.push(pg.pageNumber);
                } else if (pg.percentColor < lowerRange) {
                    element.parent().parent().hide();
                    if (element.hasClass('selected')){
                        selectorToggle(element, 'selected', 'unselected');
                    }
                    window.selectedPages.splice( $.inArray(pg.pageNumber, window.selectedPages), 1 );
                } else {
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
