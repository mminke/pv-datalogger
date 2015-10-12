app.controller('DashboardController', [
			'$scope',
			'$http',
			'$q',
			'$mdSidenav',
			'$interval',
			function($scope, $http, $q, $mdSidenav, $interval) {
								
				// Initialize the window with data
				refreshData($scope, $http, $q)
				
				// Setup refresh of the dashboard data every half minute
				$interval(function() { refreshData($scope, $http, $q) }, 30000); 					
			} ]);

function calculate($scope) {
	$scope.calculated = {
			progressToEstimatedValue: ($scope.actual.yield_ytd / $scope.inverter.estimated_yearly_yield) * 100
		};
}
	
function refreshData($scope, $http, $q) {
	var inverter_serialnumber = "<<YOUR SERIAL NUMBER>>";
		
	var inverter_promise = $http.get("api/inverters/" + inverter_serialnumber);
	var actualdata_promise = $http.get("api/inverters/" + inverter_serialnumber + "/data/actual");
	
	// After all data is loaded, do some calculations
	$q.all([inverter_promise, actualdata_promise]).then(function(promiseresults){
        $scope.inverter = promiseresults[0].data;
		$scope.actual = promiseresults[1].data;
		
        calculate($scope)  						
	});   					
	
}
