define(['angular'], function(angular) {
    var ngModule = angular.module('cockpit.plugin.control-plugin', []);

    var CaseInteractionController = ["$scope", 'camAPI', "$http","Uri",'Notifications', function($scope, camAPI, $http,Uri,Notifications) {

        $scope.startCase = function() {
            $http.post(Uri.appUri("plugin://control-plugin/:engine/start/" + $scope.definition.id))
                .success(function(data) {
                    Notifications.addMessage({
                        type:'success',
                        status:'Success',
                        message:'Instance started'
                    });
                    $scope.caseInstance = data;
                });
        };

    }];

    var Configuration = ['ViewsProvider', function(ViewsProvider) {

        ViewsProvider.registerDefaultView('cockpit.caseDefinition.tab', {
            id: 'cmmn-control',
            label: 'CMMN Control',
            url: 'plugin://control-plugin/static/app/starter.html',
            dashboardMenuLabel: 'CMMN Control',
            controller: CaseInteractionController,


            // make sure we have a higher priority than the default plugin
            priority: 12
        });
    }];



    ngModule.config(Configuration);

    return ngModule;
});