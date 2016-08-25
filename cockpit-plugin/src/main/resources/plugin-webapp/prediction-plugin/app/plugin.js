define(['angular'
    , './components/prediction-instance-list'
], function(angular
    ,PI
) {
    var ngModule = angular.module('cockpit.plugin.prediction-plugin', []);

    var CaseDefinitionController = ["$scope", 'camAPI', "$http","Uri", function($scope, camAPI, $http,Uri) {
        console.log(PI);
        //var url = Uri.appUri("plugin://prediction-plugin/static/app/components/prediction-instance-list.js");
        $http.get(Uri.appUri("plugin://prediction-plugin/:engine/predictions/" + $scope.definition.id))
            .success(function(data) {
                $scope.predictions = data;
            });

    }];

    var Configuration = ['ViewsProvider', function(ViewsProvider) {

        ViewsProvider.registerDefaultView('cockpit.caseDefinition.tab', {
            id: 'process-definitions',
            label: 'CMMN Predictions',
            url: 'plugin://prediction-plugin/static/app/predictions.html',
            dashboardMenuLabel: 'Predictions',
            controller: CaseDefinitionController,


            // make sure we have a higher priority than the default plugin
            priority: 12
        });
    }];



    ngModule.config(Configuration);

    return ngModule;
});