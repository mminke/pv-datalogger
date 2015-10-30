app.controller('MainController', [
			'$scope', '$rootScope', '$location', 'loginService',
			function($scope, $rootScope, $location, loginService) {
				
				$scope.session = loginService.session;
								
				$scope.logout = function () {
					loginService.logout();
					$location.path("/login");
				}	
			
			} ]);