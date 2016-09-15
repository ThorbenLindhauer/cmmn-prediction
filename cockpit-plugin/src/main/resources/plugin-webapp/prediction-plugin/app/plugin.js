define(['angular'], function(angular) {
    var ngModule = angular.module('cockpit.plugin.prediction-plugin', []);

    var CaseInteractionController = ["$scope", 'camAPI', "$http","Uri", function($scope, camAPI, $http,Uri) {
        var eMap = {
            0: '\ud83d\ude22',
            1: '\ud83d\ude1e',
            2: '\ud83d\ude14',
            3: '\ud83d\ude0a',
            4: '\ud83d\ude03',
            5: '\ud83d\ude03'
        };
        $scope.mapEmoji = function (p) {
            return eMap[Math.floor(p.probability * 100/20)];
        };

        if ($scope.instance.id) {
            $http.get(Uri.appUri("plugin://prediction-plugin/:engine/predictions/" + $scope.instance.id))
                .success(function(data) {

                    for (var i = 0;i < data.length; i++) {
                        var prediction = data[i];
                        prediction.emoji = $scope.mapEmoji(prediction);
                    }
                    $scope.predictions = data;

                });
        }
    }];

    var Configuration = ['ViewsProvider', function(ViewsProvider) {

        ViewsProvider.registerDefaultView('cockpit.caseInstance.tab', {
            id: 'cmmn-predictions',
            label: 'Predictions',
            url: 'plugin://prediction-plugin/static/app/predictions.html',
            dashboardMenuLabel: 'CMMN Predictions',
            controller: CaseInteractionController,


            // make sure we have a higher priority than the default plugin
            priority: 12
        });
    }];



    ngModule.config(Configuration);

    return ngModule;
});