define(['angular'], function(angular) {

    var DashboardController = ["$scope", "$http", "Uri", function($scope, $http, Uri) {
        //nothing to do yet
    }];

    var Configuration = ['ViewsProvider', function(ViewsProvider) {

        ViewsProvider.registerDefaultView('cockpit.dashboard', {
            id: 'process-definitions',
            label: 'CMMN Predictions',
            url: 'plugin://prediction-plugin/static/app/predictions.html',
            dashboardMenuLabel: 'Predictions',
            controller: DashboardController,

            // make sure we have a higher priority than the default plugin
            priority: 12
        });
    }];

    var ngModule = angular.module('cockpit.plugin.prediction-plugin', []);

    ngModule.config(Configuration);

    return ngModule;
});